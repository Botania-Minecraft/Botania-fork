/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 21, 2014, 7:51:36 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.item.IPetalApothecary;
import vazkii.botania.api.recipe.IFlowerComponent;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.common.Botania;
import vazkii.botania.common.lib.LibBlockNames;

public class TileAltar extends TileSimpleInventory implements ISidedInventory, IPetalApothecary {

	private static final Pattern SEED_PATTERN = Pattern.compile("(?:(?:(?:[A-Z-_.:]|^)seed)|(?:(?:[a-z-_.:]|^)Seed))(?:[sA-Z-_.:]|$)");

	public static final String TAG_HAS_WATER = "hasWater";
	public static final String TAG_HAS_LAVA = "hasLava";
	public static final String TAG_IS_MOSSY = "isMossy";

	public boolean hasWater = false;
	public boolean hasLava = false;

	public boolean isMossy = false;

	List<ItemStack> lastRecipe = null;
	int recipeKeepTicks = 0;

	public boolean collideEntityItem(EntityItem item) {
		ItemStack stack = item.getEntityItem();
		if(stack == null || item.isDead)
			return false;

		if(!isMossy && getBlockMetadata() == 0) {
			if(stack.getItem() == Item.getItemFromBlock(Blocks.vine) && !worldObj.isRemote) {
				isMossy = true;
				worldObj.func_147453_f(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
				stack.stackSize--;
				if(stack.stackSize == 0)
					item.setDead();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, , xCoord);
			}
		}

		if(!hasWater() && !hasLava()) {
			if(stack.getItem() == Items.water_bucket && !worldObj.isRemote) {
				setWater(true);
				worldObj.func_147453_f(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
				stack.func_150996_a(Items.bucket); // Set item
			} else if(stack.getItem() == Items.lava_bucket && !worldObj.isRemote) {
				setLava(true);
				worldObj.func_147453_f(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
				stack.func_150996_a(Items.bucket); // Set item
			} else return false;
		}

		if(hasLava()) {
			item.setFire(100);
			return true;
		}

		boolean didChange = false;

		if(stack.getItem() instanceof IFlowerComponent) {
			if(getStackInSlot(getSizeInventory() - 1) != null)
				return false;

			if(!worldObj.isRemote) {
				stack.stackSize--;
				if(stack.stackSize == 0)
					item.setDead();

				for(int i = 0; i < getSizeInventory(); i++)
					if(getStackInSlot(i) == null) {
						ItemStack stackToPut = stack.copy();
						stackToPut.stackSize = 1;
						setInventorySlotContents(i, stackToPut);
						didChange = true;
						worldObj.playSoundAtEntity(item, "game.neutral.swim.splash", 0.1F, 1F);
						break;
					}
			}
		} else if(stack.getItem() != null && SEED_PATTERN.matcher(stack.getItem().getUnlocalizedName(stack)).find()) {
			for(RecipePetals recipe : BotaniaAPI.petalRecipes) {
				if(recipe.matches(this)) {
					saveLastRecipe();

					if(!worldObj.isRemote) {
						for(int i = 0; i < getSizeInventory(); i++)
							setInventorySlotContents(i, null);

						stack.stackSize--;
						if(stack.stackSize == 0)
							item.setDead();

						ItemStack output = recipe.getOutput().copy();
						EntityItem outputItem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, output);
						worldObj.spawnEntityInWorld(outputItem);

						setWater(false);
						worldObj.func_147453_f(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
					}

					craftingFanciness();
					didChange = true;

					break;
				}
			}
		}

		return didChange;
	}

	public void saveLastRecipe() {
		lastRecipe = new ArrayList();
		for(int i = 0; i < getSizeInventory(); i++) {
			ItemStack stack = getStackInSlot(i);
			if(stack == null)
				break;
			lastRecipe.add(stack);
		}
		recipeKeepTicks = 400;
	}

	public void trySetLastRecipe(EntityPlayer player) {
		tryToSetLastRecipe(player, this, lastRecipe);
		if(!isEmpty())
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, , xCoord);
	}

	public static void tryToSetLastRecipe(EntityPlayer player, IInventory inv, List<ItemStack> lastRecipe) {
		if(lastRecipe == null || lastRecipe.isEmpty() || player.worldObj.isRemote)
			return;

		int index = 0;
		boolean didAny = false;
		for(ItemStack stack : lastRecipe) {
			if(stack == null)
				continue;

			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack pstack = player.inventory.getStackInSlot(i);
				if(pstack != null && pstack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, pstack)) {
					pstack.stackSize--;
					if(pstack.stackSize == 0)
						player.inventory.setInventorySlotContents(i, null);

					ItemStack stackToPut = pstack.copy();
					stackToPut.stackSize = 1;
					inv.setInventorySlotContents(index, stackToPut);
					didAny = true;
					index++;
					break;
				}
			}
		}

		if(didAny) {
			if(inv instanceof TileAltar)
				player.worldObj.playSoundAtEntity(player, "game.neutral.swim.splash", 0.1F, 1F);
			if(player instanceof EntityPlayerMP) {
				EntityPlayerMP mp = (EntityPlayerMP) player;
				mp.inventoryContainer.detectAndSendChanges();
			}
		}
	}

	public void craftingFanciness() {
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, "botania:altarCraft", 1F, 1F);
		for(int i = 0; i < 25; i++) {
			float red = (float) Math.random();
			float green = (float) Math.random();
			float blue = (float) Math.random();
			Botania.proxy.sparkleFX(worldObj, xCoord + 0.5 + Math.random() * 0.4 - 0.2, yCoord + 1, zCoord + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
		}
	}

	public boolean isEmpty() {
		for(int i = 0; i < getSizeInventory(); i++)
			if(getStackInSlot(i) != null)
				return false;

		return true;
	}

	@Override
	public void updateEntity() {
		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(xCoord, yCoord + 1D / 16D * 20D, zCoord, xCoord + 1, yCoord + 1D / 16D * 21D, zCoord + 1));

		boolean didChange = false;

		for(EntityItem item : items)
			didChange = collideEntityItem(item) || didChange;

		if(didChange)
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, , xCoord);

		for(int i = 0; i < getSizeInventory(); i++) {
			ItemStack stackAt = getStackInSlot(i);
			if(stackAt == null)
				break;

			if(Math.random() >= 0.97) {
				Color color = new Color(((IFlowerComponent) stackAt.getItem()).getParticleColor(stackAt));
				float red = color.getRed() / 255F;
				float green = color.getGreen() / 255F;
				float blue = color.getBlue() / 255F;
				if(Math.random() >= 0.75F)
					worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "game.neutral.swim.splash", 0.1F, 10F);
				Botania.proxy.sparkleFX(worldObj, xCoord + 0.5 + Math.random() * 0.4 - 0.2, yCoord + 1, zCoord + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
			}
		}

		if(hasLava()) {
			isMossy = false;
			worldObj.spawnParticle("smoke", xCoord + 0.5 + Math.random() * 0.4 - 0.2, yCoord + 1, zCoord + 0.5 + Math.random() * 0.4 - 0.2, 0, 0.05, 0);
			if(Math.random() > 0.9)
				worldObj.spawnParticle("lava", xCoord + 0.5 + Math.random() * 0.4 - 0.2, yCoord + 1, zCoord + 0.5 + Math.random() * 0.4 - 0.2, 0, 0.01, 0);
		}

		if(recipeKeepTicks > 0)
			--recipeKeepTicks;
		else lastRecipe = null;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		super.writeCustomNBT(cmp);

		cmp.setBoolean(TAG_HAS_WATER, hasWater());
		cmp.setBoolean(TAG_HAS_LAVA, hasLava());
		cmp.setBoolean(TAG_IS_MOSSY, isMossy);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		super.readCustomNBT(cmp);

		hasWater = cmp.getBoolean(TAG_HAS_WATER);
		hasLava = cmp.getBoolean(TAG_HAS_LAVA);
		isMossy = cmp.getBoolean(TAG_IS_MOSSY);
	}

	@Override
	public String getInventoryName() {
		return LibBlockNames.ALTAR;
	}

	@Override
	public int getSizeInventory() {
		return 16;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return false;
	}

	@Override
	public void setWater(boolean water) {
		hasWater = water;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, , xCoord);
	}

	public void setLava(boolean lava) {
		hasLava = lava;
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldObj, , xCoord);
	}

	@Override
	public boolean hasWater() {
		return hasWater;
	}

	public boolean hasLava() {
		return hasLava;
	}

}
