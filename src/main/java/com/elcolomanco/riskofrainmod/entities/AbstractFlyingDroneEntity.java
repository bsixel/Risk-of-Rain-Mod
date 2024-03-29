package com.elcolomanco.riskofrainmod.entities;

import com.elcolomanco.riskofrainmod.setup.RegistrySetup;
import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public abstract class AbstractFlyingDroneEntity extends TameableEntity implements IFlyingAnimal {
	
	private final FollowOwnerGoal followOwnerGoal = new FollowOwnerGoal(this, 1.0D, 6.0F, 4.0F, true);
	private final WaterAvoidingRandomFlyingGoal randomFyingGoal = new WaterAvoidingRandomFlyingGoal(this, 0.5D);
	private final SwimGoal swimGoal = new SwimGoal(this);
	
	private static final Set<Item> TAME_ITEMS = Sets.newHashSet(Items.GOLD_NUGGET);
	
	private float rollAmount;
	private float rollAmountO;
	private int   flyingSound;
	private int	  underWaterTicks;
	
	protected AbstractFlyingDroneEntity(EntityType<? extends AbstractFlyingDroneEntity> type, World worldIn) {
		super(type, worldIn);
		this.moveControl = new FlyingMovementController(this, 10, false);
		this.setPathfindingMalus(PathNodeType.DANGER_FIRE, -1.0F);
		this.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1.0F);
		this.setPathfindingMalus(PathNodeType.COCOA, -1.0F);
	}

	@Override
	protected PathNavigator createNavigation(World worldIn) {
		FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, worldIn);
		flyingpathnavigator.setCanOpenDoors(false);
		flyingpathnavigator.setCanFloat(false);
		flyingpathnavigator.setCanPassDoors(true);
		return flyingpathnavigator;
	}
	
	@Override
	protected void customServerAiStep() {
		if (this.isTame()) {
			this.goalSelector.addGoal(3, this.followOwnerGoal);
			this.goalSelector.addGoal(4, this.randomFyingGoal);
			this.goalSelector.addGoal(9, this.swimGoal);
		}
		if (this.isInWaterRainOrBubble()) {
			++this.underWaterTicks;
		} else {
			this.underWaterTicks = 0;
		}
		if (this.underWaterTicks > 20) {
			this.hurt(DamageSource.DROWN, 1.0F);
		}
	}
	
	@Override
	public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		
		if (itemstack.getItem() instanceof SpawnEggItem) {
			return super.mobInteract(player, hand);
		} else if (!this.isTame() && TAME_ITEMS.contains(itemstack.getItem())) {
			if (!player.abilities.instabuild) {
				itemstack.shrink(1);
			}
			if (!this.isSilent()) {
				this.level.playSound(null, this.getX(), this.getY(), this.getZ(), RegistrySetup.COIN_PROC.get(), this.getSoundSource(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			}
			if (!this.level.isClientSide) {
				if (this.random.nextInt(10) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
					this.tame(player);
					this.navigation.stop();
					this.setTarget(null);
					this.setOrderedToSit(true);
					this.level.playSound(null, this.getX(), this.getY(), this.getZ(), RegistrySetup.DRONE_REPAIR.get(), this.getSoundSource(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
					this.level.broadcastEntityEvent(this, (byte)7);
				} else {
					this.level.broadcastEntityEvent(this, (byte)6);
				}
			}
			return ActionResultType.SUCCESS;
		} else if (!this.isTame() && !TAME_ITEMS.contains(itemstack.getItem())) {
			this.level.playSound(null, this.getX(), this.getY(), this.getZ(), RegistrySetup.INSUFFICIENT_FOUNDS_PROC.get(), this.getSoundSource(), 0.5F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			return ActionResultType.SUCCESS;
		} else if (this.isTame() && this.isOwnedBy(player) && !this.isRepairItem(itemstack)) {
			if (!this.level.isClientSide) {
				this.setOrderedToSit(!this.isInSittingPose());
				this.level.playSound(null, this.getX(), this.getY(), this.getZ(), RegistrySetup.DRONE_REPAIR.get(), this.getSoundSource(), 0.2F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
			}
			return ActionResultType.SUCCESS;
		} else if (this.isTame() && this.getHealth() < this.getMaxHealth()) {
			if (this.isRepairItem(itemstack)) {
				if (!player.abilities.instabuild) {
					itemstack.shrink(1);
				}
				this.heal(2.0F);
				return ActionResultType.SUCCESS;
			}
		}
		return super.mobInteract(player, hand);
	}
	
	public boolean isRepairItem(ItemStack stack) {
		Item item = stack.getItem();
		return item.equals(Items.IRON_NUGGET);
	}
	
	public void livingTick() {
		if (!this.isTame() || this.onGround || this.isInSittingPose()) {
			flyingSound = 0;
		} else {
			++flyingSound;
		}
		if (flyingSound == 1 || flyingSound == 31) {
			this.playSound(this.getFlyingSound(), 0.05F, 1.0F);
			flyingSound = 1;
		}
		if (this.isLowHealth()) {
			for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
				this.addParticle(this.level, this.getX() - (double)0.3F, this.getX() + (double)0.3F, this.getZ() - (double)0.3F, this.getZ() + (double)0.3F, this.getY(0.5D), ParticleTypes.SMOKE);
			}
		}
		Vector3d vec3d = this.getDeltaMovement();
		if (this.isInSittingPose()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0D, ((double)-0.1F - vec3d.y), 0.0D));
		}
		super.aiStep();
		this.updateBodyPitch();
	}
	
	public boolean hurt(DamageSource source, float amount) {
		if (this.isInvulnerableTo(source)) {
			return false;
		} else {
			this.setOrderedToSit(false);
			return super.hurt(source, amount);
		}
	}
	
	@Override
	public void die(DamageSource cause) {
		this.playSound(this.getDeathSound2(), 0.6F, 1.0F);
		super.die(cause);
	}
	
	@Override
	protected SoundEvent getDeathSound() {
		return RegistrySetup.DRONE_DEATH1.get();
	}
	
	protected SoundEvent getDeathSound2() {
		return RegistrySetup.DRONE_DEATH2.get();
	}
	
	protected SoundEvent getFlyingSound() {
		return RegistrySetup.DRONE_FLYING.get();
	}
	
	protected void playStepSound(BlockPos pos, BlockState blockIn) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public float getBodyPitch(float pitch) {
		return MathHelper.lerp(pitch, this.rollAmountO, this.rollAmount);
	}
	
	private void updateBodyPitch() {
		this.rollAmountO = this.rollAmount;
		if (!this.isFlying() || this.isDroneMoving()) {
			this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
		} else {
			this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
		}
	}
	
	public boolean isLowHealth() {
		double health = this.getHealth();
		if(health < 10.0D) {
			return true;
		} else {
			return false;
		}
	}
	
	private void addParticle(World worldIn, double p_226397_2_, double p_226397_4_, double p_226397_6_, double p_226397_8_, double posY, IParticleData particleData) {
		worldIn.addParticle(particleData, MathHelper.lerp(worldIn.random.nextDouble(), p_226397_2_, p_226397_4_), posY, MathHelper.lerp(worldIn.random.nextDouble(), p_226397_6_, p_226397_8_), 0.0D, 0.0D, 0.0D);
	}

	@Override
	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}
	
	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, @Nonnull BlockState state, @Nonnull BlockPos pos) {
	}

	@Override
	public boolean isFood(@Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public boolean canMate(@Nonnull AnimalEntity otherAnimal) {
		return false;
	}
	
	public boolean isFlying() {
		return !this.onGround;
	}
	
	public boolean isDroneMoving() {
		return this.xOld != this.getX() || this.zOld != this.getZ() || this.yOld != this.getY();
	}

	@Nullable
	@Override
	public AgeableEntity getBreedOffspring(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
		return null;
	}
}
