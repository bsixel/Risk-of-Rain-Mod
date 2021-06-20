package com.elcolomanco.riskofrainmod.events;

import java.util.Random;

import com.elcolomanco.riskofrainmod.RoRconfig;
import com.elcolomanco.riskofrainmod.RoRmod;
import com.elcolomanco.riskofrainmod.entities.GunnerDroneEntity;
import com.elcolomanco.riskofrainmod.items.CrowbarItem;
import com.elcolomanco.riskofrainmod.items.LensMakersGlassesItem;
import com.elcolomanco.riskofrainmod.items.TopazBroochItem;
import com.elcolomanco.riskofrainmod.items.TougherTimesItem;
import com.elcolomanco.riskofrainmod.setup.RegistrySetup;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RoRmod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

	private static int getCountInInv(PlayerInventory inv, Item toMatch) {
		int count = 0;
		for(int n = 0; n <= 35; ++n) {
			ItemStack slot = inv.getItem(n);
			if (slot.getItem().equals(toMatch)) {
				count += slot.getCount();
			}
		}
		return count;
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onAdvancement(AdvancementEvent event) {
		if (RoRconfig.SOUNDS) {
			if (event.getEntityLiving() instanceof PlayerEntity) {
				PlayerEntity playerIn = (PlayerEntity)event.getEntityLiving();
				World worldIn = playerIn.level;
				SoundCategory soundcategory = SoundCategory.PLAYERS;
				
				worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), RegistrySetup.ADVANCEMENT_PROC.get(), soundcategory, 0.15F, 1.0F);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onCoinPickUp(EntityItemPickupEvent event) {
		if (RoRconfig.SOUNDS) {
			if (event.getEntityLiving() instanceof PlayerEntity) {
				ItemStack itemStack = event.getItem().getItem();
				Item item = itemStack.getItem();
				if (item == Items.GOLD_NUGGET) {
					PlayerEntity playerIn = (PlayerEntity)event.getEntityLiving();
					World worldIn = playerIn.level;
					SoundCategory soundcategory = SoundCategory.PLAYERS;
					
					worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), RegistrySetup.COIN_PROC.get(), soundcategory, 0.4F, 1.0F);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLevelUp(LevelChange event) {
		if (RoRconfig.SOUNDS) {
			if (event.getEntityLiving() instanceof PlayerEntity) {
				PlayerEntity playerIn = (PlayerEntity)event.getEntityLiving();
				World worldIn = playerIn.level;
				SoundCategory soundcategory = SoundCategory.PLAYERS;
				
				worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), RegistrySetup.LEVEL_UP_PROC.get(), soundcategory, 0.6F, 1.0F);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (RoRconfig.SOUNDS) {
			if (event.getEntityLiving() instanceof PlayerEntity) {
				PlayerEntity playerIn = (PlayerEntity)event.getEntityLiving();
				World worldIn = playerIn.level;
				SoundCategory soundcategory = SoundCategory.PLAYERS;
				
				worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), RegistrySetup.PLAYER_DEATH_PROC.get(), soundcategory, 1.0F, 1.0F);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void immuneDrones(LivingAttackEvent event) {
		if (event.getEntityLiving() instanceof GunnerDroneEntity) {
			if (event.getSource().getEntity() instanceof LivingEntity) {
				boolean tamed = false;
				GunnerDroneEntity drone = (GunnerDroneEntity)event.getEntityLiving();
				if (drone.isTame()) {
					tamed = true;
				}
				if (tamed) {
					return;
				}
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void crowbarEffect(LivingDamageEvent event) {
		if (event.getSource().getEntity() instanceof PlayerEntity) {
			PlayerEntity killer = (PlayerEntity)event.getSource().getEntity();
			PlayerInventory inv = killer.inventory;

			int crowbarCount = 0;
			for(int n = 0; n <= 35; ++n) {
				ItemStack slot = inv.getItem(n);
				if (slot.getItem().equals(RegistrySetup.CROWBAR.get())) {
					crowbarCount += slot.getCount();
				}
			}
			if (crowbarCount <= 0) {
				return;
			}
			boolean proc = false;
			LivingEntity entity = event.getEntityLiving();
			float currentHp = entity.getHealth();
			float maxHp = entity.getMaxHealth();
			if (currentHp >= (maxHp * 0.9)) {
				proc = true;
			}
			if (!proc) {
				return;
			}
			float damage = event.getAmount();

			double multiplier = CrowbarItem.getDamageMultiplier(crowbarCount);
			float crowbarDamage = (float)(damage * multiplier);
			World worldIn = killer.level;
			SoundCategory soundcategory = SoundCategory.PLAYERS;
			
			event.setAmount(crowbarDamage);
			worldIn.playSound(null, killer.getX(), killer.getY(), killer.getZ(), RegistrySetup.CROWBAR_PROC.get(), soundcategory, 0.4F, killer.getRandom().nextFloat() * 0.1F + 0.9F);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void lensMakersGlassesEffect(LivingDamageEvent event) {
		
		if (event.getSource().getEntity() instanceof PlayerEntity) {
			PlayerEntity killer = (PlayerEntity)event.getSource().getEntity();
			PlayerInventory inv = killer.inventory;

			int count = getCountInInv(inv, RegistrySetup.LENS_MAKERS_GLASSES.get());
			if (count == 0) {
				return;
			}
			boolean isCrit = false;
			double chance = LensMakersGlassesItem.getChance(count);
			float damage = event.getAmount();
			float crit = damage * 2;
			int random = LensMakersGlassesItem.getRandomCrit();
			World worldIn = killer.level;
			SoundCategory soundcategory = SoundCategory.PLAYERS;
			
			if (random <= chance) {
				isCrit = true;
			}
			if (!isCrit) {
				return;
			}
			event.setAmount(crit);
			worldIn.playSound(null, killer.getX(), killer.getY(), killer.getZ(), RegistrySetup.LENS_CRIT_PROC.get(), soundcategory, 0.4F, killer.getRandom().nextFloat() * 0.1F + 0.9F);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void roseBucklerEffect(LivingDamageEvent event) {
		
		if (event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			PlayerInventory inv = player.inventory;

			int count = getCountInInv(inv, RegistrySetup.ROSE_BUCKLER.get());
			if (count == 0) {
				return;
			}
			World worldIn = player.level;
			SoundCategory soundcategory = SoundCategory.PLAYERS;
			if (player.isSprinting()) {
				worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), RegistrySetup.ROSE_BUCKLER_PROC.get(), soundcategory, 0.4F, 1.0F);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void topazBroochEffect(LivingDeathEvent event) {
		
		if (event.getSource().getEntity() instanceof PlayerEntity) {
			PlayerEntity killer = (PlayerEntity)event.getSource().getEntity();
			PlayerInventory inv = killer.inventory;

			int count = getCountInInv(inv, RegistrySetup.TOPAZ_BROOCH.get());
			if (count == 0) {
				return;
			}
			(killer).addEffect(new EffectInstance(Effects.ABSORPTION, 160, TopazBroochItem.getAmplifier(count), true, false));
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void tougherTimesEffect(LivingDamageEvent event) {
		
		if (event.getEntityLiving() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			PlayerInventory inv = player.inventory;

			int count = getCountInInv(inv, RegistrySetup.TOUGHER_TIMES.get());
			if (count == 0) {
				return;
			}
			boolean isBlocking = false;
			double chance = TougherTimesItem.getChance(count);
			int random = TougherTimesItem.getRandomBlockChance();
			World worldIn = player.level;
			SoundCategory soundcategory = SoundCategory.PLAYERS;
			
			if (random <= chance) {
				isBlocking = true;
			}
			if (!isBlocking) {
				return;
			}
			event.setAmount(0);
			event.setCanceled(true); // To stop procs, too
			worldIn.playSound(null, player.getX(), player.getY(), player.getZ(), RegistrySetup.TOUGHER_TIMES_PROC.get(), soundcategory, 0.4F, 1.0F);
		}
	}
}
