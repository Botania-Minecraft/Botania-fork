package vazkii.botania.forge.xplat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import vazkii.botania.xplat.IXplatAbstractions;

import java.util.function.BiFunction;

public class ForgeXplatImpl implements IXplatAbstractions {
	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLLoader.getDist() == Dist.CLIENT;
	}

	@Override
	public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BiFunction<BlockPos, BlockState, T> func, Block... blocks) {
		return BlockEntityType.Builder.of(func::apply, blocks).build(null);
	}

	@Override
	public void registerReloadListener(PackType type, ResourceLocation id, PreparableReloadListener listener) {
		switch (type) {
			case CLIENT_RESOURCES -> MinecraftForge.EVENT_BUS.addListener(
					(RegisterClientReloadListenersEvent e) -> e.registerReloadListener(listener));
			case SERVER_DATA -> MinecraftForge.EVENT_BUS.addListener(
					(AddReloadListenerEvent e) -> e.addListener(listener));
		}
	}
}