package com.retr0.questinglibrarians.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {

    /**
     * Suppresses the default "Trades" header so it can be replaced by the Book Slots note.
     */
    @Redirect(
            method = "extractLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"
            )
    )
    private void suppressTradesTitle(GuiGraphicsExtractor graphicsExtractor, Font font, Component component, int x, int y, int color, boolean shadow) {
        if (component != null && component.getString().toLowerCase().contains("trade")) {
            // Skip rendering default "Trades" header
            return;
        }
        graphicsExtractor.text(font, component, x, y, color, shadow);
    }

    /**
     * Renders the Book Slots note at x = 8, y = 6 on the top left above the trades list.
     */
    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void renderBookSlotsLabel(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        MerchantOffers offers = screen.getMenu().getOffers();

        if (offers != null) {
            int customBookCount = 0;
            boolean isCured = false;

            for (MerchantOffer offer : offers) {
                ItemStack result = offer.getResult();
                if (result.is(Items.ENCHANTED_BOOK)) {
                    customBookCount++;
                    CustomData customData = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    if (customData.copyTag().getBooleanOr("cured_trade", false)) {
                        isCured = true;
                    }
                }
            }

            int traderLevel = screen.getMenu().getTraderLevel();
            int maxBooks = isCured ? 4 : ((traderLevel >= 5) ? 3 : 2);

            // Render Book Slots label at x = 8, y = 6 (replacing "Trades")
            Component noteText = Component.literal("Book Slots: " + customBookCount + "/" + maxBooks);
            graphicsExtractor.text(Minecraft.getInstance().font, noteText, 8, 6, 0xFF404040, false);
        }
    }
}
