package dev.buildtool.traj.preview;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tags.ITag;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CrossbowArrowPreview extends Entity implements PreviewEntity<AbstractArrowEntity> {
    private boolean inGround;

    public CrossbowArrowPreview(World level) {
        super(EntityType.ARROW, level);
    }

    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof CrossbowItem) {
            if (CrossbowItem.isCharged(associatedItem)) {
                List<ItemStack> chargedArrows = getChargedProjectiles(associatedItem);
                if (chargedArrows.size() > 0) {
                    List<AbstractArrowEntity> arrows = new ArrayList<>(chargedArrows.size());
                    for (int i = 0; i < chargedArrows.size(); i++) {
                        AbstractArrowEntity arrow = getArrow(level, player, associatedItem, chargedArrows.get(i));
                        Vector3d vec31 = player.getUpVector(1.0F);
                        Quaternion quaternion;
                        if (i == 0) {
                            quaternion = new Quaternion(new Vector3f(vec31), 0, true);
                        } else if (i == 1) {
                            quaternion = new Quaternion(new Vector3f(vec31), -10, true);
                        } else {
                            quaternion = new Quaternion(new Vector3f(vec31), 10, true);
                        }
                        Vector3d vec3 = player.getViewVector(1.0F);
                        Vector3f vector3f = new Vector3f(vec3);
                        vector3f.transform(quaternion);
                        arrow.shoot(vector3f.x(), vector3f.y(), vector3f.z(), 3.15f, 0);
                        arrows.add(arrow);
                    }
                    return arrows;
                }
            }
        }
        return null;
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack p_40942_) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundNBT compoundtag = p_40942_.getTag();
        if (compoundtag != null && compoundtag.contains("ChargedProjectiles", 9)) {
            ListNBT listtag = compoundtag.getList("ChargedProjectiles", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundNBT compoundtag1 = listtag.getCompound(i);
                list.add(ItemStack.of(compoundtag1));
            }
        }

        return list;
    }

    private static AbstractArrowEntity getArrow(World p_40915_, LivingEntity p_40916_, ItemStack crossbow, ItemStack arrows) {
        ArrowItem arrowitem = (ArrowItem) (arrows.getItem() instanceof ArrowItem ? arrows.getItem() : Items.ARROW);
        AbstractArrowEntity abstractarrow = arrowitem.createArrow(p_40915_, arrows, p_40916_);
        if (p_40916_ instanceof PlayerEntity) {
            abstractarrow.setCritArrow(true);
        }

        abstractarrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        abstractarrow.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            abstractarrow.setPierceLevel((byte) i);
        }

        return abstractarrow;
    }

    @Override
    public void simulateShot(AbstractArrowEntity simulatedEntity) {
        super.tick();
        boolean flag = noPhysics;
        Vector3d vector3d = this.getDeltaMovement();
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
            this.yRot = (float) (MathHelper.atan2(vector3d.x, vector3d.z) * (double) (180F / (float) Math.PI));
            this.xRot = (float) (MathHelper.atan2(vector3d.y, (double) f) * (double) (180F / (float) Math.PI));
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

        if (this.isInWaterOrRain() || blockstate.is(Blocks.SNOW)) {
            this.clearFire();
        }

        if (this.inGround && !flag) {
            remove();
        } else {
            Vector3d vec32 = this.position();
            Vector3d vec33 = vec32.add(vector3d);
            RayTraceResult hitresult = this.level.clip(new RayTraceContext(vec32, vec33, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (hitresult.getType() != RayTraceResult.Type.MISS) {
                vec33 = hitresult.getLocation();
            }

            while (!this.removed) {
                EntityRayTraceResult entityhitresult = simulatedEntity.findHitEntity(vec32, vec33);
                if (entityhitresult != null) {
                    hitresult = entityhitresult;
                }

                if (hitresult != null && hitresult.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult) hitresult).getEntity();
                    Entity entity1 = simulatedEntity.getOwner();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity) entity1).canHarmPlayer((PlayerEntity) entity)) {
                        hitresult = null;
                        entityhitresult = null;
                    }
                }

                if (hitresult != null && hitresult.getType() != RayTraceResult.Type.MISS && !flag) {
                    this.hasImpulse = true;
                }

                if (entityhitresult == null || simulatedEntity.getPierceLevel() <= 0) {
                    break;
                }

                hitresult = null;
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

            this.xRot = (float) (MathHelper.atan2(d4, (double) f1) * (double) (180F / (float) Math.PI));
            this.xRot = lerpRotation(this.xRotO, this.xRot);
            this.yRot = lerpRotation(this.yRotO, this.yRot);
            float f = 0.99F;
            if (this.isInWater()) {
                remove();
            }

            this.setDeltaMovement(vector3d.scale(f));
            if (!this.isNoGravity() && !flag) {
                Vector3d vec34 = this.getDeltaMovement();
                this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
            }

            this.setPos(d5, d1, d2);
            this.checkInsideBlocks();
        }
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

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(ITag<Fluid> p_210500_1_, double p_210500_2_) {
        return false;
    }
}
