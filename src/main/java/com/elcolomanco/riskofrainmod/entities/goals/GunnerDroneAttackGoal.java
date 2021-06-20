package com.elcolomanco.riskofrainmod.entities.goals;

import java.util.EnumSet;

import com.elcolomanco.riskofrainmod.entities.GunnerDroneEntity;
import com.elcolomanco.riskofrainmod.entities.IronNuggetEntity;
import com.elcolomanco.riskofrainmod.setup.RegistrySetup;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class GunnerDroneAttackGoal extends Goal {
	
	private final float maxAttackDistance;
	private final GunnerDroneEntity entity;
	private int	attackStep;
    private int	attackTime;
    
    public GunnerDroneAttackGoal(GunnerDroneEntity entityIn, float maxAttackDistanceIn) {
    	this.entity = entityIn;
    	this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
    	this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
    	LivingEntity livingentity = this.entity.getTarget();
    	return livingentity != null && livingentity.isAlive() && this.entity.canAttack(livingentity);
    }
    
    public void start() {
    	super.start();
        this.entity.setAggressive(true);
    	this.attackStep = 0;
    }

    @Override
    public void stop() {
    	LivingEntity livingentity = this.entity.getTarget();
		if(!EntityPredicates.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
			this.entity.setTarget(null);
		}
    	this.entity.setAggressive(false);
    	this.entity.getNavigation().stop();
    }

	public void tick() {
    	--this.attackTime;
    	LivingEntity livingentity = this.entity.getTarget();
    	if (livingentity != null) {
    		double d0 = this.entity.distanceToSqr(livingentity);
    		boolean flag = this.entity.getSensing().canSee(livingentity);
    		Vector3d vec3d = this.entity.getViewVector(1.0F);
    		Vector3d vec3d1 = this.entity.getLookAngle().multiply(1.0D, 1.0D, 1.0D).normalize().scale(0.01D).reverse();
			if (!flag) {
            	this.entity.getNavigation().moveTo(livingentity, 0.8D);
            }
			if (livingentity.getEyeY() > this.entity.getEyeY()) {
				Vector3d vec3d2 = this.entity.getDeltaMovement();
					this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(0.0D, ((double)0.1F - vec3d2.y) * (double)0.1F, 0.0D));
					this.entity.hasImpulse = true;
			}
    		if (d0 < (double)(this.maxAttackDistance) && flag) {
    			if (d0 < (this.maxAttackDistance * 0.3) && flag) {
    				this.entity.setDeltaMovement(this.entity.getDeltaMovement().add(vec3d1.x, 0.0D, vec3d1.z));
    			}
    			if (d0 > (this.maxAttackDistance * 0.75) && flag) {
    				this.entity.getNavigation().moveTo(livingentity, 0.8D);
    			}
    			this.entity.getLookControl().setLookAt(livingentity, 30.0F, 90.0F);
    			this.entity.lookAt(livingentity, 30.0F, 90.0F);
    		}
    		double d1 = livingentity.getX() - this.entity.getX() + vec3d.x * 4.0D;
			double d2 = livingentity.getY(0.3333333333333333D) - this.entity.getEyeY();
			double d3 = livingentity.getZ() - this.entity.getZ() + vec3d.z * 4.0D;
			
			if (d0 < (double)(this.maxAttackDistance) && flag) {
				if (this.attackTime <= 0) {
					++this.attackStep;
					if (this.attackStep == 1) {
						this.attackTime = 30;
					} else if (this.attackStep <= 5) {
						this.attackTime = 4;
					} else {
						this.attackTime = 30;
						this.attackStep = 0;
						this.entity.setTarget(null);
					}
					if (this.attackStep > 1) {
						SoundCategory soundcategory = this.entity instanceof GunnerDroneEntity ? SoundCategory.PLAYERS : SoundCategory.NEUTRAL;
						this.entity.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), RegistrySetup.GUNNER_DRONE_SHOOT.get(), soundcategory, 0.4F, 1.0F);
						
						for(int i = 0; i < 1; ++i) {
							IronNuggetEntity bullet = new IronNuggetEntity(this.entity, this.entity.level);
							bullet.shoot(d1, d2, d3, 3.0F, 1.0F);
							this.entity.level.addFreshEntity(bullet);
						}
					}
					this.entity.getLookControl().setLookAt(livingentity, 30.0F, 90.0F);
				}
			}
    		super.tick();
    	}
    }
}
