package com.brandon3055.draconicevolution.client;

import codechicken.lib.render.buffer.TransformingVertexBuilder;
import com.brandon3055.brandonscore.client.BCSprites;
import com.brandon3055.draconicevolution.DraconicEvolution;
import com.brandon3055.draconicevolution.blocks.tileentity.TileEnergyTransfuser;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 14/12/20
 * This all goes through BCSprites which means these are also registered to the BC GUI Texture sheet.
 * This means i can use these sprites anywhere i the BCSprites can be used without having to worry about texture sheets.
 */
public class DESprites {

    public static ResourceLocation LOCATION_GUI_ATLAS = BCSprites.LOCATION_GUI_ATLAS;
    public static final RenderType GUI_TEX_TYPE = BCSprites.GUI_TYPE;

    public static void initialize(ColorHandlerEvent.Block event) {
        //Gui Transfuser
        for (TileEnergyTransfuser.ItemIOMode mode : TileEnergyTransfuser.ItemIOMode.values()) {
            register(DraconicEvolution.MODID, mode.getSpriteName());
        }
        register(DraconicEvolution.MODID, "transfuser/balanced_charge");
        register(DraconicEvolution.MODID, "transfuser/sequential_charge");

        register(DraconicEvolution.MODID, "dislocator/slot");
        register(DraconicEvolution.MODID, "dislocator/slot_selected");
        register(DraconicEvolution.MODID, "dislocator/locked");
        register(DraconicEvolution.MODID, "dislocator/unlocked");
        register(DraconicEvolution.MODID, "dislocator/delete");
        register(DraconicEvolution.MODID, "dislocator/add_top");
        register(DraconicEvolution.MODID, "dislocator/add_bottom");

        // Gui Draconium chest
        register(DraconicEvolution.MODID, "chest/fire_over");
        register(DraconicEvolution.MODID, "chest/fire_base");
        register(DraconicEvolution.MODID, "chest/feed_off");
        register(DraconicEvolution.MODID, "chest/feed_all");
        register(DraconicEvolution.MODID, "chest/feed_filter");
        register(DraconicEvolution.MODID, "chest/feed_filter_sticky");
        register(DraconicEvolution.MODID, "chest/color_picker");

        // Hud
        register(DraconicEvolution.MODID, "hud/ryg_bar");
        register(DraconicEvolution.MODID, "hud/shield_icon");
        register(DraconicEvolution.MODID, "hud/undying");


        // Particle
        for (int i = 0; i < 8; i++) {
            register(DraconicEvolution.MODID, "effect/glitter_" + i);
        }
    }


    //region register

    public static void registerThemed(String modid, String location) {
        BCSprites.registerThemed(modid, location);
    }

    public static void register(String modid, String location) {
        BCSprites.register(modid, location);
    }

    public static void register(ResourceLocation location) {
        BCSprites.register(location);
    }

    //endregion
    public static RenderMaterial getThemed(String modid, String location) {
        return BCSprites.getThemed(modid, location);
    }

    public static RenderMaterial getThemed(String location) {
        return BCSprites.getThemed(DraconicEvolution.MODID, location);
    }

    public static RenderMaterial get(String modid, String location) {
        return BCSprites.get(modid, location);
    }

    public static RenderMaterial get(String location) {
        return BCSprites.get(DraconicEvolution.MODID, location);
    }

    public static TextureAtlasSprite getSprite(String location) {
        return get(location).sprite();
    }

    public static TextureAtlasSprite getSprite(String modid, String location) {
        return get(modid, location).sprite();
    }

    public static Supplier<RenderMaterial> themedGetter(String modid, String location) {
        return BCSprites.themedGetter(modid, location);
    }

    public static Supplier<RenderMaterial> themedGetter(String location) {
        return BCSprites.themedGetter(DraconicEvolution.MODID, location);
    }

    public static Supplier<RenderMaterial> getter(String modid, String location) {
        return BCSprites.getter(modid, location);
    }

    public static Supplier<RenderMaterial> getter(String location) {
        return BCSprites.getter(DraconicEvolution.MODID, location);
    }

    public static IVertexBuilder builder(IRenderTypeBuffer getter, MatrixStack mStack) {
        return new TransformingVertexBuilder(getter.getBuffer(BCSprites.GUI_TYPE), mStack);
    }

    public static IVertexBuilder builder(IRenderTypeBuffer getter) {
        return getter.getBuffer(BCSprites.GUI_TYPE);
    }
}
