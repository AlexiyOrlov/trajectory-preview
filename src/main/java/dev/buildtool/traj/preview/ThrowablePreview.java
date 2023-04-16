package dev.buildtool.traj.preview;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ThrowablePreview extends Entity implements PreviewEntity<ThrowableEntity> {
    public ThrowablePreview(World p_19871_) {
        super(EntityType.SNOWBALL, p_19871_);
    }

    @Override
    public List<ThrowableEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        Item item = associatedItem.getItem();
        if (item instanceof SnowballItem) {
            SnowballEntity snowball = new SnowballEntity(level, player);
            snowball.shootFromRotation(player, player.xRot, player.yRot, 0, 1.5f, 0);
            return Collections.singletonList(snowball);
        } else if (item instanceof EggItem) {
            EggEntity egg = new EggEntity(level, player);
            egg.shootFromRotation(player, player.xRot, player.yRot, 0, 1.5f, 0);
            return Collections.singletonList(egg);
        } else if (item instanceof EnderPearlItem) {
            EnderPearlEntity thrownEnderpearl = new EnderPearlEntity(level, player);
            thrownEnderpearl.shootFromRotation(player, player.xRot, player.yRot, 0, 1.5f, 0);
            return Collections.singletonList(thrownEnderpearl);
        } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            PotionEntity thrownPotion = new PotionEntity(level, player);
            thrownPotion.shootFromRotation(player, player.xRot, player.yRot, -20, 0.5f, 0);
            return Collections.singletonList(thrownPotion);
        } else if (item instanceof ExperienceBottleItem) {
            ExperienceBottleEntity experienceBottle = new ExperienceBottleEntity(level, player);
            experienceBottle.shootFromRotation(player, player.xRot, player.yRot, -20, 0.7f, 0);
            return Collections.singletonList(experienceBottle);
        }
        return null;
    }

    @Override
    public void simulateShot(ThrowableEntity simulatedEntity) {
        super.tick();
        RayTraceResult hitresult = ProjectileHelper.getHitResult(this, entity -> !entity.isSpectator() && entity.isAlive() && entity.isPickable());
        boolean flag = false;
        if (hitresult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult) hitresult).getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.is(Blocks.NETHER_PORTAL)) {
                remove();
            } else if (blockstate.is(Blocks.END_GATEWAY)) {
                remove();
            }
        }

        if (hitresult.getType() != RayTraceResult.Type.MISS) {
            remove();
        }

        this.checkInsideBlocks();
        Vector3d vec3 = this.getDeltaMovement();
        double d2 = this.getX() + vec3.x;
        double d0 = this.getY() + vec3.y;
        double d1 = this.getZ() + vec3.z;
        float f = 0.99f;
        if (this.isInWater()) {
            remove();
        }

        this.setDeltaMovement(vec3.scale(f));
        if (!this.isNoGravity()) {
            Vector3d vec31 = this.getDeltaMovement();
            this.setDeltaMovement(vec31.x, vec31.y - getGravity(simulatedEntity), vec31.z);
        }

        this.setPos(d2, d0, d1);
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

    private float getGravity(Entity simulated) {
        if (simulated instanceof PotionEntity)
            return 0.05f;
        else if (simulated instanceof ExperienceBottleEntity)
            return 0.07f;
        return 0.03f;
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(ITag<Fluid> p_210500_1_, double p_210500_2_) {
        return false;
    }
}
