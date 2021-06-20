package com.elcolomanco.riskofrainmod.items;

import java.util.List;

import com.elcolomanco.riskofrainmod.RoRmod;
import com.elcolomanco.riskofrainmod.setup.ModSetup;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SoldiersSyringeItem extends Item {
	
	public SoldiersSyringeItem(Properties properties) {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.soldiers_syringe.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.soldiers_syringe.tooltip2"));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		
		if (!worldIn.isClientSide && entityIn instanceof LivingEntity) {
			EffectInstance entityRegen = ((LivingEntity)entityIn).getEffect(Effects.DIG_SPEED);
			if (entityRegen == null || entityRegen.getDuration() < 40) {
				((LivingEntity)entityIn).addEffect(new EffectInstance(Effects.DIG_SPEED, 2, (int) RoRmod.clamp( (float)stack.getCount() / 4, 0,4), true, false));
			}
		}
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}
}
