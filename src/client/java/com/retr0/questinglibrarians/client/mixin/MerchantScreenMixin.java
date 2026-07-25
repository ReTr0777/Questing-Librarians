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
     * Renders the Book Slots note in extractLabels.
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

    /**
     * Renders the first-time tutorial overlay card centered inside the trading window.
     */
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void renderTutorialOverlay(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!TutorialConfig.hasSeenTutorial) {
            Font font = Minecraft.getInstance().font;

            // Full-screen dim overlay
            graphicsExtractor.fill(-300, -300, 600, 600, 0xD0000000);

            // Centered Tutorial Card (X: 28 to 248, Y: 18 to 148)
            graphicsExtractor.fill(28, 18, 248, 148, 0xF0141418);

            // Blue accent borders
            graphicsExtractor.fill(26, 16, 250, 18, 0xFF4A90E2);
            graphicsExtractor.fill(26, 148, 250, 150, 0xFF4A90E2);
            graphicsExtractor.fill(26, 16, 28, 150, 0xFF4A90E2);
            graphicsExtractor.fill(248, 16, 250, 150, 0xFF4A90E2);

            // Centered Header
            Component title = Component.literal("Questing Librarians Guide");
            int titleX = 138 - (font.width(title) / 2);
            graphicsExtractor.text(font, title, titleX, 24, 0xFFFFD700, true);

            // Guide Lines
            graphicsExtractor.text(font, Component.literal("1. Teach Trades: Right-click a Librarian"), 36, 40, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a found Enchanted Book."), 36, 50, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("2. Curing Bonus: Cure a Master Zombie"), 36, 64, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   Villager for a max-level book trade!"), 36, 74, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("3. Grindstone Reset: Wipe taught trades"), 36, 88, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a Grindstone to refund Books."), 36, 98, 0xFFA0A0A0, false);

            // Centered Button Box
            graphicsExtractor.fill(78, 120, 198, 138, 0xFF2E7D32);
            graphicsExtractor.fill(76, 118, 200, 120, 0xFF4CAF50);
            graphicsExtractor.fill(76, 138, 200, 140, 0xFF4CAF50);

            // Button Label
            Component buttonText = Component.literal("[ Click to Continue ]");
            int buttonX = 138 - (font.width(buttonText) / 2);
            graphicsExtractor.text(font, buttonText, buttonX, 124, 0xFFFFFFFF, true);
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
