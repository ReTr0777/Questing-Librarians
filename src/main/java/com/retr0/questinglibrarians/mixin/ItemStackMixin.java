package com.retr0.questinglibrarians.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void addCustomTradeTooltips(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.is(Items.ENCHANTED_BOOK)) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            boolean isCuredTrade = customData.copyTag().getBooleanOr("cured_trade", false);
            boolean isTraded = customData.copyTag().getBooleanOr("traded", false);

            List<Component> tooltip = cir.getReturnValue();
            if (isCuredTrade) {
                tooltip.add(Component.literal("★ Cured Reward Trade").withStyle(ChatFormatting.GOLD));
            } else if (isTraded) {
                tooltip.add(Component.literal("✖ Cannot Teach Librarians (Traded Book)").withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.literal("✔ Can Teach Librarians (Found Book)").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
