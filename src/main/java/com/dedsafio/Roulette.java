package com.dedsafio;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

/** La Ruleta: cada cierto tiempo activa una regla nueva que endurece el juego. Las reglas se acumulan. */
public final class Roulette {
	private Roulette() {}

	private static final ResourceLocation FAST = Dedsafio.id("roulette_fast");
	private static final ResourceLocation TOUGH = Dedsafio.id("roulette_tough");
	private static final ResourceLocation STRONG = Dedsafio.id("roulette_strong");
	private static final ResourceLocation HEARTS = Dedsafio.id("roulette_hearts");

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Roulette::tick);
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof Monster mob) {
				applyToMob(mob, Dedsafio.state(world.getServer()));
			}
		});
	}

	private static void tick(MinecraftServer server) {
		DedsafioState st = Dedsafio.state(server);
		if (--st.ticksLeft <= 0) spin(server, st);
		int t = server.getTickCount();
		if (t % 100 == 0) applyPlayerRules(server, st);
		if (t % 1200 == 0) st.setDirty();
	}

	/** Gira la ruleta. Devuelve false si ya estan todas las reglas activas. */
	public static boolean spin(MinecraftServer server, DedsafioState st) {
		st.ticksLeft = st.interval;
		List<Rule> pool = new ArrayList<>(List.of(Rule.values()));
		pool.removeAll(st.rules);
		st.setDirty();
		if (pool.isEmpty()) return false;

		st.day++;
		Rule picked = pool.get(server.overworld().getRandom().nextInt(pool.size()));
		st.rules.add(picked);

		server.getPlayerList().broadcastSystemMessage(
				Component.translatable("message.dedsafio.spin", st.day, picked.text())
						.withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
					SoundSource.MASTER, 0.6f, 1.0f);
		}
		applyPlayerRules(server, st);
		return true;
	}

	public static void reset(MinecraftServer server, DedsafioState st) {
		st.rules.clear();
		st.day = 0;
		st.ticksLeft = st.interval;
		st.setDirty();
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
			if (inst != null) inst.removeModifier(HEARTS);
		}
	}

	// ---- Reglas de mobs (solo mobs que aparecen/cargan despues de activarse) ----
	static void applyToMob(Monster mob, DedsafioState st) {
		if (st.rules.contains(Rule.MOBS_FAST))
			addModifier(mob, Attributes.MOVEMENT_SPEED, FAST, 0.3, false);
		if (st.rules.contains(Rule.MOBS_TOUGH))
			addModifier(mob, Attributes.MAX_HEALTH, TOUGH, 1.0, true);
		if (st.rules.contains(Rule.MOBS_STRONG))
			addModifier(mob, Attributes.ATTACK_DAMAGE, STRONG, 0.5, false);
	}

	// ---- Reglas de jugadores (se refrescan cada 5 segundos) ----
	static void applyPlayerRules(MinecraftServer server, DedsafioState st) {
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			if (p.isSpectator() || p.isCreative()) continue;
			if (st.rules.contains(Rule.PLAYER_HUNGER)) effect(p, MobEffects.HUNGER);
			if (st.rules.contains(Rule.PLAYER_WEAK)) effect(p, MobEffects.WEAKNESS);
			if (st.rules.contains(Rule.PLAYER_SLOW)) effect(p, MobEffects.MOVEMENT_SLOWDOWN);
			if (st.rules.contains(Rule.PLAYER_FATIGUE)) effect(p, MobEffects.DIG_SLOWDOWN);
			if (st.rules.contains(Rule.LESS_HEARTS)) {
				addModifier(p, Attributes.MAX_HEALTH, HEARTS, -0.4, false); // -4 corazones
				if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
			}
		}
	}

	private static void effect(ServerPlayer p, Holder<MobEffect> e) {
		p.addEffect(new MobEffectInstance(e, 220, 0, true, false, true));
	}

	private static void addModifier(LivingEntity e, Holder<Attribute> attr,
	                                ResourceLocation id, double value, boolean heal) {
		AttributeInstance inst = e.getAttribute(attr);
		if (inst == null || inst.getModifier(id) != null) return;
		inst.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
		if (heal) e.setHealth(e.getMaxHealth());
	}
}
