package com.brandon3055.draconicevolution.items.equipment;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.lib.TechPropBuilder;
import com.brandon3055.draconicevolution.api.modules.lib.ModularOPStorage;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleHostImpl;
import com.brandon3055.draconicevolution.init.EquipCfg;
import com.brandon3055.draconicevolution.init.ModuleCfg;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Created by brandon3055 on 21/5/20.
 */
public class ModularPickaxe extends PickaxeItem implements IModularMiningTool {
    public static final Set<Material> EFFECTIVE_MATS = ImmutableSet.of(Material.METAL, Material.HEAVY_METAL, Material.STONE, Material.GLASS);
    private final TechLevel techLevel;
    private final DEItemTier itemTier;

    public ModularPickaxe(TechPropBuilder props) {
        super(new DEItemTier(props, EquipCfg::getPickaxeDmgMult, EquipCfg::getPickaxeSpeedMult), 0, 0, props.pickaxeProps());
        this.techLevel = props.techLevel;
        this.itemTier = (DEItemTier) getTier();
    }

    @Override
    public TechLevel getTechLevel() {
        return techLevel;
    }

    @Override
    public DEItemTier getItemTier() {
        return itemTier;
    }

    @Override
    public ModuleHostImpl createHost(ItemStack stack) {
        return new ModuleHostImpl(techLevel, ModuleCfg.toolWidth(techLevel), ModuleCfg.toolHeight(techLevel), "pickaxe", ModuleCfg.removeInvalidModules);
    }

    @Nullable
    @Override
    public ModularOPStorage createOPStorage(ItemStack stack, ModuleHostImpl host) {
        return new ModularOPStorage(host, EquipCfg.getBaseToolEnergy(techLevel), EquipCfg.getBaseToolTransfer(techLevel));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return IModularMiningTool.super.getDestroySpeed(stack, state);
    }

    @Override
    public float getBaseEfficiency() {
        return getTier().getSpeed();
    }

    @Override
    public Set<Block> effectiveBlockAdditions() {
        return blocks;
    }

    @Override
    public boolean overrideEffectivity(Material material) {
        return EFFECTIVE_MATS.contains(material);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        addModularItemInformation(stack, worldIn, tooltip, flagIn);
    }
}
