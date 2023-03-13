package de.mariocake.cakecable;

import de.mariocake.cakecable.common.CableBlock;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CakeCableMod implements ModInitializer {
	private static final String modId = "cakecable";

	public static Identifier Identifier(String id) {
		return new Identifier(modId, id);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger("CakeCableMod");

	public static final CableBlock CABLE_BLOCK = new CableBlock(QuiltBlockSettings.of(Material.STONE).hardness(1.0f).strength(1.0f));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.BLOCK, CableBlock.IDENTIFIER, CABLE_BLOCK);
		Registry.register(Registries.ITEM, CableBlock.IDENTIFIER, new BlockItem(CABLE_BLOCK, new QuiltItemSettings().rarity(Rarity.RARE)));
	}
}
