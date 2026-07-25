package com.retr0.questinglibrarians.mixin;

import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
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
}
