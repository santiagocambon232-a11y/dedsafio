package com.dedsafio;

import java.util.LinkedHashSet;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

/** Datos guardados en el mundo: jugadores muertos, reglas activas y temporizador de la ruleta. */
public class DedsafioState extends SavedData {
	// El 3er parametro (DataFixTypes) puede ir null en mods.
	public static final SavedData.Factory<DedsafioState> FACTORY =
			new SavedData.Factory<>(DedsafioState::new, DedsafioState::fromTag, null);

	public final LinkedHashSet<UUID> dead = new LinkedHashSet<>();   // orden de muerte
	public final LinkedHashSet<Rule> rules = new LinkedHashSet<>();  // reglas activas
	public int interval = 24000;  // ticks entre giros (24000 = 1 dia de Minecraft)
	public int ticksLeft = 24000;
	public int day = 0;

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag d = new ListTag();
		for (UUID u : dead) d.add(StringTag.valueOf(u.toString()));
		tag.put("dead", d);
		ListTag r = new ListTag();
		for (Rule rule : rules) r.add(StringTag.valueOf(rule.id()));
		tag.put("rules", r);
		tag.putInt("interval", interval);
		tag.putInt("ticksLeft", ticksLeft);
		tag.putInt("day", day);
		return tag;
	}

	public static DedsafioState fromTag(CompoundTag tag, HolderLookup.Provider registries) {
		DedsafioState s = new DedsafioState();
		ListTag d = tag.getList("dead", Tag.TAG_STRING);
		for (int i = 0; i < d.size(); i++) {
			try { s.dead.add(UUID.fromString(d.getString(i))); } catch (IllegalArgumentException ignored) {}
		}
		ListTag r = tag.getList("rules", Tag.TAG_STRING);
		for (int i = 0; i < r.size(); i++) {
			Rule rule = Rule.byId(r.getString(i));
			if (rule != null) s.rules.add(rule);
		}
		if (tag.contains("interval")) s.interval = tag.getInt("interval");
		if (tag.contains("ticksLeft")) s.ticksLeft = tag.getInt("ticksLeft");
		s.day = tag.getInt("day");
		return s;
	}
}
