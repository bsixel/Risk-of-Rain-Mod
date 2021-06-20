package com.elcolomanco.riskofrainmod.items;

import java.util.List;

import com.elcolomanco.riskofrainmod.RoRmod;
import com.elcolomanco.riskofrainmod.setup.ModSetup;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TopazBroochItem extends Item {
	
	public TopazBroochItem(Properties properties) {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.topaz_brooch.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.topaz_brooch.tooltip2"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.topaz_brooch.tooltip3"));
	}
	
	public static int getAmplifier(int amount) {
		return (int) RoRmod.clamp((float)amount / 4, 1, 16);
	}
}
