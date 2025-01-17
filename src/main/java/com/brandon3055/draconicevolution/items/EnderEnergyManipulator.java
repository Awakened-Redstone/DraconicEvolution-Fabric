package com.brandon3055.draconicevolution.items;

import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.utils.InventoryUtils;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 9/05/2017.
 */
public class EnderEnergyManipulator extends Item /*implements IRenderOverride*/ {

    public EnderEnergyManipulator(Properties properties) {
        super(properties);
//        this.setMaxStackSize(8);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        assert false; //Not Implemented
        PlayerEntity player = context.getPlayer();
        World cWorld = context.getLevel();
        Hand hand = context.getHand();
        BlockPos pos = context.getClickedPos();

        if (cWorld instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) cWorld;

            ItemStack stack = context.getItemInHand();
            BlockState state = world.getBlockState(pos);
            List<Entity> list = world.getEntities(null, Entity::isAlive);
            if (world.dimension() == World.END && Utils.getDistanceAtoB(Vec3D.getCenter(pos), new Vec3D(0, pos.getY(), 0)) <= 8 && state.getBlock() == Blocks.BEDROCK && list.isEmpty()) {
                if (!world.isClientSide) {
//                    EntityEnderEnergyManipulator entity = new EntityEnderEnergyManipulator(world);
//                    entity.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//                    world.addEntity(entity);
                }

                InventoryUtils.consumeHeldItem(player, stack, hand);
                return ActionResultType.SUCCESS;
            }

            if (!world.isClientSide) {
                if (!list.isEmpty()) {
                    player.sendMessage(new TranslationTextComponent("info.de.ender_energy_manipulator.running.msg"), Util.NIL_UUID);
                } else {
                    player.sendMessage(new TranslationTextComponent("info.de.ender_energy_manipulator.location.msg"), Util.NIL_UUID);
                }
            }
        }
        return super.useOn(context);
    }

//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public void registerRenderer(Feature feature) {
//        ModelRegistryHelper.registerItemRenderer(this, new RenderItemEnderEnergyManipulator());
//    }
//
//    @Override
//    public boolean registerNormal(Feature feature) {
//        return false;
//    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslationTextComponent("info.de.ender_energy_manipulator.info.txt"));
        tooltip.add(new TranslationTextComponent("info.de.ender_energy_manipulator.info2.txt"));
    }
}
