package draconicevolution.common.items.weapons;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import draconicevolution.DraconicEvolution;
import draconicevolution.common.items.ModItems;
import draconicevolution.common.items.tools.ToolHandler;
import draconicevolution.common.lib.References;
import draconicevolution.common.lib.Strings;

public class WyvernSword extends ItemSword {
	public WyvernSword() {
		super(ModItems.DRACONIUM_T1);
		this.setUnlocalizedName(Strings.wyvernSwordName);
		this.setCreativeTab(DraconicEvolution.getCreativeTab(1));
		GameRegistry.registerItem(this, Strings.wyvernSwordName);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(final IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(References.RESOURCESPREFIX + "sword_wyvern");
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
	{
		ToolHandler.AOEAttack(player, entity, stack, 8, 1);
		ToolHandler.demageEntytyBasedOnHealth(entity, player, 0.2F);
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(final ItemStack stack, final EntityPlayer player, final List list, final boolean extraInformation)
	{
		list.add(EnumChatFormatting.DARK_RED + "Your enemy's strength will be their undoing");
		list.add("");
		list.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + "Weary of plain tools you begin to understand");
		list.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + "ways to use Draconic energy to upgrade");
	}

	@Override
	public EnumRarity getRarity(ItemStack stack)
	{
		return EnumRarity.uncommon;
	}

	public static void registerRecipe()
	{
		CraftingManager.getInstance().addRecipe(new ItemStack(ModItems.wyvernSword), " C ", "CSC", " C ", 'C', ModItems.infusedCompound, 'S', Items.diamond_sword);
	}

	@Override
	public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
	{
		System.out.println(par2EntityLivingBase.isEntityAlive());
		System.out.println(par3EntityLivingBase.isEntityAlive());
		return super.hitEntity(par1ItemStack, par2EntityLivingBase, par3EntityLivingBase);
	}
}