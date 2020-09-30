package io.github.alexiyorlov.tp.basic;

import io.github.alexiyorlov.tp.api.InvisibleEntity;
import io.github.alexiyorlov.tp.api.PreviewEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created on 2/24/18.
 */
@InvisibleEntity
public class BowArrowPreview extends Entity implements PreviewEntity<AbstractArrowEntity>
{
    protected Entity shooter;
    private boolean inGround;
    public BowArrowPreview(World worldIn)
    {
        super(EntityType.ARROW, worldIn);
    }

    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        int timeleft = player.getItemInUseCount();
        if (timeleft > 0) {
            int maxduration = player.getHeldItemMainhand().getUseDuration();
            int difference = maxduration - timeleft;
            float arrowVelocity = BowItem.getArrowVelocity(difference);
            if (arrowVelocity >= 0.1) {
                ArrowEntity entityArrow = new ArrowEntity(world, player);
                //inaccuracy always 0
                entityArrow.shoot(player, player.rotationPitch, player.rotationYaw, 0, 3 * arrowVelocity, 0);
                shooter = player;
                return Collections.singletonList(entityArrow);
            }
        }
        return null;
    }

    /**
     * @return motion modifier in water
     */
    protected float waterDrag() {
        return 0.6f;
    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity) {
        super.tick();
        final Vec3d motion = getMotion();
        double value = motion.x * motion.x + motion.z * motion.z;
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(value);
            this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(motion.y, f) * (180D / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        BlockPos blockpos = new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
        BlockState iblockstate = this.world.getBlockState(blockpos);

        if (!iblockstate.isAir(this.world, blockpos))
        {
            VoxelShape voxelshape = iblockstate.getCollisionShape(this.world, blockpos);
            if (!voxelshape.isEmpty())
            {
                for (AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList())
                {
                    if (axisalignedbb.offset(blockpos).contains(new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ())))
                    {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.inGround)
        {

            this.remove();

        }
        else
        {
            Vec3d vec3d1 = new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ());
            Vec3d vec3d = new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ()).add(motion);
            BlockRayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vec3d1, vec3d, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));

            vec3d1 = new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ());
            vec3d = new Vec3d(this.getPosX() + motion.x, this.getPosY() + motion.y, this.getPosZ() + motion.z);

            if (raytraceresult.getType() != RayTraceResult.Type.MISS)
            {
                vec3d = new Vec3d(raytraceresult.getHitVec().x, raytraceresult.getHitVec().y, raytraceresult.getHitVec().z);
            }

            Entity entity = this.findEntityOnPath(vec3d1, vec3d);

            if (entity instanceof PlayerEntity)
            {
                PlayerEntity entityplayer = (PlayerEntity) entity;

                if (shooter instanceof PlayerEntity && !((PlayerEntity) shooter).canAttackPlayer(entityplayer))
                {
                    raytraceresult = null;
                }
            }

            if (entity != null)
            {
                remove();
            }

            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS)
            {
                remove();
            }

            this.setPosition(this.getPosX()+motion.x, this.getPosY()+motion.y, this.getPosZ()+motion.z);

            float f4 = MathHelper.sqrt(value);
            this.rotationYaw = (float) (MathHelper.atan2(motion.x, motion.z) * (180D / Math.PI));

            this.rotationPitch = (float) (MathHelper.atan2(motion.y, f4) * (180D / Math.PI));
            while (this.rotationPitch - this.prevRotationPitch < -180.0F)
            {
                this.prevRotationPitch -= 360.0F;
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
            {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F)
            {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
            {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
            float f1 = 0.99F;
            if (this.isInWater())
            {

                f1 = waterDrag();
            }

            setMotion(motion.x * f1, motion.y * f1, motion.z * f1);

            if (!this.hasNoGravity())
            {
                addVelocity(0, -0.05000000074505806D, 0);
            }

            this.doBlockCollisions();
        }


    }

    @Nullable
    private Entity findEntityOnPath(Vec3d start, Vec3d end)
    {
        Entity entity = null;
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox().expand(getMotion()).grow(1.0D), entity1 -> !entity1.isSpectator() && entity1.isAlive() && entity1.canBeCollidedWith());
        double d0 = 0;
        for (Entity entity1 : list)
        {
            if (entity1 != shooter)
            {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(0.30000001192092896D);
                Optional<Vec3d> raytraceresult = axisalignedbb.rayTrace(start, end);

                if (raytraceresult.isPresent())
                {
                    double d1 = start.squareDistanceTo(raytraceresult.get());

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }

        return entity;
    }


    @Override
    protected void writeAdditional(CompoundNBT compound)
    {

    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return new SSpawnObjectPacket(this);
    }

    @Override
    protected void registerData()
    {

    }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {

    }
}
