package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderItemInFirstPersonEvent
extends Event {
    public final EntityLivingBase entity;
    public final ItemCameraTransforms.TransformType transformType;
    public final boolean leftHanded;
    public ItemStack stack;

    public RenderItemInFirstPersonEvent(EntityLivingBase entitylivingbaseIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform, boolean leftHanded, int stage) {
        super(stage);
        this.entity = entitylivingbaseIn;
        this.stack = heldStack;
        this.transformType = transform;
        this.leftHanded = leftHanded;
    }

    public ItemCameraTransforms.TransformType getTransformType() {
        return this.transformType;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    public EntityLivingBase getEntity() {
        return this.entity;
    }
}

