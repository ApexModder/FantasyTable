package xyz.apex.forge.fantasytable.init;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import xyz.apex.forge.fantasytable.FantasyTable;

public final class FBlocks
{
	// @formatter:off
	public static final BlockEntry<Block> GAMBLING_TABLE = FantasyTable
			.registrate()
			.object(FStrings.BLOCK_GAMBLING_TABLE)
			.block(Block::new)

			.initialProperties(Material.WOOD, MaterialColor.COLOR_BROWN)
			.properties(properties -> properties.strength(2.5F).sound(SoundType.WOOD))
			.blockstate((ctx, provider) ->
					provider.simpleBlock(ctx.get(),
							provider.models()
							        .cubeBottomTop(
											ctx.getName(),
									        new ResourceLocation(FantasyTable.ID, "block/gambling_table/side"),
									        new ResourceLocation(FantasyTable.ID, "block/gambling_table/bottom"),
									        new ResourceLocation(FantasyTable.ID, "block/gambling_table/top")
							        )
					)
			)

			.item()
			.model((ctx, provider) -> provider.blockItem(ctx))
			.build()

			.lang("Gambling Table")

			.register();
	// @formatter:on

	@Deprecated // internal use only
	public static void register() { }
}