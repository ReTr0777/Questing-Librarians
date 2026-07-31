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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {

    private boolean isLibrarianScreen(MerchantScreen screen) {
        if (screen == null || screen.getTitle() == null) return false;
        String title = screen.getTitle().getString().toLowerCase();
        if (title.contains("librarian")) return true;
        MerchantOffers offers = screen.getMenu().getOffers();
        if (offers != null) {
            for (MerchantOffer offer : offers) {
                if (offer.getResult().is(Items.ENCHANTED_BOOK)) return true;
            }
        }
        return false;
    }

    /**
     * Renders the Book Slots note and a clickable [?] Help button in extractLabels for Librarians.
     */
    @Inject(method = "extractLabels", at = @At("TAIL"))
    private void renderBookSlotsLabel(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        if (!isLibrarianScreen(screen)) return;

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
            int maxNormal = com.retr0.questinglibrarians.config.QuestingLibrariansConfig.maxBooksNormal;
            int maxMaster = com.retr0.questinglibrarians.config.QuestingLibrariansConfig.maxBooksMaster;
            int maxCured = com.retr0.questinglibrarians.config.QuestingLibrariansConfig.maxBooksCured;
            int maxBooks = isCured ? maxCured : ((traderLevel >= 5) ? maxMaster : maxNormal);

            // Cover default "Trades" header with container gray background (x = 7 to 85, y = 5 to 16)
            graphicsExtractor.fill(7, 5, 85, 16, 0xFFC6C6C6);

            // Render Book Slots label at x = 8, y = 6 (replacing "Trades")
            Component noteText = Component.literal("Book Slots: " + customBookCount + "/" + maxBooks);
            graphicsExtractor.text(Minecraft.getInstance().font, noteText, 8, 6, 0xFF404040, false);

            // Render clickable [?] Help button next to the label (x = 90, y = 6)
            Component helpButton = Component.literal("[?]");
            graphicsExtractor.text(Minecraft.getInstance().font, helpButton, 90, 6, 0xFF4A90E2, true);
        }
    }

    /**
     * Renders the first-time tutorial overlay card dead-centered on the screen strictly for Librarians.
     */
    @Inject(method = "extractContents", at = @At("TAIL"))
    private void renderTutorialOverlay(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        if (!isLibrarianScreen(screen)) return;

        MerchantOffers offers = screen.getMenu().getOffers();
        boolean hasCustomTrade = false;
        if (offers != null) {
            for (MerchantOffer offer : offers) {
                ItemStack result = offer.getResult();
                if (result.is(Items.ENCHANTED_BOOK)) {
                    CustomData customData = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    if (customData.copyTag().getBooleanOr("traded", false) || customData.copyTag().getBooleanOr("cured_trade", false)) {
                        hasCustomTrade = true;
                        break;
                    }
                }
            }
        }

        if (hasCustomTrade) {
            for (net.minecraft.client.gui.components.events.GuiEventListener listener : net.fabricmc.fabric.api.client.screen.v1.Screens.getWidgets(screen)) {
                if (listener.getClass().getSimpleName().equals("CycleTradesButton")) {
                    if (listener instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                        if (widget.visible) {
                            widget.visible = false;
                            widget.active = false;
                        }
                    }
                }
            }
        }

        if (!TutorialConfig.hasSeenTutorial) {
            Font font = Minecraft.getInstance().font;

            int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
            int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

            // 1. Dim full screen background
            graphicsExtractor.fill(0, 0, screenWidth, screenHeight, 0xD0000000);

            // 2. Exact screen center
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            int minX = centerX - 160;
            int maxX = centerX + 160;
            int minY = centerY - 80;
            int maxY = centerY + 80;

            // Tutorial Card Box (320x160) dead-centered on screen
            graphicsExtractor.fill(minX, minY, maxX, maxY, 0xF0141418);

            // Blue accent borders
            graphicsExtractor.fill(minX - 2, minY - 2, maxX + 2, minY, 0xFF4A90E2);
            graphicsExtractor.fill(minX - 2, maxY, maxX + 2, maxY + 2, 0xFF4A90E2);
            graphicsExtractor.fill(minX - 2, minY - 2, minX, maxY + 2, 0xFF4A90E2);
            graphicsExtractor.fill(maxX, minY - 2, maxX + 2, maxY + 2, 0xFF4A90E2);

            // Centered Header (Yellow)
            Component title = Component.literal("Questing Librarians Guide");
            int titleX = centerX - (font.width(title) / 2);
            graphicsExtractor.text(font, title, titleX, minY + 8, 0xFFFFD700, true);

            // Guide Lines
            int textLeft = minX + 16;

            // Line 1: Teach Trades
            Component b1Header = Component.literal("1. Teach Trades:");
            Component b1Desc1 = Component.literal("   Right-click a Librarian with a found");
            Component b1Desc2 = Component.literal("   Enchanted Book to teach trades.");
            graphicsExtractor.text(font, b1Header, textLeft, minY + 22, 0xFFFFFFFF, false);
            graphicsExtractor.text(font, b1Desc1, textLeft, minY + 32, 0xFFA0A0A0, false);
            graphicsExtractor.text(font, b1Desc2, textLeft, minY + 41, 0xFFA0A0A0, false);

            // Line 2: Curing Bonus
            Component b2Header = Component.literal("2. Curing Bonus:");
            Component b2Desc1 = Component.literal("   Cure a Master Zombie Villager for");
            Component b2Desc2 = Component.literal("   a guaranteed max-level book trade!");
            graphicsExtractor.text(font, b2Header, textLeft, minY + 54, 0xFFFFFFFF, false);
            graphicsExtractor.text(font, b2Desc1, textLeft, minY + 64, 0xFFA0A0A0, false);
            graphicsExtractor.text(font, b2Desc2, textLeft, minY + 73, 0xFFA0A0A0, false);

            // Line 3: Grindstone Reset
            Component b3Header = Component.literal("3. Grindstone Reset:");
            Component b3Desc1 = Component.literal("   Right-click a Librarian with a Grindstone");
            Component b3Desc2 = Component.literal("   to wipe taught trades & refund Books.");
            graphicsExtractor.text(font, b3Header, textLeft, minY + 86, 0xFFFFFFFF, false);
            graphicsExtractor.text(font, b3Desc1, textLeft, minY + 96, 0xFFA0A0A0, false);
            graphicsExtractor.text(font, b3Desc2, textLeft, minY + 105, 0xFFA0A0A0, false);

            // Centered Button Box
            int btnWidth = 130;
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
     * Intercepts mouse clicks strictly for Librarians to dismiss or re-open the tutorial guide card.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onTutorialClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        if (!isLibrarianScreen(screen)) return;

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
