/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Nov 14, 2014, 5:04:22 PM (GMT)]
 */
package vazkii.botania.common.block.tile.string;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.ITileBound;
import vazkii.botania.common.block.tile.TileMod;

import javax.annotation.Nonnull;

public abstract class TileRedString extends TileMod implements ITileBound, ITickableTileEntity {

	private BlockPos binding;

	public TileRedString(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void tick() {
		Direction dir = getOrientation();
		BlockPos pos_ = getPos();
		int range = getRange();
		BlockPos currBinding = getBinding();
		setBinding(null);

		for(int i = 0; i < range; i++) {
			pos_ = pos_.offset(dir);
			if(world.isAirBlock(pos_))
				continue;

			TileEntity tile = world.getTileEntity(pos_);
			if(tile instanceof TileRedString)
				continue;

			if(acceptBlock(pos_)) {
				setBinding(pos_);
				if(currBinding == null || !currBinding.equals(pos_))
					onBound(pos_);
				break;
			}
		}
	}

	public int getRange() {
		return 8;
	}

	public abstract boolean acceptBlock(BlockPos pos);

	public void onBound(BlockPos pos) {}

	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	public BlockPos getBinding() {
		return binding;
	}

	public void setBinding(BlockPos binding) {
		this.binding = binding;
	}

	public Direction getOrientation() {
		BlockState state = world.getBlockState(getPos());
		if(state.getProperties().contains(BotaniaStateProps.FACING))
			return state.get(BotaniaStateProps.FACING);
		
		return Direction.WEST; // fallback
	}

	public TileEntity getTileAtBinding() {
		BlockPos binding = getBinding();
		return binding == null ? null : world.getTileEntity(binding);
	}

	public BlockState getStateAtBinding() {
		BlockPos binding = getBinding();
		return binding == null ? Blocks.AIR.getDefaultState() : world.getBlockState(binding);
	}

	public Block getBlockAtBinding() {
		return getStateAtBinding().getBlock();
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

}
