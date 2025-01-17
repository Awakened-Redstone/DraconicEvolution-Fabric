package com.brandon3055.draconicevolution.api.modules.entities;

import com.brandon3055.brandonscore.api.power.IOPStorageModifiable;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.utils.MathUtils;
import com.brandon3055.draconicevolution.api.capability.DECapabilities;
import com.brandon3055.draconicevolution.api.capability.ModuleHost;
import com.brandon3055.draconicevolution.api.modules.Module;
import com.brandon3055.draconicevolution.api.modules.ModuleTypes;
import com.brandon3055.draconicevolution.api.modules.data.UndyingData;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleContext;
import com.brandon3055.draconicevolution.api.modules.lib.ModuleEntity;
import com.brandon3055.draconicevolution.api.modules.lib.StackModuleContext;
import com.brandon3055.draconicevolution.client.render.item.ToolRenderBase;
import com.brandon3055.draconicevolution.network.DraconicNetwork;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.Iterator;

public class UndyingEntity extends ModuleEntity {

    private int charge;
    private int invulnerableTime = 0;

    public UndyingEntity(Module<UndyingData> module) {
        super(module);
    }

    @Override
    public void onInstalled(ModuleContext context) {
        super.onInstalled(context);
        invulnerableTime = 0;
    }

    @Override
    public void tick(ModuleContext moduleContext) {
        if (invulnerableTime > 0) {
            invulnerableTime--;
            if (moduleContext instanceof StackModuleContext) {
                LivingEntity entity = ((StackModuleContext) moduleContext).getEntity();
                if (entity instanceof PlayerEntity) {
                    if (invulnerableTime == 0){
                        ((PlayerEntity) entity).displayClientMessage(new StringTextComponent(""), true);
                    } else {
                        ((PlayerEntity) entity).displayClientMessage(new TranslationTextComponent("module.draconicevolution.undying.invuln.active", MathUtils.round(invulnerableTime / 20D, 10)).withStyle(TextFormatting.GOLD), true);
                    }
                }
            }
        }

        IOPStorageModifiable storage = moduleContext.getOpStorage();
        if (!(moduleContext instanceof StackModuleContext && EffectiveSide.get().isServer() && storage != null)) {
            return;
        }

        StackModuleContext context = (StackModuleContext) moduleContext;
        UndyingData data = (UndyingData) module.getData();
        if (!context.isEquipped() || charge >= data.getChargeTime()) {
            return;
        }

        if (storage.getOPStored() >= data.getChargeEnergyRate()) {
            storage.modifyEnergyStored(-data.getChargeEnergyRate());
            charge++;
        }
    }

    public boolean tryBlockDamage(LivingAttackEvent event) {
        if (invulnerableTime > 0) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public boolean tryBlockDamage(LivingDamageEvent event) {
        if (invulnerableTime > 0) {
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    public boolean isCharged() {
        UndyingData data = (UndyingData) module.getData();
        return charge >= data.getChargeTime();
    }

    public double getCharge() {
        UndyingData data = (UndyingData) module.getData();
        return charge / (double)data.getChargeTime();
    }

    public boolean tryBlockDeath(LivingDeathEvent event) {
        UndyingData data = (UndyingData) module.getData();
        if (charge >= data.getChargeTime()) {
            LivingEntity entity = event.getEntityLiving();
            entity.setHealth(entity.getHealth() + data.getHealthBoost());
            ItemStack stack = entity.getItemBySlot(EquipmentSlotType.CHEST);
            if (!stack.isEmpty()) {
                LazyOptional<ModuleHost> optionalHost = stack.getCapability(DECapabilities.MODULE_HOST_CAPABILITY);
                optionalHost.ifPresent(stackHost -> {
                    ShieldControlEntity shield = stackHost.getEntitiesByType(ModuleTypes.SHIELD_CONTROLLER).map(e -> (ShieldControlEntity) e).findAny().orElse(null);
                    if (shield != null) {
                        shield.boost(data.getShieldBoost(), data.getShieldBoostTime());
                    }
                });
            }
            if (module.getModuleTechLevel().index >= 2) {
                entity.clearFire();
                Iterator<EffectInstance> iterator = entity.getActiveEffectsMap().values().iterator();
                while (iterator.hasNext()) {
                    EffectInstance effect = iterator.next();
                    if (!effect.getEffect().isBeneficial()) {
                        entity.onEffectRemoved(effect);
                        iterator.remove();
                    }
                }
            }
            charge = 0;
            DraconicNetwork.sendUndyingActivation(entity, module.getItem());
            entity.level.playSound(null, entity.blockPosition(), SoundEvents.TOTEM_USE, SoundCategory.PLAYERS, 5F, (0.95F + (entity.level.random.nextFloat() * 0.1F)));
            invulnerableTime = data.getInvulnerableTime();
            return true;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderSlotOverlay(IRenderTypeBuffer getter, Minecraft mc, int x, int y, int width, int height, double mouseX, double mouseY, boolean mouseOver, float partialTicks) {
        UndyingData data = (UndyingData) module.getData();
        if (charge >= data.getChargeTime()) return;
        double diameter = Math.min(width, height) * 0.425;
        double progress = charge / Math.max(1D, data.getChargeTime());
        MatrixStack mStack = new MatrixStack();

        GuiHelper.drawRect(getter, mStack, x, y, width, height, 0x20FF0000);
        IVertexBuilder builder = getter.getBuffer(GuiHelperOld.FAN_TYPE);
        builder.vertex(x + (width / 2D), y + (height / 2D), 0).color(0, 255, 255, 64).endVertex();
        for (double d = 0; d <= 1; d += 1D / 30D) {
            double angle = (d * progress) + 0.5 - progress;
            double vertX = x + (width / 2D) + Math.sin(angle * (Math.PI * 2)) * diameter;
            double vertY = y + (height / 2D) + Math.cos(angle * (Math.PI * 2)) * diameter;
            builder.vertex(vertX, vertY, 0).color(255, 255, 255, 64).endVertex();
        }
        ToolRenderBase.endBatch(getter);

        String pText = (int) (progress * 100) + "%";
        String tText = ((data.getChargeTime() - charge) / 20) + "s";
        drawBackgroundString(getter, mStack, mc.font, pText, x + width / 2F, y + height / 2F - 8, 0, 0x4000FF00, 1, false, true);
        drawBackgroundString(getter, mStack, mc.font, tText, x + width / 2F, y + height / 2F + 1, 0, 0x4000FF00, 1, false, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawBackgroundString(IRenderTypeBuffer getter, MatrixStack mStack, FontRenderer font, String text, float x, float y, int colour, int background, int padding, boolean shadow, boolean centered) {
        MatrixStack matrixstack = new MatrixStack();
        int width = font.width(text);
        x = centered ? x - width / 2F : x;
        GuiHelper.drawRect(getter, mStack, x - padding, y - padding, width + padding * 2, font.lineHeight - 2 + padding * 2, background);
        font.drawInBatch(text, x, y, colour, shadow, matrixstack.last().pose(), getter, false, 0, 15728880);
    }


    @Override
    public void writeToItemStack(ItemStack stack, ModuleContext context) {
        super.writeToItemStack(stack, context);
        stack.getOrCreateTag().putInt("charge", charge);
    }

    @Override
    public void readFromItemStack(ItemStack stack, ModuleContext context) {
        super.readFromItemStack(stack, context);
        if (stack.hasTag()) {
            charge = stack.getOrCreateTag().getInt("charge");
        }
    }

    @Override
    public void writeToNBT(CompoundNBT compound) {
        super.writeToNBT(compound);
        compound.putInt("charge", charge);
        compound.putInt("invul", invulnerableTime);
    }

    @Override
    public void readFromNBT(CompoundNBT compound) {
        super.readFromNBT(compound);
        charge = compound.getInt("charge");
        invulnerableTime = compound.getInt("invul");
    }
}
