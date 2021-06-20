package com.elcolomanco.riskofrainmod.items;

import java.util.List;

import com.elcolomanco.riskofrainmod.setup.ModSetup;
import com.elcolomanco.riskofrainmod.setup.RegistrySetup;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BustlingFungusItem extends Item {

	private final String TIMER_TAG = "time_to_active";
	private final String OLD_COORD_TAG_X = "old_coord_x";
	private final String OLD_COORD_TAG_Y = "old_coord_y";
	private final String OLD_COORD_TAG_Z = "old_coord_z";

	public BustlingFungusItem(Properties properties) {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.bustling_fungus.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.bustling_fungus.tooltip2"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.bustling_fungus.tooltip3"));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

		int amount = stack.getCount();
		SoundCategory soundcategory = entityIn instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.NEUTRAL;
		if (!worldIn.isClientSide) {
			if (entityIn instanceof LivingEntity ) {
				CompoundNBT stackNbt = stack.getOrCreateTag();
				int timer = stackNbt.getInt(TIMER_TAG);
				EffectInstance entityRegen = ((LivingEntity)entityIn).getEffect(Effects.REGENERATION);
				if (isEntityMoving(entityIn, stackNbt)) {
					timer = -40 / Math.max(1, amount / 4);
					stack.getOrCreateTag().putInt(TIMER_TAG, timer);
					if (entityRegen != null) {
						((LivingEntity)entityIn).removeEffect(entityRegen.getEffect());
					}
				} else {
					timer++;
					stackNbt.putInt(TIMER_TAG, timer);
				}
				if (timer == -1) {
					worldIn.playSound(null, entityIn.getX(), entityIn.getY(), entityIn.getZ(), RegistrySetup.MUSHROOM_PROC_1.get(), soundcategory, 0.5F, 1.0F);
					worldIn.playSound(null, entityIn.getX(), entityIn.getY(), entityIn.getZ(), RegistrySetup.MUSHROOM_PROC_2.get(), soundcategory, 0.5F, 1.0F);
				}
				if (timer >= 0) {
					int regenLevel = 0;
					if (amount >= 4 && amount < 8) {
						regenLevel = 1;
					} else if (amount >= 8 && amount < 12) {
						regenLevel = 2;
					} else if (amount >= 12 && amount < 16) {
						regenLevel = 3;
					} else if (amount >= 16) {
						regenLevel = 4;
					}
					if (entityRegen == null || entityRegen.getDuration() < 40) {
						((LivingEntity)entityIn).addEffect(new EffectInstance(Effects.REGENERATION, 100, regenLevel, true, true));
					}
					stackNbt.putInt(TIMER_TAG, 0);
				}
				stackNbt.putDouble(OLD_COORD_TAG_X, entityIn.getX());
				stackNbt.putDouble(OLD_COORD_TAG_Y, entityIn.getY());
				stackNbt.putDouble(OLD_COORD_TAG_Z, entityIn.getZ());
				stack.setTag(stackNbt);
			}
		}
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}
		
	private boolean isEntityMoving(Entity entity, CompoundNBT stackNbt) {
		// I can't figure out why deltaMovement is such garbage so yeah I'm using this instead
		double oldX = stackNbt.getDouble(OLD_COORD_TAG_X);
		double oldY = stackNbt.getDouble(OLD_COORD_TAG_Y);
		double oldZ = stackNbt.getDouble(OLD_COORD_TAG_Z);
		return entity.getX() != oldX || entity.getY() != oldY || entity.getZ() != oldZ;
	}
}
