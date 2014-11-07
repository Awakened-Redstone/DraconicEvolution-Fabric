package com.brandon3055.draconicevolution.common.core.handler;


import com.brandon3055.draconicevolution.common.blocks.ModBlocks;
import com.brandon3055.draconicevolution.common.core.utills.ItemNBTHelper;
import com.brandon3055.draconicevolution.common.core.utills.LogHelper;
import com.brandon3055.draconicevolution.common.entity.EntityCustomDragon;
import com.brandon3055.draconicevolution.common.entity.ExtendedPlayer;
import com.brandon3055.draconicevolution.common.items.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinecraftForgeEventHandler {

	Random random = new Random();
	public static List<String> playersWithUphillStep = new ArrayList<String>();
	public static List<String> playersWithFlight = new ArrayList<String>();

	@SubscribeEvent
	public void onEntityUpdate(LivingUpdateEvent event){
		EntityLivingBase entityLiving = event.entityLiving;

		if (entityLiving.worldObj.isRemote && entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;
			boolean hasUphillStep = playersWithUphillStep.contains(player.getDisplayName());
			boolean shouldHaveUphillStep = player.getEquipmentInSlot(1) != null && player.getEquipmentInSlot(1).getItem() == ModItems.draconicBoots && !player.isSneaking();

			if (!hasUphillStep && shouldHaveUphillStep)
			{
				playersWithUphillStep.add(player.getDisplayName());
				player.stepHeight = 1;
			}
			if (hasUphillStep && !shouldHaveUphillStep)
			{
				playersWithUphillStep.remove(player.getDisplayName());
				player.stepHeight = 0.5F;
			}
		}

		if(entityLiving instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) entityLiving;
			boolean hasFlight = playersWithFlight.contains(player.getDisplayName());
			boolean shouldHaveFlight = player.getEquipmentInSlot(3) != null && player.getEquipmentInSlot(3).getItem() == ModItems.draconicChest;

			if ((!hasFlight || !player.capabilities.allowFlying) && shouldHaveFlight)
			{
				if (!playersWithFlight.contains(player.getDisplayName()) && !player.worldObj.isRemote)
					playersWithFlight.add(player.getDisplayName());
				player.capabilities.allowFlying = true;
			}
			if (player.worldObj.isRemote && hasFlight && player.capabilities.getFlySpeed() <= 0.05F) player.capabilities.setFlySpeed(player.capabilities.getFlySpeed() + 0.05F);
			//if (!player.worldObj.isRemote) LogHelper.info(hasFlight);
			if (hasFlight && !shouldHaveFlight)
			{
				if (player.worldObj.isRemote && player.capabilities.getFlySpeed() > 0.05F) player.capabilities.setFlySpeed(0.05F);
				if (!player.worldObj.isRemote)playersWithFlight.remove(player.getDisplayName());
				player.capabilities.allowFlying = false;
				player.capabilities.isFlying = false;
				if (player.capabilities.isCreativeMode)
					player.capabilities.allowFlying = true;
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onDropEvent(LivingDropsEvent event){
		if (event.entity instanceof EntityDragon && !event.entity.worldObj.isRemote){
			EntityItem item = new EntityItem(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, new ItemStack(ModItems.dragonHeart));
			event.entity.worldObj.spawnEntityInWorld(item);
			if (event.entity instanceof EntityCustomDragon && ((EntityCustomDragon)event.entity).getIsUber()) event.entity.worldObj.spawnEntityInWorld(item);

			int count = 30 + event.entity.worldObj.rand.nextInt(30);
			for (int i = 0; i < count; i++) {
				float mm = 0.3F;
				EntityItem item2 = new EntityItem(event.entity.worldObj, event.entity.posX-2+event.entity.worldObj.rand.nextInt(4), event.entity.posY-2+event.entity.worldObj.rand.nextInt(4), event.entity.posZ-2+event.entity.worldObj.rand.nextInt(4), new ItemStack(ModItems.draconiumDust));
				item.motionX = mm * ((((float) event.entity.worldObj.rand.nextInt(100)) / 100F) - 0.5F);
				item.motionY = mm * ((((float) event.entity.worldObj.rand.nextInt(100)) / 100F) - 0.5F);
				item.motionZ = mm * ((((float) event.entity.worldObj.rand.nextInt(100)) / 100F) - 0.5F);
				event.entity.worldObj.spawnEntityInWorld(item2);
			}
		}

		if (event.entity.worldObj.isRemote || !(event.source.damageType.equals("player") || event.source.damageType.equals("arrow")) || !isValidEntity(event.entityLiving)){ return; }
		EntityLivingBase entity = event.entityLiving;
		Entity attacker = event.source.getEntity();
		if (attacker == null || !(attacker instanceof EntityPlayer)) { return; }
		if (((EntityPlayer) attacker).getHeldItem() == null || !(((EntityPlayer) attacker).getHeldItem().getItem().equals(ModItems.draconicSword) || ((EntityPlayer) attacker).getHeldItem().getItem().equals(ModItems.draconicDistructionStaff) || ((EntityPlayer) attacker).getHeldItem().getItem().equals(ModItems.draconicBow) || ((EntityPlayer) attacker).getHeldItem().getItem().equals(ModItems.wyvernBow) || ((EntityPlayer) attacker).getHeldItem().getItem().equals(ModItems.wyvernSword))) { return; }
		World world = entity.worldObj;
		int rand = random.nextInt(ConfigHandler.soulDropChance);
		int rand2 = random.nextInt(ConfigHandler.passiveSoulDropChance);
		boolean isAnimal = entity instanceof EntityAnimal;
		if ((rand == 0 && !isAnimal) || (rand2 == 0 && isAnimal)) {
			ItemStack soul = new ItemStack(ModItems.mobSoul);
			String name = EntityList.getEntityString(entity);
			ItemNBTHelper.setString(soul, "Name", name);
			world.spawnEntityInWorld(new EntityItem(world, entity.posX, entity.posY, entity.posZ, soul));
		}
	}

	private boolean isValidEntity(EntityLivingBase entity){
		if (entity instanceof IBossDisplayData) { return false; }
		for (int i = 0; i < ConfigHandler.spawnerList.length; i++)
		{
			if (ConfigHandler.spawnerList[i].equals(entity.getCommandSenderName()) && ConfigHandler.spawnerListType)
			{
				return true;
			} else if (ConfigHandler.spawnerList[i].equals(entity.getCommandSenderName()) && !ConfigHandler.spawnerListType)
			{
				return false;
			}
		}
		if (ConfigHandler.spawnerListType) {
			return false;
		}else {
			return true;
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void ChunkEvent(ChunkEvent event) {
		if (!ConfigHandler.updateFix) return;
		Chunk chunk = event.getChunk();
		for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
			if (storage != null) {
				for (int x = 0; x < 16; ++x) {
					for (int y = 0; y < 16; ++y) {
						for (int z = 0; z < 16; ++z) {
							if (changeBlock(storage, x, y, z)) chunk.isModified = true;
						}
					}
				}
			}
		}
	}

	private boolean changeBlock(ExtendedBlockStorage storage, int x, int y, int z){

		if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.particleGenerator")) return makeChange(storage, x, y, z, ModBlocks.particleGenerator);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.customSpawner")) return makeChange(storage, x, y, z, ModBlocks.customSpawner);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.generator")) return makeChange(storage, x, y, z, ModBlocks.generator);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.playerDetectorAdvanced")) return makeChange(storage, x, y, z, ModBlocks.playerDetectorAdvanced);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.energyInfuser")) return makeChange(storage, x, y, z, ModBlocks.energyInfuser);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.draconiumOre")) return makeChange(storage, x, y, z, ModBlocks.draconiumOre);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.weatherController")) return makeChange(storage, x, y, z, ModBlocks.weatherController);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.longRangeDislocator")) return makeChange(storage, x, y, z, ModBlocks.longRangeDislocator);
		else if (storage.getBlockByExtId(x, y, z).getUnlocalizedName().equals("tile.tile.grinder")) return makeChange(storage, x, y, z, ModBlocks.grinder);
		return false;
	}

	private boolean makeChange(ExtendedBlockStorage storage, int x, int y, int z, Block block){
		if (block == null) return false;
		LogHelper.info("Changing block at [X:"+x+" Y:"+y+" Z:"+z+"] from "+storage.getBlockByExtId(x, y, z).getUnlocalizedName()+" to "+block.getUnlocalizedName());
		storage.func_150818_a(x, y, z, block);
		return true;
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer) event.entity) == null) ExtendedPlayer.register((EntityPlayer) event.entity);
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void itemTooltipEvent(ItemTooltipEvent event){
		if (ConfigHandler.showUnlocalizedNames) event.toolTip.add(event.itemStack.getUnlocalizedName());
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void stopUsingEvent(PlayerUseItemEvent.Start event){
		if (!ConfigHandler.pigmenBloodRage || event.item == null || event.item.getItem() == null) return;
		if (event.item.getItem() == Items.porkchop || event.item.getItem() == Items.cooked_porkchop){
			World world = event.entityPlayer.worldObj;
			if (world.isRemote) return;
			EntityPlayer player = event.entityPlayer;
			List list = world.getEntitiesWithinAABB(EntityPigZombie.class, AxisAlignedBB.getBoundingBox(player.posX - 32, player.posY - 32, player.posZ - 32, player.posX + 32, player.posY + 32, player.posZ + 32));

			EntityZombie entityAtPlayer = new EntityPigZombie(world);
			entityAtPlayer.setPosition(player.posX, player.posY, player.posZ);

			boolean flag = false;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity)list.get(i);

				if (entity1 instanceof EntityPigZombie)
				{
					EntityPigZombie entitypigzombie = (EntityPigZombie)entity1;
					NBTTagCompound compound = new NBTTagCompound();
					entitypigzombie.writeEntityToNBT(compound);
					compound.setShort("Anger", (short)1000);
					entitypigzombie.readEntityFromNBT(compound);
					if (Math.abs(entitypigzombie.posX - player.posX) < 14 && Math.abs(entitypigzombie.posY - player.posY) < 14 && Math.abs(entitypigzombie.posZ - player.posZ) < 14) flag = true;
					entitypigzombie.addPotionEffect(new PotionEffect(5, 10000, 3));
					entitypigzombie.addPotionEffect(new PotionEffect(11, 10000, 2));
				}
			}

			if (flag) player.addPotionEffect(new PotionEffect(2, 500, 3));
		}
	}
}