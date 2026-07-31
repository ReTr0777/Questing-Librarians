package com.retr0.questinglibrarians.config;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class QuestingLibrariansConfigMenu extends AbstractContainerMenu {
    private final Container container;

    public QuestingLibrariansConfigMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(27));
    }

    public QuestingLibrariansConfigMenu(int containerId, Inventory playerInventory, Container container) {
        super(MenuType.GENERIC_9x3, containerId);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Add container slots (0 to 26)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Add player inventory slots (27 to 53)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar slots (54 to 62)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        updateItems();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        // Prevent all interaction with the container slots except config changes
        if (slotId >= 0 && slotId < 27) {
            if (clickType == ContainerInput.PICKUP) {
                handleConfigClick(slotId, button, player);
            }
            return;
        }

        // Also prevent shift-clicking player items into the GUI
        if (clickType == ContainerInput.QUICK_MOVE) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    private void handleConfigClick(int slotId, int button, Player player) {
        boolean changed = false;
        boolean isRightClick = (button == 1);

        switch (slotId) {
            // Booleans
            case 10:
                QuestingLibrariansConfig.disableCuringDiscounts = !QuestingLibrariansConfig.disableCuringDiscounts;
                changed = true;
                break;
            case 11:
                QuestingLibrariansConfig.allowGrindstoneUnlearning = !QuestingLibrariansConfig.allowGrindstoneUnlearning;
                changed = true;
                break;
            case 12:
                QuestingLibrariansConfig.allowTradeUpgrading = !QuestingLibrariansConfig.allowTradeUpgrading;
                changed = true;
                break;
            case 13:
                QuestingLibrariansConfig.guaranteeZombification = !QuestingLibrariansConfig.guaranteeZombification;
                changed = true;
                break;
            case 14:
                QuestingLibrariansConfig.curingRewardEnabled = !QuestingLibrariansConfig.curingRewardEnabled;
                changed = true;
                break;

            // Integers
            case 19:
                QuestingLibrariansConfig.maxBooksNormal = adjustInt(QuestingLibrariansConfig.maxBooksNormal, isRightClick, 0, 10);
                changed = true;
                break;
            case 20:
                QuestingLibrariansConfig.maxBooksMaster = adjustInt(QuestingLibrariansConfig.maxBooksMaster, isRightClick, 0, 10);
                changed = true;
                break;
            case 21:
                QuestingLibrariansConfig.maxBooksCured = adjustInt(QuestingLibrariansConfig.maxBooksCured, isRightClick, 0, 10);
                changed = true;
                break;
            case 22:
                QuestingLibrariansConfig.baseEmeraldCost = adjustInt(QuestingLibrariansConfig.baseEmeraldCost, isRightClick, 1, 64);
                changed = true;
                break;
            case 23:
                QuestingLibrariansConfig.discountPerLevel = adjustInt(QuestingLibrariansConfig.discountPerLevel, isRightClick, 0, 64);
                changed = true;
                break;
            case 24:
                QuestingLibrariansConfig.curingRewardMinLevel = adjustInt(QuestingLibrariansConfig.curingRewardMinLevel, isRightClick, 1, 5);
                changed = true;
                break;
            case 25:
                QuestingLibrariansConfig.curingRewardEmeraldCost = adjustInt(QuestingLibrariansConfig.curingRewardEmeraldCost, isRightClick, 1, 64);
                changed = true;
                break;
        }

        if (changed) {
            QuestingLibrariansConfig.save();
            updateItems();
            this.broadcastChanges();

            // Sync to clients on change
            if (player.level().getServer() != null) {
                ConfigSyncPayload syncPayload = new ConfigSyncPayload(
                        QuestingLibrariansConfig.maxBooksNormal,
                        QuestingLibrariansConfig.maxBooksMaster,
                        QuestingLibrariansConfig.maxBooksCured
                );
                for (net.minecraft.server.level.ServerPlayer onlinePlayer : player.level().getServer().getPlayerList().getPlayers()) {
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(onlinePlayer, syncPayload);
                }
            }

            // Play click feedback sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    private int adjustInt(int current, boolean decrease, int min, int max) {
        if (decrease) {
            return Math.max(min, current - 1);
        } else {
            return Math.min(max, current + 1);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public void updateItems() {
        ItemStack glass = new ItemStack(BuiltInRegistries.ITEM.get(Identifier.fromNamespaceAndPath("minecraft", "gray_stained_glass_pane")).orElseThrow());
        glass.set(DataComponents.CUSTOM_NAME, Component.literal(" "));

        for (int i = 0; i < 27; i++) {
            container.setItem(i, glass.copy());
        }

        // Toggles row (10-14)
        container.setItem(10, createGuiItem(
                QuestingLibrariansConfig.disableCuringDiscounts ? Items.REDSTONE_BLOCK : Items.REDSTONE,
                "Disable Curing Discounts",
                getStatusComponent(QuestingLibrariansConfig.disableCuringDiscounts),
                "Left-click to toggle"
        ));

        container.setItem(11, createGuiItem(
                Items.GRINDSTONE,
                "Allow Grindstone Unlearning",
                getStatusComponent(QuestingLibrariansConfig.allowGrindstoneUnlearning),
                "Left-click to toggle"
        ));

        container.setItem(12, createGuiItem(
                Items.ANVIL,
                "Allow Trade Upgrading",
                getStatusComponent(QuestingLibrariansConfig.allowTradeUpgrading),
                "Left-click to toggle"
        ));

        container.setItem(13, createGuiItem(
                Items.ROTTEN_FLESH,
                "Guarantee Zombification",
                getStatusComponent(QuestingLibrariansConfig.guaranteeZombification),
                "Left-click to toggle"
        ));

        container.setItem(14, createGuiItem(
                Items.TOTEM_OF_UNDYING,
                "Curing Reward",
                getStatusComponent(QuestingLibrariansConfig.curingRewardEnabled),
                "Left-click to toggle"
        ));

        // Integers row (19-25)
        container.setItem(19, createGuiItem(
                Items.BOOK,
                "Max Books (Level 1-4)",
                getValueComponent(QuestingLibrariansConfig.maxBooksNormal),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(20, createGuiItem(
                Items.WRITABLE_BOOK,
                "Max Books (Master)",
                getValueComponent(QuestingLibrariansConfig.maxBooksMaster),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(21, createGuiItem(
                Items.ENCHANTED_BOOK,
                "Max Books (Cured)",
                getValueComponent(QuestingLibrariansConfig.maxBooksCured),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(22, createGuiItem(
                Items.EMERALD,
                "Base Emerald Cost",
                getValueComponent(QuestingLibrariansConfig.baseEmeraldCost),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(23, createGuiItem(
                Items.GOLD_INGOT,
                "Emerald Discount per Level",
                getValueComponent(QuestingLibrariansConfig.discountPerLevel),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(24, createGuiItem(
                Items.EXPERIENCE_BOTTLE,
                "Curing Reward Min Level",
                getValueComponent(QuestingLibrariansConfig.curingRewardMinLevel),
                "Left-click to +1",
                "Right-click to -1"
        ));

        container.setItem(25, createGuiItem(
                Items.EMERALD_BLOCK,
                "Curing Reward Emerald Cost",
                getValueComponent(QuestingLibrariansConfig.curingRewardEmeraldCost),
                "Left-click to +1",
                "Right-click to -1"
        ));
    }

    private ItemStack createGuiItem(Item item, String name, Component statusText, String... instructions) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD));

        List<Component> components = new ArrayList<>();
        components.add(statusText);
        for (String line : instructions) {
            components.add(Component.literal(line).withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
        stack.set(DataComponents.LORE, new ItemLore(components));
        return stack;
    }

    private Component getStatusComponent(boolean enabled) {
        return Component.literal("Current: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(enabled ? "ENABLED" : "DISABLED")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private Component getValueComponent(int value) {
        return Component.literal("Current: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(value)).withStyle(ChatFormatting.YELLOW));
    }
}
