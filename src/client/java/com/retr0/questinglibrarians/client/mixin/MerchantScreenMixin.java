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

            // Render clickable [?] Help button next to the label (x = 85, y = 6)
            Component helpButton = Component.literal("[?]");
            graphicsExtractor.text(Minecraft.getInstance().font, helpButton, 85, 6, 0xFF4A90E2, true);
        }
    }

    /**
     * Renders the first-time tutorial overlay card perfectly centered on the screen.
     */
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void renderTutorialOverlay(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!TutorialConfig.hasSeenTutorial) {
            MerchantScreen screen = (MerchantScreen) (Object) this;
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
            Font font = Minecraft.getInstance().font;

            int left = accessor.getLeftPos();
            int top = accessor.getTopPos();

            // Full-screen dim overlay covering absolute screen bounds (0,0 to screenWidth,screenHeight)
            graphicsExtractor.fill(-left, -top, screen.width - left, screen.height - top, 0xD0000000);

            // Centered Tutorial Card (X: 23 to 253, Y: 15 to 151) inside container space
            graphicsExtractor.fill(23, 15, 253, 151, 0xF0141418);

            // Blue accent borders
            graphicsExtractor.fill(21, 13, 255, 15, 0xFF4A90E2);
            graphicsExtractor.fill(21, 151, 255, 153, 0xFF4A90E2);
            graphicsExtractor.fill(21, 13, 23, 153, 0xFF4A90E2);
            graphicsExtractor.fill(253, 13, 255, 153, 0xFF4A90E2);

            // Centered Header
            Component title = Component.literal("Questing Librarians Guide");
            int titleX = 138 - (font.width(title) / 2);
            graphicsExtractor.text(font, title, titleX, 22, 0xFFFFD700, true);

            // Guide Lines
            graphicsExtractor.text(font, Component.literal("1. Teach Trades: Right-click a Librarian"), 31, 38, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a found Enchanted Book."), 31, 48, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("2. Curing Bonus: Cure a Master Zombie"), 31, 62, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   Villager for a max-level book trade!"), 31, 72, 0xFFA0A0A0, false);

            graphicsExtractor.text(font, Component.literal("3. Grindstone Reset: Wipe taught trades"), 31, 86, 0xFFE0E0E0, false);
            graphicsExtractor.text(font, Component.literal("   with a Grindstone to refund Books."), 31, 96, 0xFFA0A0A0, false);

            // Centered Button Box
            graphicsExtractor.fill(78, 120, 198, 140, 0xFF2E7D32);
            graphicsExtractor.fill(76, 118, 200, 120, 0xFF4CAF50);
            graphicsExtractor.fill(76, 140, 200, 142, 0xFF4CAF50);

            // Button Label
            Component buttonText = Component.literal("[ Click to Continue ]");
            int buttonX = 138 - (font.width(buttonText) / 2);
            graphicsExtractor.text(font, buttonText, buttonX, 126, 0xFFFFFFFF, true);
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
            // Check if user clicked the [?] Help button at x = 80..105, y = 4..16 relative to leftPos/topPos
            AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
            double relX = event.x() - accessor.getLeftPos();
            double relY = event.y() - accessor.getTopPos();

            if (relX >= 80 && relX <= 105 && relY >= 4 && relY <= 16) {
                // Re-open tutorial guide card
                TutorialConfig.hasSeenTutorial = false;
                cir.setReturnValue(true);
            }
        }
    }
}
