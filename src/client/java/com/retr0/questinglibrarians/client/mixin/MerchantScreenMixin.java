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
     * Suppresses the default "Trades" header so it can be replaced by the Book Slots note and Help button.
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
     * Renders the Book Slots note and a clickable [?] Help button in extractLabels.
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

            // Render clickable [?] Help button next to the label (x = 90, y = 6)
            Component helpButton = Component.literal("[?]");
            graphicsExtractor.text(Minecraft.getInstance().font, helpButton, 90, 6, 0xFF4A90E2, true);
        }
    }

    /**
     * Renders the first-time tutorial overlay card dead-centered on the player's screen monitor.
     */
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void renderTutorialOverlay(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!TutorialConfig.hasSeenTutorial) {
            MerchantScreen screen = (MerchantScreen) (Object) this;
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
            Font font = Minecraft.getInstance().font;

            int left = accessor.getLeftPos();
            int top = accessor.getTopPos();

            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            // 1. Dim full screen background
            graphicsExtractor.fill(-left, -top, screenWidth - left, screenHeight - top, 0xD0000000);

            // 2. Exact screen center relative to container space
            int centerX = (screenWidth / 2) - left;
            int centerY = (screenHeight / 2) - top;

            int minX = centerX - 120;
            int maxX = centerX + 120;
            int minY = centerY - 70;
            int maxY = centerY + 70;

            // Tutorial Card Box (240x140) dead-centered on screen
            graphicsExtractor.fill(minX, minY, maxX, maxY, 0xF0141418);

            // Blue accent borders
            graphicsExtractor.fill(minX - 2, minY - 2, maxX + 2, minY, 0xFF4A90E2);
            graphicsExtractor.fill(minX - 2, maxY, maxX + 2, maxY + 2, 0xFF4A90E2);
            graphicsExtractor.fill(minX - 2, minY - 2, minX, maxY + 2, 0xFF4A90E2);
            graphicsExtractor.fill(maxX, minY - 2, maxX + 2, maxY + 2, 0xFF4A90E2);

            // Centered Header
            Component title = Component.literal("Questing Librarians Guide");
            int titleX = centerX - (font.width(title) / 2);
            graphicsExtractor.text(font, title, titleX, minY + 8, 0xFFFFD700, true);

            // Guide Lines
            int textLeft = minX + 12;
            graphicsExtractor.text(font, Component.literal("1. Teach Trades: Right-click a Librarian"), textLeft, minY + 24, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a found Enchanted Book."), textLeft, minY + 34, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("2. Curing Bonus: Cure a Master Zombie"), textLeft, minY + 48, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   Villager for a max-level book trade!"), textLeft, minY + 58, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("3. Grindstone Reset: Wipe taught trades"), textLeft, minY + 72, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a Grindstone to refund Books."), textLeft, minY + 82, 0xFFA0A0A0, false);

            // Centered Button Box
            int btnWidth = 120;
            int btnHeight = 20;
            int btnMinX = centerX - (btnWidth / 2);
            int btnMaxX = centerX + (btnWidth / 2);
            int btnMinY = maxY - 26;
            int btnMaxY = maxY - 6;

            graphicsExtractor.fill(btnMinX, btnMinY, btnMaxX, btnMaxY, 0xFF2E7D32);
            graphicsExtractor.fill(btnMinX - 2, btnMinY - 2, btnMaxX + 2, btnMinY, 0xFF4CAF50);
            graphicsExtractor.fill(btnMinX - 2, btnMaxY, btnMaxX + 2, btnMaxY + 2, 0xFF4CAF50);

            // Button Label
            Component buttonText = Component.literal("[ Click to Continue ]");
            int buttonX = centerX - (font.width(buttonText) / 2);
            graphicsExtractor.text(font, buttonText, buttonX, btnMinY + 6, 0xFFFFFFFF, true);
        }
    }

    /**
     * Intercepts mouse clicks to dismiss or re-open the tutorial guide card.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onTutorialClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        MerchantScreen screen = (MerchantScreen) (Object) this;

        if (!TutorialConfig.hasSeenTutorial) {
            // Dismiss active tutorial on click
            TutorialConfig.hasSeenTutorial = true;
            TutorialConfig.save();
            cir.setReturnValue(true);
        } else {
            // Check if user clicked the [?] Help button at x = 85..110, y = 4..16 relative to leftPos/topPos
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
            double relX = event.x() - accessor.getLeftPos();
            double relY = event.y() - accessor.getTopPos();

            if (relX >= 85 && relX <= 110 && relY >= 4 && relY <= 16) {
                // Re-open tutorial guide card
                TutorialConfig.hasSeenTutorial = false;
                cir.setReturnValue(true);
            }
        }
    }
}
