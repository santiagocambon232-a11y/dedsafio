package com.dedsafio;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class ModItems {
	private ModItems() {}

	public static Item RESURRECTION_SPOON;

	public static void register() {
		RESURRECTION_SPOON = Registry.register(BuiltInRegistries.ITEM, Dedsafio.id("resurrection_spoon"),
				new ResurrectionSpoonItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register(entries -> entries.accept(RESURRECTION_SPOON));
	}
}
