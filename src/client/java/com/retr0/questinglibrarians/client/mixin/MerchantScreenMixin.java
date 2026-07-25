package com.retr0.questinglibrarians.client.mixin;

import com.retr0.questinglibrarians.client.config.TutorialConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
     * Renders the Book Slots note and the first-time tutorial card overlay.
     */
    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void renderBookSlotsLabelAndTutorial(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, CallbackInfo ci) {
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

        // Render first-time tutorial overlay card if the player has not seen it yet
        if (!TutorialConfig.hasSeenTutorial) {
            Font font = Minecraft.getInstance().font;

            // Semi-transparent dark background card (-10, 10) to (286, 156)
            graphicsExtractor.fill(-10, 10, 286, 156, 0xF0181820);

            // Blue accent borders
            graphicsExtractor.fill(-12, 8, 288, 10, 0xFF4A90E2);
            graphicsExtractor.fill(-12, 156, 288, 158, 0xFF4A90E2);
            graphicsExtractor.fill(-12, 8, -10, 158, 0xFF4A90E2);
            graphicsExtractor.fill(286, 8, 288, 158, 0xFF4A90E2);

            // Tutorial Title
            graphicsExtractor.text(font, Component.literal("Questing Librarians Guide"), -2, 16, 0xFFFFD700, true);

            // Tutorial Lines
            graphicsExtractor.text(font, Component.literal("• Teach Trades: Right-click a Librarian while holding"), -2, 34, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("  a found Enchanted Book to teach them the trade."), -2, 46, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("• Curing Bonus: Cure a Master (Lvl 5) Zombie Villager"), -2, 62, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("  for a random max-level book trade & 4th slot!"), -2, 74, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("• Reset Trades: Right-click with a Grindstone to wipe"), -2, 90, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("  taught trades and receive regular Books back."), -2, 102, 0xFFA0A0A0, false);

            // Button Box at bottom center
            graphicsExtractor.fill(70, 126, 206, 146, 0xFF2E7D32);
            graphicsExtractor.fill(68, 124, 208, 126, 0xFF4CAF50);
            graphicsExtractor.fill(68, 146, 208, 148, 0xFF4CAF50);

            // Button Label
            graphicsExtractor.text(font, Component.literal("[ Click to Continue ]"), 78, 132, 0xFFFFFFFF, true);
        }
    }

    /**
     * Intercepts mouse clicks when the tutorial is active to dismiss it permanently.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onTutorialClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!TutorialConfig.hasSeenTutorial) {
            TutorialConfig.hasSeenTutorial = true;
            TutorialConfig.save();
            cir.setReturnValue(true);
        }
    }
}
