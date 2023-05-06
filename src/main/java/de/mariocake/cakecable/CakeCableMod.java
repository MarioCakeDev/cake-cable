package de.mariocake.cakecable;

import de.mariocake.cakecable.common.CableBlock;
import de.mariocake.cakecable.common.CableEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class CakeCableMod implements ModInitializer {
	private static final String modId = "cakecable";

	public static Identifier Identifier(String id) {
		return new Identifier(modId, id);
	}

	public static final CableBlock CABLE_BLOCK = new CableBlock(QuiltBlockSettings.of(Material.GLASS).hardness(0.3f));
	public static final BlockEntityType<CableEntity> CABLE_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			CakeCableMod.Identifier("cable_entity"),
			QuiltBlockEntityTypeBuilder.create(CableEntity::new, CABLE_BLOCK).build()
	);

	private static final ItemGroup CAKECABLE_ITEMGROUP = FabricItemGroup.builder(CakeCableMod.Identifier("general"))
			.icon(() -> new ItemStack(CABLE_BLOCK))
			.build();

	@Override
	public void onInitialize(ModContainer mod) {
		BlockItem BLOCK_ITEM = new BlockItem(CABLE_BLOCK, new QuiltItemSettings().maxCount(64));

		ItemGroupEvents.modifyEntriesEvent(CAKECABLE_ITEMGROUP).register(content -> {
			content.addItem(BLOCK_ITEM);
		});

		Registry.register(Registries.BLOCK, CableBlock.IDENTIFIER, CABLE_BLOCK);
		Registry.register(Registries.ITEM, CableBlock.IDENTIFIER, BLOCK_ITEM);

	}
}
