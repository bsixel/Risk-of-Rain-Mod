package com.elcolomanco.riskofrainmod.entities.goals;

import java.util.EnumSet;

import com.elcolomanco.riskofrainmod.entities.LemurianEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;

public class LemurianAttackGoal extends Goal {

	private final LemurianEntity entity;
	private final double moveSpeedAmp;
	
	protected int attackTick;
	private final boolean longMemory;
	private Path path;
	private int delayCounter;
	private double targetX;
	private double targetY;
	private double targetZ;
	private int failedPathFindingPenalty = 0;
	private boolean canPenalize = false;
	
	private final float maxAttackDistance;
    private int attackStep;
    private int attackTime;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public LemurianAttackGoal(LemurianEntity entityIn, double moveSpeedAmpIn, float maxAttackDistanceIn, boolean useLongMemory) {
    	this.entity = entityIn;
    	this.moveSpeedAmp = moveSpeedAmpIn;
    	this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
    	this.longMemory = useLongMemory;
    	this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    // Returns whether execution should begin. You can also read and cache any state necessary for execution in this method as well
	@Override
    public boolean canUse() {

    	if(entity.level.getDifficulty() == Difficulty.PEACEFUL) {
			return false;
		}
    	LivingEntity livingentity = this.entity.getTarget();
    	return livingentity != null && livingentity.isAlive() && this.entity.canAttack(livingentity);
    }

    @Override
    public boolean canContinueToUse() {
    	LivingEntity livingentity = this.entity.getTarget();
		if(livingentity == null) {
			return false;
		} else if(!livingentity.isAlive()) {
			return false;
		} else if(!this.longMemory) {
			return !this.entity.getNavigation().isDone();
		} else if(!this.entity.isWithinRestriction(livingentity.blockPosition())) {
			return false;
		} else {
			return !(livingentity instanceof PlayerEntity) || !livingentity.isSpectator() && !((PlayerEntity) livingentity).isCreative();
		}
	}

	// Execute a one shot task or start executing a continuous task
	@Override
    public void start() {
    	super.start();
    	this.entity.getNavigation().moveTo(this.path, this.moveSpeedAmp);
        this.entity.setAggressive(true);
    	this.attackStep = 0;
    	this.delayCounter = 0;
    }

    // Reset the task's internal state. Called when this task is interrupted by another one
	@Override
	public void stop() {
    	LivingEntity livingentity = this.entity.getTarget();
		if(!EntityPredicates.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
			this.entity.setTarget(null);
		}
    	this.entity.setAggressive(false);
		this.entity.getNavigation().stop();
        this.seeTime = 0;
    }

    @Override
    // Keep ticking a continuous task that has already been started
    public void tick() {
    	--this.attackTime;
    	LivingEntity livingentity = this.entity.getTarget();
    	if (livingentity != null) {
    		double d0 = this.entity.distanceToSqr(livingentity.getX(), livingentity.getY(), livingentity.getZ());
    		boolean flag = this.entity.getSensing().canSee(livingentity);
    		
    		if (flag) {
    			++this.seeTime;
    		} else {
    			--this.seeTime;
    		}
    		// Melee attack
    		if (d0 < (this.maxAttackDistance * 0.3)) {
    			if (!flag) {
                	this.entity.getNavigation().moveTo(livingentity, this.moveSpeedAmp);
                }
                this.entity.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
                --this.delayCounter;
                if ((this.longMemory || this.entity.getSensing().canSee(livingentity)) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || livingentity.distanceToSqr(this.targetX, this.targetY, this.targetZ) >= 1.0D || this.entity.getRandom().nextFloat() < 0.05F)) {
                	this.targetX = livingentity.getX();
                	this.targetY = livingentity.getY();
                    this.targetZ = livingentity.getZ();
                    this.delayCounter = 4 + this.entity.getRandom().nextInt(7);
                    if (this.canPenalize) {
                    	this.delayCounter += failedPathFindingPenalty;
                    	if (this.entity.getNavigation().getPath() != null) {
                    		PathPoint finalPathPoint = this.entity.getNavigation().getPath().getEndNode();
                    		if (finalPathPoint != null && livingentity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                    			failedPathFindingPenalty = 0;
                    		else
                    			failedPathFindingPenalty += 10;
                    	} else {
                    		failedPathFindingPenalty += 10;
                    	}
                    }
                    if (d0 > 576.0D) {
                    	this.delayCounter += 10;
                    } else if (d0 > 144.0D) {
                    	 this.delayCounter += 5;
                     }
                    if (!this.entity.getNavigation().moveTo(livingentity, this.moveSpeedAmp)) {
                    	 this.delayCounter += 15;
                     }
                }
                this.attackTick = Math.max(this.attackTick - 1, 0);
                this.checkAndPerformAttack(livingentity, d0);
    		}
    		// Strafing
    		if(d0 < (double)(this.maxAttackDistance)) {
    			if (d0 > (this.maxAttackDistance * 0.3) && flag) {
    				if (!(d0 > (double)this.maxAttackDistance) && this.seeTime >= 20) {
    					this.entity.getNavigation().stop();
    					++this.strafingTime;
    				} else {
    					this.entity.getNavigation().moveTo(livingentity, 0.8D);
    					this.strafingTime = -1;
    				}
    				if (this.strafingTime >= 20) {
    					if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
    						this.strafingClockwise = !this.strafingClockwise;
    					}
    					if ((double)this.entity.getRandom().nextFloat() < 0.3D) {
    						this.strafingBackwards = !this.strafingBackwards;
    					}
    					this.strafingTime = 0;
    				}
    				if (this.strafingTime > -1) {
    					if (d0 > (double)(this.maxAttackDistance * 0.8333333333333333F)) {
    						this.strafingBackwards = false;
    					} else if (d0 < (double)(this.maxAttackDistance * 0.6F)) {
    						this.strafingBackwards = true;
    					}
    					this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.2F : 0.4F, this.strafingClockwise ? 0.4F : -0.4F);
    					this.entity.lookAt(livingentity, 30.0F, 30.0F);
    				}else {
    					this.entity.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
    				}
    			}
    			// Fireball attack
    			if ((d0 > (this.maxAttackDistance * 0.3) && flag) || ((this.entity.getNavigation().isDone()) && (d0 < (double)(this.maxAttackDistance)) && flag)) {
    				double d1 = livingentity.getX() - this.entity.getX();
    				double d2 = livingentity.getY(0.3333333333333333D) - this.entity.getY(0.6D);
    				double d3 = livingentity.getZ() - this.entity.getZ();
    				if (this.attackTime <= 0) {
    					++this.attackStep;
    					if (this.attackStep == 1) {
    						this.attackTime = 20;
    					} else if (this.attackStep <= 2) {
    						this.attackTime = 6;
    					} else {
    						this.attackTime = 60;
    						this.attackStep = 0;
    					}
    					if (this.attackStep > 1) {
    						float f = MathHelper.sqrt(MathHelper.sqrt(d0)) * 0.5F;
    						this.entity.level.levelEvent(null, 1018, entity.blockPosition(), 0);
    						
    						for(int i = 0; i < 1; ++i) {
    							SmallFireballEntity smallfireballentity = new SmallFireballEntity(this.entity.level, this.entity, d1 * (double)f, d2, d3 * (double)f);
    							smallfireballentity.setPos(smallfireballentity.getX(), this.entity.getY(0.6D) + 0.5D, smallfireballentity.getZ());
    							this.entity.level.addFreshEntity(smallfireballentity);
    						}
    					}
    				}
    				this.entity.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
    			}
    		}
    		super.tick();
    	}
    }

    protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
		double d0 = this.getAttackReachSqr(enemy);
		if (distToEnemySqr <= d0 && this.attackTick <= 0) {
			this.attackTick = 20;
			this.entity.doHurtTarget(enemy);
		}
	}

	protected double getAttackReachSqr(LivingEntity attackTarget) {
		return this.entity.getBbWidth() * 2.0F * this.entity.getBbWidth() * 2.0F + attackTarget.getBbWidth();
	}
}
