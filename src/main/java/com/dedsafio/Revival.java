package com.dedsafio;

import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;

/** Muerte = espectador hasta que alguien use una Cuchara de Resurreccion (o un admin use /dedsafio revive). */
public final class Revival {
	private Revival() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer p) {
				DedsafioState st = Dedsafio.state(p.getServer());
				st.dead.add(p.getUUID());
				st.setDirty();
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			if (Dedsafio.state(newPlayer.getServer()).dead.contains(newPlayer.getUUID())) {
				newPlayer.setGameMode(GameType.SPECTATOR);
				newPlayer.sendSystemMessage(Component.translatable("message.dedsafio.you_died").withStyle(ChatFormatting.GRAY));
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer p = handler.player;
			if (Dedsafio.state(server).dead.contains(p.getUUID())) {
				p.setGameMode(GameType.SPECTATOR);
			}
		});
	}

	/** Primer jugador muerto (por orden de muerte) que este conectado. */
	public static ServerPlayer nextDeadOnline(MinecraftServer server) {
		for (UUID u : Dedsafio.state(server).dead) {
			ServerPlayer p = server.getPlayerList().getPlayer(u);
			if (p != null) return p;
		}
		return null;
	}

	/** Revive a target. Si reviver != null, lo teletransporta a su posicion. */
	public static void revive(ServerPlayer target, ServerPlayer reviver) {
		MinecraftServer server = target.getServer();
		DedsafioState st = Dedsafio.state(server);
		st.dead.remove(target.getUUID());
		st.setDirty();

		target.setGameMode(GameType.SURVIVAL);
		if (reviver != null) {
			target.teleportTo(reviver.serverLevel(), reviver.getX(), reviver.getY(), reviver.getZ(),
					reviver.getYRot(), reviver.getXRot());
		}
		target.setHealth(target.getMaxHealth());
		target.getFoodData().setFoodLevel(20);
		target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));

		target.serverLevel().playSound(null, target.blockPosition(), SoundEvents.TOTEM_USE,
				SoundSource.PLAYERS, 1.0f, 1.0f);
		server.getPlayerList().broadcastSystemMessage(
				Component.translatable("message.dedsafio.revived", target.getDisplayName())
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
	}
}
