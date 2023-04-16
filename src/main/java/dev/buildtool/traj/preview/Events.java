package dev.buildtool.traj.preview;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class Events {

    static Set<PreviewProvider> previewProviders = ImmutableSet.of(new BasicPlugin());
    static boolean render = true;

    @SubscribeEvent
    public static void drawTrajectory(RenderGameOverlayEvent.Post event) {
        if (render) {
            Minecraft minecraft = Minecraft.getInstance();
            ParticleManager particleEngine = minecraft.particleEngine;
            PlayerEntity player = minecraft.player;
            assert player != null;
            World level = player.level;
            ItemStack itemStack = player.getMainHandItem();
            Item item = itemStack.getItem();
            if (!itemStack.isEmpty()) {
                for (PreviewProvider previewProvider : previewProviders) {
                    Class<? extends PreviewEntity<?>> previewEntityClass = previewProvider.getPreviewEntityFor(player, item);
                    if (previewEntityClass != null) {
                        try {
                            PreviewEntity<Entity> previewEntity = (PreviewEntity<Entity>) previewEntityClass.getConstructor(World.class).newInstance(level);
                            List<Entity> targets = previewEntity.initializeEntities(player, itemStack);
                            if (targets != null) {
                                for (Entity target : targets) {
                                    previewEntity = (PreviewEntity<Entity>) previewEntityClass.getConstructor(World.class).newInstance(level);
                                    Entity entity = (Entity) previewEntity;
                                    entity.setPos(target.getX(), target.getY(), target.getZ());
                                    entity.setDeltaMovement(target.getDeltaMovement());
                                    entity.xRot = target.xRot;
                                    entity.yRot = target.yRot;
                                    level.addFreshEntity(entity);
                                    ArrayList<Vector3d> trajectory = new ArrayList<>(128);
                                    short cycle = 0;
                                    while (entity.isAlive()) {
                                        previewEntity.simulateShot(target);
                                        Vector3d newPoint = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
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
                                    for (Vector3d vec3 : trajectory) {
                                        double distanceFromPlayer = Math.sqrt(player.distanceToSqr(vec3));
                                        Vector3d end = trajectory.get(trajectory.size() - 1);
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

    @SubscribeEvent
    public static void keyEvents(InputEvent.KeyInputEvent keyInputEvent) {
        if (keyInputEvent.getAction() == GLFW.GLFW_RELEASE && keyInputEvent.getKey() == ClientModSetup.keyMapping.getKey().getValue()) {
            render = !render;
        }
    }
}
