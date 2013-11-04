package openblocks.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import openblocks.OpenBlocks;
import openblocks.common.block.OpenBlock;
import openblocks.utils.BlockUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class OpenTileEntity extends TileEntity {

	private boolean initialized = false;
	private boolean isActive = false;

	private boolean isUsedForClientInventoryRendering = false;

	
	/**
	 * The block rotation stored in metadata. This can be used for
	 * NORTH/EAST/SOUTH/WEST
	 */
	private ForgeDirection rotation = ForgeDirection.UNKNOWN;

	/**
	 * A random flag that can be stored in metadata
	 */
	private boolean flag1 = false;

	/**
	 * A random flag that can be stored in metadata
	 */
	private boolean flag2 = false;

	/**
	 * Get the current block rotation
	 * 
	 * @return the block rotation
	 */
	public ForgeDirection getRotation() {
		if (isUsedForClientInventoryRendering) { return rotation; }
		return OpenBlock.getRotation(worldObj, xCoord, yCoord, zCoord);
	}

	/**
	 * @param block
	 * @param metadata
	 */
	@SideOnly(Side.CLIENT)
	public void prepareForInventoryRender(Block block, int metadata) {
		isUsedForClientInventoryRendering = true;
	}

	/**
	 * Set the block rotation. To sync to the client call sync()
	 * This supports all 6 directions provided the Block rotation mode is set as so.
	 * 
	 * @param rot
	 */
	public void setRotation(ForgeDirection rot) {
		if (rot == ForgeDirection.UNKNOWN) {
			rot = BlockUtils.DEFAULT_BLOCK_DIRECTION;
		}
		rotation = rot;
	}

	private boolean getFlag(int index) {
		return OpenBlock.getFlag(worldObj, xCoord, yCoord, zCoord, index);
	}

	public boolean getFlag1() {
		if (isUsedForClientInventoryRendering) { return flag1; }
		return getFlag(0);
	}

	public boolean getFlag2() {
		if (isUsedForClientInventoryRendering) { return flag2; }
		return getFlag(1);
	}

	public void setFlag1(boolean on) {
		flag1 = on;
	}

	public void setFlag2(boolean on) {
		flag2 = on;
	}

	@Override
	public void updateEntity() {
		isActive = true;
		if (!initialized) {
			rotation = getRotation();
			flag1 = getFlag1();
			flag2 = getFlag2();
			initialize();
			initialized = true;
		}
	}

	public boolean isLoaded() {
		return initialized;
	}

	public boolean isAddedToWorld() {
		return worldObj != null;
	}

	protected void initialize() {
		flag1 = getFlag1();
		flag2 = getFlag2();
		rotation = getRotation();
	}

	protected boolean isActive() {
		return isActive;
	}

	@Override
	public void onChunkUnload() {
		isActive = false;
	}

	public TileEntity getTileInDirection(ForgeDirection direction) {
		int x = xCoord + direction.offsetX;
		int y = yCoord + direction.offsetY;
		int z = zCoord + direction.offsetZ;
		/* TODO: Mikee, getBlockTileEntity returns null anyway, why the extra block check ? */
		if (worldObj != null && worldObj.blockExists(x, y, z)) { return worldObj.getBlockTileEntity(x, y, z); }
		return null;
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s", xCoord, yCoord, zCoord);
	}

	public boolean isAirBlock(ForgeDirection direction) {
		return worldObj != null
				&& worldObj.isAirBlock(xCoord + direction.offsetX, yCoord
						+ direction.offsetY, zCoord + direction.offsetZ);
	}

	public void sendBlockEvent(int key, int value) {
		worldObj.addBlockEvent(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord), key, value);
	}

	public void sync() {
		OpenBlock block = getBlock();
		if (block != null) {
			switch(block.getBlockRotationMode()) {
				case NONE: 
					OpenBlock.setFlag(worldObj, xCoord, yCoord, zCoord, 0, flag1, false);
					OpenBlock.setFlag(worldObj, xCoord, yCoord, zCoord, 1, flag2, true);
					break;
				case FOUR_DIRECTIONS: 
					OpenBlock.setFlag(worldObj, xCoord, yCoord, zCoord, 0, flag1, false);
					OpenBlock.setFlag(worldObj, xCoord, yCoord, zCoord, 1, flag2, false);
					OpenBlock.setRotation(worldObj, xCoord, yCoord, zCoord, rotation);
					break;
				case SIX_DIRECTIONS:
					OpenBlock.setRotation(worldObj, xCoord, yCoord, zCoord, rotation);
			}
		}
		/* TODO: I don't think this is required. We should look in to that */
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean shouldRefresh(int oldID, int newID, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldID != newID;
	}

	public OpenBlock getBlock() {
		return OpenBlock.getOpenBlock(worldObj, xCoord, yCoord, zCoord);
	}

	public int getMetadata() {
		return worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}

	public void openGui(EntityPlayer player) {
		player.openGui(OpenBlocks.instance, -1, worldObj, xCoord, yCoord, zCoord);
	}

	public AxisAlignedBB getBB() {
		return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	public boolean isRenderedInInventory() {
		return isUsedForClientInventoryRendering;
	}

}
