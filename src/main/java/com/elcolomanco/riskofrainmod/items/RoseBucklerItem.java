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

public class RoseBucklerItem extends Item {

	public RoseBucklerItem(Properties properties) {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.rose_buckler.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.rose_buckler.tooltip2"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.rose_buckler.tooltip3"));
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		
		if (!worldIn.isClientSide) {
			if (entityIn instanceof LivingEntity) {
				if (entityIn.isSprinting()) {
					((LivingEntity)entityIn).addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 2, this.getAmplifier(stack.getCount()), true, false));
				}
			}
		}
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}
	
	public int getAmplifier(int amount) {
		return (int) RoRmod.clamp(amount, 0, 3);
	}
}
