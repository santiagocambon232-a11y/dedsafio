package com.dedsafio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ResurrectionSpoonItem extends Item {
	public ResurrectionSpoonItem(Properties properties) { super(properties); }

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) return InteractionResultHolder.success(stack);

		ServerPlayer target = Revival.nextDeadOnline(player.getServer());
		if (target == null) {
			player.displayClientMessage(Component.translatable("message.dedsafio.no_dead"), true);
			return InteractionResultHolder.fail(stack);
		}
		Revival.revive(target, (ServerPlayer) player);
		stack.consume(1, player);
		return InteractionResultHolder.success(stack);
	}

	@Override
	public boolean isFoil(ItemStack stack) { return true; }
}
