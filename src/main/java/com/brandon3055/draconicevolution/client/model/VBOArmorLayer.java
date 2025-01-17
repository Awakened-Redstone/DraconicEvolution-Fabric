package com.brandon3055.draconicevolution.client.model;

import codechicken.lib.util.SneakyUtils;
import com.brandon3055.draconicevolution.integration.equipment.EquipmentManager;
import com.brandon3055.draconicevolution.items.equipment.ModularChestpiece;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 30/6/20
 */
public class VBOArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    public VBOArmorLayer(IEntityRenderer<T, M> renderer, @Nullable BipedArmorLayer<T, M, A> parent) {
        super(renderer, parent == null ? SneakyUtils.unsafeCast(new BipedModel<T>(0.5F)) : parent.innerModel, parent == null ? SneakyUtils.unsafeCast(new BipedModel<T>(1.0F)) : parent.outerModel);
    }

    protected void setPartVisibility(A model, EquipmentSlotType slot) {
        this.setModelVisible(model);
        switch (slot) {
            case HEAD:
                model.head.visible = true;
                model.hat.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
                break;
            case LEGS:
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
        }

    }

    protected void setModelVisible(A model) {
        model.setAllVisible(false);
    }

    @Override
    protected A getArmorModelHook(T entity, net.minecraft.item.ItemStack itemStack, EquipmentSlotType slot, A model) {
        return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }


    @Override
    public void render(MatrixStack mStack, IRenderTypeBuffer getter, int packedLightIn, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.CHEST, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.LEGS, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.FEET, packedLightIn);
        this.renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.HEAD, packedLightIn);

        ItemStack stack = EquipmentManager.findItem(e -> e.getItem() instanceof ModularChestpiece, livingEntity);
        if (!stack.isEmpty()) {
            renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.CHEST, stack, packedLightIn, !livingEntity.getItemBySlot(EquipmentSlotType.CHEST).isEmpty());
        }
    }

    private void renderArmorPart(MatrixStack mStack, IRenderTypeBuffer getter, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType slot, int packedLight) {
        ItemStack itemstack = livingEntity.getItemBySlot(slot);
        renderArmorPart(mStack, getter, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, slot, itemstack, packedLight, false);
    }

    private void renderArmorPart(MatrixStack mStack, IRenderTypeBuffer getter, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType slot, ItemStack itemstack, int packedLight, boolean onArmor) {
        if (itemstack.getItem() instanceof ArmorItem) {
            ArmorItem armoritem = (ArmorItem) itemstack.getItem();
            if (armoritem.getSlot() == slot) {
                A baseModel = this.getArmorModel(slot);
                A model = baseModel;

                if (armoritem instanceof ModularChestpiece) {
                    model = ((ModularChestpiece) armoritem).getChestPieceModel(livingEntity, itemstack, slot, onArmor);
                } else {
                    model = getArmorModelHook(livingEntity, itemstack, slot, model);
                }

                if (model instanceof VBOBipedModel) {
                    @SuppressWarnings("unchecked") VBOBipedModel<T> bpModel = (VBOBipedModel<T>) model;
                    BipedModel<T> entityModel = this.getParentModel();
                    entityModel.copyPropertiesTo(bpModel);
                    bpModel.leftArmPose = entityModel.leftArmPose;
                    bpModel.rightArmPose = entityModel.rightArmPose;
                    bpModel.crouching = entityModel.crouching;
                    bpModel.bipedHead.copyFrom(entityModel.head);
                    bpModel.bipedBody.copyFrom(entityModel.body);
                    bpModel.bipedRightArm.copyFrom(entityModel.rightArm);
                    bpModel.bipedLeftArm.copyFrom(entityModel.leftArm);
                    bpModel.bipedRightLeg.copyFrom(entityModel.rightLeg);
                    bpModel.bipedLeftLeg.copyFrom(entityModel.leftLeg);
                    bpModel.render(mStack, getter, livingEntity, itemstack, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }
    }
}
