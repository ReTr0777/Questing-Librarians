package com.retr0.questinglibrarians.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {

    /**
     * Disables vanilla curing discounts so trade prices remain fixed.
     */
    @Inject(method = "updateSpecialPrices", at = @At("HEAD"), cancellable = true)
    private void disableSpecialPrices(Player player, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        for (MerchantOffer offer : villager.getOffers()) {
            offer.resetSpecialPriceDiff();
        }
        ci.cancel();
    }

    /**
     * Triggers a fanfare sound and particle celebration when a Librarian reaches Level 5 (Master).
     */
    @Inject(method = "setVillagerData", at = @At("HEAD"))
    private void onLevelUpToMaster(VillagerData newVillagerData, CallbackInfo ci) {
        Villager villager = (Villager) (Object) this;
        VillagerData currentData = villager.getVillagerData();

        if (currentData.profession().is(VillagerProfession.LIBRARIAN)) {
            if (currentData.level() < 5 && newVillagerData.level() >= 5) {
                Level world = villager.level();
                world.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1.0f, 1.0f);

                if (world instanceof ServerLevel serverWorld) {
                    serverWorld.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, villager.getX(), villager.getY() + 1.2, villager.getZ(), 40, 0.5, 0.5, 0.5, 0.15);
                    serverWorld.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0, villager.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                }
            }
        }
    }
}
