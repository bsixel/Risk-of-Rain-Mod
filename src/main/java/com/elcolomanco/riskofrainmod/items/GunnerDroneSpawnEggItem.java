package com.elcolomanco.riskofrainmod.items;

import java.util.List;
import java.util.Objects;

import com.elcolomanco.riskofrainmod.setup.ModSetup;
import com.elcolomanco.riskofrainmod.setup.RegistrySetup;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class GunnerDroneSpawnEggItem extends Item {

	public GunnerDroneSpawnEggItem() {
		super(new Item.Properties()
				.stacksTo(64)
				.tab(ModSetup.RIKSOFRAIN_GROUP));
	}
	
	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.gunner_drone_spawn_egg.tooltip1"));
		tooltip.add(new TranslationTextComponent("item.riskofrainmod.gunner_drone_spawn_egg.tooltip2"));
	}
	
	@Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        } else {
        	ItemStack itemstack = context.getItemInHand();
        	BlockPos blockpos = context.getClickedPos();
        	Direction direction = context.getClickedFace();
        	BlockState blockstate = world.getBlockState(blockpos);
        	BlockPos blockpos1;
            if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }
            if (RegistrySetup.GUNNER_DRONE.get().spawn((ServerWorld)world, itemstack, context.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
                itemstack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
	}
}
