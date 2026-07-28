package com.retr0.questinglibrarians.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerMixin {

    @Inject(method = "overrideOffers", at = @At("HEAD"), cancellable = true)
    private void preventCycleOfCustomTrades(MerchantOffers offers, CallbackInfo ci) {
        if (offers == null || offers.isEmpty()) {
            if (isCalledFromTradeCycling()) {
                AbstractVillager self = (AbstractVillager) (Object) this;
                MerchantOffers currentOffers = self.getOffers();
                if (currentOffers != null) {
                    for (MerchantOffer offer : currentOffers) {
                        ItemStack result = offer.getResult();
                        if (result.is(Items.ENCHANTED_BOOK)) {
                            CustomData customData = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                            if (customData.copyTag().getBooleanOr("traded", false) || customData.copyTag().getBooleanOr("cured_trade", false)) {
                                ci.cancel();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isCalledFromTradeCycling() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("de.maxhenkel.tradecycling")) {
                return true;
            }
        }
        return false;
    }
}
