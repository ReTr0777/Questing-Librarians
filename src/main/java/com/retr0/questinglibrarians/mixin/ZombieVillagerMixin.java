package com.retr0.questinglibrarians.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin {

    /**
     * Intercepts convertTo in finishConversion to add a random max-level Enchanted Book trade
     * to Level 5 (Master) Villagers upon curing. Guaranteed to occur only once per villager.
     */
    @Redirect(
            method = "finishConversion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
            )
    )
    private <T extends Mob> T onFinishConversion(ZombieVillager zombieVillager, EntityType<T> type, ConversionParams params, ConversionParams.AfterConversion<T> afterConversion, ServerLevel level) {
        T converted = zombieVillager.convertTo(type, params, afterConversion);

        if (converted instanceof Villager villager) {
            // Curing trade reward ONLY applies to Level 5 (Master) villagers, and only once per villager
            if (villager.getVillagerData().level() >= 5 && !villager.entityTags().contains("questing_librarians:cured")) {
                villager.addTag("questing_librarians:cured");

                var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                List<Holder.Reference<Enchantment>> enchantments = enchantmentRegistry.listElements().toList();

                if (!enchantments.isEmpty()) {
                    RandomSource random = level.getRandom();
                    Holder<Enchantment> chosenEnchantment = enchantments.get(random.nextInt(enchantments.size()));
                    int maxLevel = chosenEnchantment.value().getMaxLevel();

                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantmentHelper.updateEnchantments(enchantedBook, mutable -> mutable.set(chosenEnchantment, maxLevel));

                    // Mark the book as "traded" and "cured_trade" in CustomData
                    // "traded": prevents using this book to teach other villagers
                    // "cured_trade": protects this trade from being wiped by the Grindstone
                    CustomData.update(DataComponents.CUSTOM_DATA, enchantedBook, tag -> {
                        tag.putBoolean("traded", true);
                        tag.putBoolean("cured_trade", true);
                    });

                    MerchantOffer curedTrade = new MerchantOffer(
                            new ItemCost(Items.EMERALD, 7), // Level 5 emerald cost is 7
                            Optional.of(new ItemCost(Items.BOOK, 1)),
                            enchantedBook,
                            12,   // maxUses
                            5,    // villagerXp
                            0.05f // priceMultiplier
                    );

                    // Add the cured trade to the top of the villager's trade offers
                    villager.getOffers().add(0, curedTrade);
                }
            }
        }
        return converted;
    }
}
