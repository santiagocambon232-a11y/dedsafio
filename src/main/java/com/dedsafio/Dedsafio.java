package com.dedsafio;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dedsafio implements ModInitializer {
	public static final String MOD_ID = "dedsafio";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		Revival.register();
		Roulette.register();
		DedsafioCommands.register();
		LOGGER.info("Dedsafio cargado");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static DedsafioState state(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(DedsafioState.FACTORY, MOD_ID);
	}
}
