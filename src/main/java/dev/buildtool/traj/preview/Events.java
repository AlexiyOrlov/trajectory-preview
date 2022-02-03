package dev.buildtool.traj.preview;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class Events {

    static Set<PreviewProvider> previewProviders = ImmutableSet.of(new BasicPlugin());

    @SubscribeEvent
    public static void drawTrajectory(RenderGameOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ParticleEngine particleEngine = minecraft.particleEngine;
        Player player = minecraft.player;
        assert player != null;
        Level level = player.level;
        ItemStack itemStack = player.getMainHandItem();
        Item item = itemStack.getItem();
        if (!itemStack.isEmpty()) {
            for (PreviewProvider previewProvider : previewProviders) {
                Class<? extends PreviewEntity<?>> previewEntityClass = previewProvider.getPreviewEntityFor(player, item);
                if (previewEntityClass != null) {
                    try {
                        PreviewEntity<Entity> previewEntity = (PreviewEntity<Entity>) previewEntityClass.getConstructor(Level.class).newInstance(level);
                        List<Entity> targets = previewEntity.initializeEntities(player, itemStack);
                        if (targets != null) {
                            for (Entity target : targets) {
                                previewEntity = (PreviewEntity<Entity>) previewEntityClass.getConstructor(Level.class).newInstance(level);
                                Entity entity = (Entity) previewEntity;
                                entity.setPos(target.position());
                                entity.setDeltaMovement(target.getDeltaMovement());
                                entity.setXRot(target.getXRot());
                                entity.setYRot(target.getYRot());
                                level.addFreshEntity(entity);
                                ArrayList<Vec3> trajectory = new ArrayList<>(128);
                                short cycle = 0;
                                while (entity.isAlive()) {
                                    previewEntity.simulateShot(target);
                                    Vec3 newPoint = new Vec3(entity.getX(), entity.getY(), entity.getZ());
                                    if (Math.sqrt(player.distanceToSqr(newPoint)) > TrajectoryPreview.trajectoryStart.get()) {
                                        trajectory.add(newPoint);
                                    }
                                    cycle++;
                                    if (cycle > 512)
                                        break;
                                }
                                IntegerColor first = new IntegerColor(Integer.parseInt(TrajectoryPreview.firstColor.get(), 16));
                                IntegerColor second = new IntegerColor(Integer.parseInt(TrajectoryPreview.secondColor.get(), 16));
                                double pointScale;
                                for (Vec3 vec3 : trajectory) {
                                    double distanceFromPlayer = Math.sqrt(player.distanceToSqr(vec3));
                                    Vec3 end = trajectory.get(trajectory.size() - 1);
                                    double totalDistance = Math.sqrt(player.distanceToSqr(end));
                                    pointScale = distanceFromPlayer / totalDistance;
                                    Particle particle = particleEngine.createParticle(ParticleTypes.END_ROD, vec3.x, vec3.y, vec3.z, 0, 0, 0);
                                    if (particle != null) {
                                        if (trajectory.indexOf(vec3) % 2 == 0) {
                                            particle.setColor(first.getRed() / 255f, first.getGreen() / 255f, first.getBlue() / 255f);
                                        } else {
                                            particle.setColor(second.getRed() / 255f, second.getGreen() / 255f, second.getBlue() / 255f);
                                        }
                                        particle.scale((float) pointScale);
                                        particle.remove();
                                    }
                                }
                            }

                        }
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
    }
}