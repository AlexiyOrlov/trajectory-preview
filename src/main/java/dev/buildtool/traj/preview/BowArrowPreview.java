package dev.buildtool.traj.preview;


import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BowArrowPreview extends Entity implements PreviewEntity<AbstractArrowEntity> {
    private boolean inGround;

    public BowArrowPreview(World level) {
        super(EntityType.ARROW, level);
    }

    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        int timeLeft = player.getUseItemRemainingTicks();
        if (timeLeft > 0) {
            int maxDuration = player.getMainHandItem().getUseDuration();
            int difference = maxDuration - timeLeft;
            float arrowVelocity = BowItem.getPowerForTime(difference);
            if (arrowVelocity >= 0.1) {
                ArrowEntity arrow = new ArrowEntity(level, player);
                arrow.shootFromRotation(player, player.xRot, player.yRot, 0, 3 * arrowVelocity, 0);
                return Collections.singletonList(arrow);
            }
        }
        return null;
    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity) {
        super.tick();
        Vector3d vector3d = this.getDeltaMovement();
        boolean flag = this.noPhysics;
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
            this.yRot = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
            this.xRot = (float) (MathHelper.atan2(vector3d.y, f) * (double) (180F / (float) Math.PI));
            this.yRotO = this.yRot;
            this.xRotO = this.xRot;
        }

        BlockPos blockpos = this.blockPosition();
        BlockState blockstate = this.level.getBlockState(blockpos);
        if (!blockstate.isAir() && !flag) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
            if (!voxelshape.isEmpty()) {
                Vector3d vec31 = this.position();

                for (AxisAlignedBB aabb : voxelshape.toAabbs()) {
                    if (aabb.move(blockpos).contains(vec31)) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.inGround && !flag) {
            remove();
        } else {
            Vector3d vector3d2 = this.position();
            Vector3d vector3d3 = vector3d2.add(vector3d);
            RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                vector3d3 = raytraceresult.getLocation();
            }

            while (!this.removed) {
                EntityRayTraceResult entityraytraceresult = simulatedEntity.findHitEntity(vector3d2, vector3d3);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult) raytraceresult).getEntity();
                    Entity entity1 = simulatedEntity.getOwner();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity) entity1).canHarmPlayer((PlayerEntity) entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag) {
                    this.hasImpulse = true;
                }

                if (entityraytraceresult == null || simulatedEntity.getPierceLevel() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            vector3d = this.getDeltaMovement();
            double d3 = vector3d.x;
            double d4 = vector3d.y;
            double d0 = vector3d.z;

            double d5 = this.getX() + d3;
            double d1 = this.getY() + d4;
            double d2 = this.getZ() + d0;
            float f1 = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
            if (flag) {
                this.yRot = (float) (MathHelper.atan2(-d3, -d0) * (double) (180F / (float) Math.PI));
            } else {
                this.yRot = (float) (MathHelper.atan2(d3, d0) * (double) (180F / (float) Math.PI));
            }

            this.xRot = (float) (MathHelper.atan2(d4, f1) * (double) (180F / (float) Math.PI));
            this.xRot = lerpRotation(this.xRotO, this.xRot);
            this.yRot = lerpRotation(this.yRotO, this.yRot);
            if (this.isInWater()) {
                remove();
            }
            float f2 = 0.99F;

            this.setDeltaMovement(vector3d.scale(f2));
            if (!this.isNoGravity() && !flag) {
                Vector3d vector3d4 = this.getDeltaMovement();
                this.setDeltaMovement(vector3d4.x, vector3d4.y - (double) 0.05F, vector3d4.z);
            }

            this.setPos(d5, d1, d2);
            this.checkInsideBlocks();
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT p_20139_) {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return new SSpawnObjectPacket(this);
    }

    protected static float lerpRotation(float p_37274_, float p_37275_) {
        while (p_37275_ - p_37274_ < -180.0F) {
            p_37274_ -= 360.0F;
        }

        while (p_37275_ - p_37274_ >= 180.0F) {
            p_37274_ += 360.0F;
        }

        return MathHelper.lerp(0.2F, p_37274_, p_37275_);
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(ITag<Fluid> p_210500_1_, double p_210500_2_) {
        return false;
    }
}
