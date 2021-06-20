package com.elcolomanco.riskofrainmod.items;

import java.util.List;
import java.util.Random;

import com.elcolomanco.riskofrainmod.RoRmod;
import com.elcolomanco.riskofrainmod.setup.ModSetup;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class TougherTimesItem extends Item {

	public TougherTimesItem(Properties properties) {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.tougher_times.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.tougher_times.tooltip2"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.tougher_times.tooltip3"));
	}
	
	public static double getChance(int amount) {
		return 3.75 * RoRmod.clamp((float)amount / 4, 1, 16); // Anywhere between 3.75 and 60
	}
	
	public static int getRandomBlockChance() {
		return random.nextInt(99);
	}
}
