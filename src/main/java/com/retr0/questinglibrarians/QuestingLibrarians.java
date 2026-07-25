package com.retr0.questinglibrarians;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class QuestingLibrarians implements ModInitializer {
	public static final String MOD_ID = "questing-librarians";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Questing Librarians!");

		// Register interaction event to allow Librarians to learn and unlearn Enchanted Book trades
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			// Ensure logic only executes on the server side to prevent desyncs
			if (!world.isClientSide()) {
				// Check if targeted entity is a Villager with Librarian profession
				if (entity instanceof Villager villager
						&& villager.getVillagerData().profession().is(VillagerProfession.LIBRARIAN)) {

					ItemStack itemInHand = player.getItemInHand(hand);

					// Feature 1: Grindstone Unlearning
					// Right-clicking a Librarian with a Grindstone removes ALL TAUGHT Enchanted Book trades
					// and gives back regular Books to the player. Cured reward trades (tagged cured_trade) are preserved.
					if (itemInHand.is(Items.GRINDSTONE)) {
						MerchantOffers offers = villager.getOffers();
						int removedCount = 0;
						for (int i = offers.size() - 1; i >= 0; i--) {
							MerchantOffer offer = offers.get(i);
							ItemStack result = offer.getResult();
							if (result.is(Items.ENCHANTED_BOOK)) {
								CustomData customData = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
								boolean isCuredTrade = customData.copyTag().getBooleanOr("cured_trade", false);
								// Do NOT remove cured reward trades
								if (!isCuredTrade) {
									offers.remove(i);
									removedCount++;
								}
							}
						}

						if (removedCount > 0) {
							// Return normal books back to the player (drop if inventory is full)
							ItemStack returnedBooks = new ItemStack(Items.BOOK, removedCount);
							if (!player.addItem(returnedBooks)) {
								player.drop(returnedBooks, false);
							}

							// Play grindstone sound & spawn poof particles
							world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
									SoundEvents.GRINDSTONE_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);

							if (world instanceof ServerLevel serverWorld) {
								serverWorld.sendParticles(ParticleTypes.POOF, villager.getX(), villager.getY() + 1.0, villager.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
							}

							return InteractionResult.SUCCESS;
						}
					}

					// Feature 2: Teaching Enchanted Books (Found Books ONLY, not Traded/Bought Books)
					if (itemInHand.is(Items.ENCHANTED_BOOK)) {
						// Reject traded/bought books
						CustomData customData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
						if (customData.copyTag().getBooleanOr("traded", false)) {
							world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
									SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
							return InteractionResult.FAIL;
						}

						MerchantOffers offers = villager.getOffers();

						// Count existing taught Enchanted Book trades on this villager
						int customBookCount = 0;
						for (MerchantOffer offer : offers) {
							if (offer.getResult().is(Items.ENCHANTED_BOOK)) {
								customBookCount++;
							}
						}

						// Calculate maximum allowed book trades:
						// If cured: Max 4 slots total (1 cured trade + 3 taught trades)
						// Otherwise: Levels 1-4 = Max 2 books | Level 5 (Master) = Max 3 books
						int villagerLevel = villager.getVillagerData().level();
						boolean isCured = villager.entityTags().contains("questing_librarians:cured");
						int maxBooks = isCured ? 4 : ((villagerLevel >= 5) ? 3 : 2);

						// Refuse learning if maximum capacity has been reached
						if (customBookCount >= maxBooks) {
							world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
									SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0f, 1.0f);
							return InteractionResult.FAIL;
						}

						// Copy exact Enchanted Book item stack from player's hand (setting count to 1 for trade output)
						ItemStack tradeBook = itemInHand.copy();
						tradeBook.setCount(1);

						// Mark the sold Enchanted Book as "traded" so it cannot be used to teach other villagers
						CustomData.update(DataComponents.CUSTOM_DATA, tradeBook, tag -> tag.putBoolean("traded", true));

						// Calculate emerald cost based on villager level
						// Level 1 = 15, Level 2 = 13, Level 3 = 11, Level 4 = 9, Level 5 = 7
						int emeraldCost = Math.max(1, 15 - (villagerLevel - 1) * 2);

						// Create a new TradeOffer: (15 - level discount) Emeralds + 1 Book -> Provided Enchanted Book (Traded)
						MerchantOffer newOffer = new MerchantOffer(
								new ItemCost(Items.EMERALD, emeraldCost),
								Optional.of(new ItemCost(Items.BOOK, 1)),
								tradeBook,
								12,   // maxUses
								5,    // villagerXp
								0.05f // priceMultiplier
						);

						// Insert new offer at index 0 so it always appears at the top of the trading list
						offers.add(0, newOffer);

						// Decrement Enchanted Book from player's hand by 1 (unless player is in Creative mode)
						if (!player.getAbilities().instabuild) {
							itemInHand.shrink(1);
						}

						// Play enchantment table chime & villager work sound
						world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
								SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);
						world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
								SoundEvents.VILLAGER_WORK_LIBRARIAN, SoundSource.NEUTRAL, 0.8f, 1.0f);

						// Spawn glowing enchantment glyph particles around villager
						if (world instanceof ServerLevel serverWorld) {
							serverWorld.sendParticles(ParticleTypes.ENCHANT, villager.getX(), villager.getY() + 1.0, villager.getZ(), 25, 0.5, 0.5, 0.5, 0.1);
							serverWorld.sendParticles(ParticleTypes.ENCHANTED_HIT, villager.getX(), villager.getY() + 1.0, villager.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
						}

						// Return SUCCESS to cleanly consume interaction event
						return InteractionResult.SUCCESS;
					}
				}
			}
			return InteractionResult.PASS;
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
