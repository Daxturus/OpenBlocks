package openblocks.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.Stencil;
import openblocks.common.tileentity.TileEntityCanvas;

public class BlockCanvas extends OpenBlock {

	private int layer = 0;
	private int renderSide = 0;
	public Icon baseIcon;

	public BlockCanvas() {
		super(Config.blockCanvasId, Material.ground);
		setupBlock(this, "canvas", TileEntityCanvas.class);
	}

	@Override
	public void registerIcons(IconRegister registry) {
		baseIcon = registry.registerIcon("openblocks:canvas");
		for (Stencil stencil : Stencil.values()) {
			stencil.registerBlockIcons(registry);
		}
		super.registerIcons(registry);
	}

	@Override
	public boolean shouldRenderBlock() {
		return true;
	}

	public void setLayerForRender(int layer) {
		this.layer = layer;
	}

	public void setSideForRender(int side) {
		this.renderSide = side;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return side == renderSide
				&& super.shouldSideBeRendered(world, x, y, z, side);
	}

	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		TileEntityCanvas tile = this.getTileEntity(world, x, y, z, TileEntityCanvas.class);
		if (tile != null) { return tile.getColorForRender(renderSide, layer); }
		return 16777215;
	}

	@Override
	public Icon getUnrotatedTexture(ForgeDirection direction, IBlockAccess world, int x, int y, int z) {
		TileEntityCanvas tile = this.getTileEntity(world, x, y, z, TileEntityCanvas.class);
		if (tile != null) { return tile.getTextureForRender(renderSide, layer); }
		return super.getUnrotatedTexture(direction, world, x, y, z);

	}

	public static void replaceBlock(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y,z);
		int meta = world.getBlockMetadata(x, y, z);
		world.setBlock(x, y, z, OpenBlocks.Blocks.canvas.blockID);
		TileEntityCanvas tile = (TileEntityCanvas)world.getBlockTileEntity(x, y, z);
		tile.paintedBlockId.setValue(id);
		tile.paintedBlockMeta.setValue(meta);
	}
}