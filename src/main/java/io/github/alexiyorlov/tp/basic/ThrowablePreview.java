package io.github.alexiyorlov.tp.basic;

import io.github.alexiyorlov.tp.api.InvisibleEntity;
import io.github.alexiyorlov.tp.api.PreviewEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

/**
 * Created on 2/25/18.
 */
@InvisibleEntity
public class ThrowablePreview extends Entity implements PreviewEntity<ThrowableEntity>
{
    protected boolean inGround;
    protected Entity ignoreEntity, shooter;
    protected int ignoreTime;
    public ThrowablePreview(World worldIn)
    {
        super(EntityType.SNOWBALL, worldIn);
    }

    @Override
    public ThrowableEntity initializeEntity(PlayerEntity player, ItemStack associatedItem)
    {
        Item item = associatedItem.getItem();
        if (item == Items.SNOWBALL)
        {
            shooter = player;
            SnowballEntity entitySnowball = new SnowballEntity(world, player);
            entitySnowball.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5f, 0);
            return entitySnowball;
        }
        else if (item == Items.EGG)
        {
            shooter = player;
            EggEntity entityEgg = new EggEntity(world, player);
            entityEgg.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5f, 0);
            return entityEgg;
        }
        else if (item == Items.ENDER_PEARL)
        {
            EnderPearlEntity entityEnderPearl = new EnderPearlEntity(world, player);
            shooter = player;
            entityEnderPearl.shoot(player, player.rotationPitch, player.rotationYaw, 0, 1.5f, 0);
            return entityEnderPearl;
        }
        else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION)
        {
            PotionEntity potionEntity = new PotionEntity(world, player);
            shooter = player;
            potionEntity.shoot(player, player.rotationPitch, player.rotationYaw, -20, 0.5f, 0);
            return potionEntity;
        }
        return null;
    }

    @Override
    public void simulateShot(ThrowableEntity simulatedEntity)
    {
        this.lastTickPosX = this.getPosX();
        this.lastTickPosY = this.getPosY();
        this.lastTickPosZ = this.getPosZ();
        super.tick();

        if (this.inGround)
        {
            remove();
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox().expand(this.getMotion()).grow(1.0D);

        for (Entity entity : this.world.getEntitiesInAABBexcluding(this, axisalignedbb, (p_213881_0_) -> !p_213881_0_.isSpectator() && p_213881_0_.canBeCollidedWith()))
        {
            if (entity == this.ignoreEntity)
            {
                ++this.ignoreTime;
                break;
            }

            if (simulatedEntity.getThrower() != null && this.ticksExisted < 2 && this.ignoreEntity == null)
            {
                this.ignoreEntity = entity;
                this.ignoreTime = 3;
                break;
            }
        }

        RayTraceResult raytraceresult = ProjectileHelper.rayTrace(this, axisalignedbb, (p_213880_1_) -> !p_213880_1_.isSpectator() && p_213880_1_.canBeCollidedWith() && p_213880_1_ != this.ignoreEntity, RayTraceContext.BlockMode.OUTLINE, true);
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0)
        {
            this.ignoreEntity = null;
        }

        if (raytraceresult.getType() != RayTraceResult.Type.MISS)
        {
            remove();
        }

        Vec3d vec3d = this.getMotion();

        this.setPosition(this.getPosX()+vec3d.x, this.getPosY()+vec3d.y, this.getPosZ()+vec3d.z);
        float f = MathHelper.sqrt(horizontalMag(vec3d));
        this.rotationYaw = (float) (MathHelper.atan2(vec3d.x, vec3d.z) * (double) (180F / (float) Math.PI));

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
        float f1;
        if (this.isInWater())
        {
            f1 = 0.8F;
        }
        else
        {
            f1 = 0.99F;
        }

        this.setMotion(vec3d.scale(f1));
        if (!this.hasNoGravity())
        {
            Vec3d vec3d1 = this.getMotion();
            // this is net.minecraft.entity.projectile.ThrowableEntity.getGravityVelocity()
            double yy;
            if (simulatedEntity instanceof PotionEntity)
            {
                yy = 0.05f;
            }
            else
            {
                yy = 0.03f;
            }
            this.setMotion(vec3d1.x, vec3d1.y - yy, vec3d1.z);
        }

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
