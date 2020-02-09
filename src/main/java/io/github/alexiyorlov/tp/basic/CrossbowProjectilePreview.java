package io.github.alexiyorlov.tp.basic;

import com.google.common.collect.Lists;
import io.github.alexiyorlov.tp.api.PreviewEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created on 12/18/19.
 */
public class CrossbowProjectilePreview extends Entity implements PreviewEntity<AbstractArrowEntity>
{
    public CrossbowProjectilePreview(World worldIn)
    {
        super(EntityType.ARROW, worldIn);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbow)
    {
        List<ItemStack> list = Lists.newArrayList();
        CompoundNBT compoundnbt = crossbow.getTag();
        if (compoundnbt != null && compoundnbt.contains("ChargedProjectiles", 9))
        {
            ListNBT listnbt = compoundnbt.getList("ChargedProjectiles", 10);
            if (listnbt != null)
            {
                for (int i = 0; i < listnbt.size(); ++i)
                {
                    CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                    list.add(ItemStack.read(compoundnbt1));
                }
            }
        }
        return list;
    }

    private static AbstractArrowEntity createArrow(World worldIn, LivingEntity shooter, ItemStack crossbow, ItemStack ammo)
    {
        ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);
        AbstractArrowEntity abstractarrowentity = arrowitem.createArrow(worldIn, ammo, shooter);
        if (shooter instanceof PlayerEntity)
        {
            abstractarrowentity.setIsCritical(true);
        }

        abstractarrowentity.setHitSound(SoundEvents.ITEM_CROSSBOW_HIT);
        abstractarrowentity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (i > 0)
        {
            abstractarrowentity.setPierceLevel((byte) i);
        }

        return abstractarrowentity;
    }

    @Override
    public AbstractArrowEntity initializeEntity(PlayerEntity player, ItemStack associatedItem)
    {
        if (associatedItem.getItem() instanceof CrossbowItem)
        {
            if (CrossbowItem.isCharged(associatedItem))
            {
                List<ItemStack> allProjectiles = getChargedProjectiles(associatedItem);
                if (allProjectiles.size() == 1 && allProjectiles.get(0).getItem() instanceof ArrowItem)
                {
                    AbstractArrowEntity abstractArrowEntity = createArrow(world, player, associatedItem, new ItemStack(Items.ARROW));
                    Vec3d vec3d1 = player.getLook(1.0F);
                    //0, -10, 10 for all projectiles
                    Quaternion quaternion = new Quaternion(new Vector3f(vec3d1), 0, true);
                    Vec3d vec3d = player.getLook(1.0F);
                    Vector3f vector3f = new Vector3f(vec3d);
                    vector3f.transform(quaternion);
                    float velocity = 3.15f;
//                1.6F for firework rocket; 3.15F for arrow
                    abstractArrowEntity.shoot(vector3f.getX(), vector3f.getY(), vector3f.getZ(), velocity, 0);
                    return abstractArrowEntity;
                }
            }
        }
        return null;
    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity)
    {
        super.tick();
        boolean flag = simulatedEntity.getNoClip();
        Vec3d vec3d = this.getMotion();
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(horizontalMag(vec3d));
            this.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (double) (180F / (float) Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }

        BlockPos blockpos = new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
        BlockState blockstate = this.world.getBlockState(blockpos);
        if (!blockstate.isAir(this.world, blockpos) && !flag)
        {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
            if (!voxelshape.isEmpty())
            {
                for (AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList())
                {
                    if (axisalignedbb.offset(blockpos).contains(new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ())))
                    {
                        remove();
                        return;
                    }
                }
            }
        }



            Vec3d vec3d1 = new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ());
            Vec3d vec3d2 = vec3d1.add(vec3d);
            RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vec3d1, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS)
            {
                vec3d2 = raytraceresult.getHitVec();
            }

            while (isAlive())
            {
                EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(this.world, this, vec3d, vec3d2, simulatedEntity.getBoundingBox().expand(this.getMotion()).grow(1.0D), (p_213871_1_) -> !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.canBeCollidedWith() && (p_213871_1_ != simulatedEntity.getShooter())); /*|| simulatedEntity.ticksInAir >= 5) && (simulatedEntity.piercedEntities == null || !simulatedEntity.piercedEntities.contains(p_213871_1_.getEntityId())))*/

                if (entityraytraceresult != null)
                {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY)
                {
                    Entity entity = ((EntityRayTraceResult) raytraceresult).getEntity();
                    Entity entity1 = simulatedEntity.getShooter();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity) entity1).canAttackPlayer((PlayerEntity) entity))
                    {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && !flag)
                {
//                    remove();
                    this.isAirBorne = true;
                }

                if (entityraytraceresult == null || simulatedEntity.getPierceLevel() <= 0)
                {
                    break;
                }

                raytraceresult = null;
            }

            vec3d = this.getMotion();
            double d1 = vec3d.x;
            double d2 = vec3d.y;
            double d0 = vec3d.z;
//            if (simulatedEntity.getIsCritical()) {
//                for(int i = 0; i < 4; ++i) {
//                    this.world.addParticle(ParticleTypes.CRIT, this.posX + d1 * (double)i / 4.0D, this.posY + d2 * (double)i / 4.0D, this.posZ + d0 * (double)i / 4.0D, -d1, -d2 + 0.2D, -d0);
//                }
//            }
            this.setPosition(this.getPosX()+d1, this.getPosY()+d2, this.getPosZ()+d0);

            float f4 = MathHelper.sqrt(horizontalMag(vec3d));
            if (flag)
            {
                this.rotationYaw = (float) (MathHelper.atan2(-d1, -d0) * (double) (180F / (float) Math.PI));
            }
            else
            {
                this.rotationYaw = (float) (MathHelper.atan2(d1, d0) * (double) (180F / (float) Math.PI));
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

            this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
            this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
            float f1 = 0.99F;
            if (this.isInWater())
            {
//                for(int j = 0; j < 4; ++j) {
//                    float f3 = 0.25F;
//                    this.world.addParticle(ParticleTypes.BUBBLE, this.posX - d1 * f3, this.posY - d2 * 0.25D, this.posZ - d0 *f3, d1, d2, d0);
//                }
                f1 = 0.6f;// simulatedEntity.getWaterDrag();
            }

            this.setMotion(vec3d.scale(f1));
            if (!this.hasNoGravity() && !flag)
            {
                Vec3d vec3d3 = this.getMotion();
                this.setMotion(vec3d3.x, vec3d3.y - (double) 0.05F, vec3d3.z);
            }

            this.doBlockCollisions();

    }

    @Override
    protected void registerData()
    {

    }

    @Override
    protected void readAdditional(CompoundNBT compound)
    {

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
}
