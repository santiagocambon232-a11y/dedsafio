package com.dedsafio;

import java.util.Locale;
import net.minecraft.network.chat.Component;

/** Reglas que puede sacar la ruleta. Se acumulan: cada giro suma una. */
public enum Rule {
	MOBS_FAST, MOBS_TOUGH, MOBS_STRONG,
	PLAYER_HUNGER, PLAYER_WEAK, PLAYER_SLOW, PLAYER_FATIGUE, LESS_HEARTS;

	public String id() { return name().toLowerCase(Locale.ROOT); }

	public Component text() { return Component.translatable("rule.dedsafio." + id()); }

	public static Rule byId(String id) {
		for (Rule r : values()) if (r.id().equals(id)) return r;
		return null;
	}
}
