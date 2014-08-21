/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Aug 21, 2014, 5:24:55 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.lib.LibItemNames;

public class ItemSpark extends ItemMod {

	public static IIcon invIcon, worldIcon;
	
	public ItemSpark() {
		setUnlocalizedName(LibItemNames.SPARK);
	}
	
	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		invIcon = IconHelper.forItem(par1IconRegister, this, 0);
		worldIcon = IconHelper.forItem(par1IconRegister, this, 1);
	}
	
	@Override
	public IIcon getIconFromDamage(int p_77617_1_) {
		return invIcon;
	}
	
}
