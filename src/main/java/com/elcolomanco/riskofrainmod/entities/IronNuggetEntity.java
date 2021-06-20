package com.elcolomanco.riskofrainmod.entities;

import com.elcolomanco.riskofrainmod.setup.RegistrySetup;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class IronNuggetEntity extends ProjectileItemEntity {

	public IronNuggetEntity(EntityType<IronNuggetEntity> type, World worldIn) {
		super(type, worldIn);
	}
	
	public IronNuggetEntity(LivingEntity entity, World worldIn) {
		super(RegistrySetup.DRONE_BULLET_ENTITY.get(), entity, worldIn);
	}
	
	public IronNuggetEntity(double x, double y, double z, World worldIn) {
		super(RegistrySetup.DRONE_BULLET_ENTITY.get(), x, y, z, worldIn);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.IRON_NUGGET;
	}

	@Override
	protected void onHit(RayTraceResult result) {
		if (result.getType() == RayTraceResult.Type.ENTITY) {
			Entity entity = ((EntityRayTraceResult)result).getEntity();
			entity.hurt(DamageSource.thrown(this, this.getOwner()), (float)2.0);
		}
		if (!this.level.isClientSide) {
			this.remove();
		}
	}
	
	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
