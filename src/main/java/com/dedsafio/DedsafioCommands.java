package com.dedsafio;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class DedsafioCommands {
	private DedsafioCommands() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
			literal("dedsafio").requires(src -> src.hasPermission(2))
				.then(literal("roulette")
					.then(literal("spin").executes(ctx -> {
						MinecraftServer s = ctx.getSource().getServer();
						boolean ok = Roulette.spin(s, Dedsafio.state(s));
						if (!ok) ctx.getSource().sendFailure(Component.translatable("message.dedsafio.all_active"));
						return ok ? 1 : 0;
					}))
					.then(literal("list").executes(ctx -> {
						DedsafioState st = Dedsafio.state(ctx.getSource().getServer());
						MutableComponent out = Component.translatable("message.dedsafio.active_rules");
						if (st.rules.isEmpty()) out.append(Component.translatable("message.dedsafio.none"));
						for (Rule r : st.rules) out.append(Component.literal("\n - ")).append(r.text());
						ctx.getSource().sendSuccess(() -> out, false);
						return st.rules.size();
					}))
					.then(literal("reset").executes(ctx -> {
						MinecraftServer s = ctx.getSource().getServer();
						Roulette.reset(s, Dedsafio.state(s));
						ctx.getSource().sendSuccess(() -> Component.translatable("message.dedsafio.reset"), true);
						return 1;
					}))
					.then(literal("interval").then(argument("ticks", IntegerArgumentType.integer(200))
						.executes(ctx -> {
							DedsafioState st = Dedsafio.state(ctx.getSource().getServer());
							st.interval = IntegerArgumentType.getInteger(ctx, "ticks");
							st.ticksLeft = st.interval;
							st.setDirty();
							ctx.getSource().sendSuccess(() -> Component.literal("Intervalo: " + st.interval + " ticks"), true);
							return 1;
						}))))
				.then(literal("revive").then(argument("player", EntityArgument.player())
					.executes(ctx -> {
						ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
						if (!Dedsafio.state(ctx.getSource().getServer()).dead.contains(target.getUUID())) {
							ctx.getSource().sendFailure(Component.translatable("message.dedsafio.not_dead"));
							return 0;
						}
						Revival.revive(target, ctx.getSource().getPlayer());
						return 1;
					})))
		));
	}
}
