package dev.buildtool.trajectory.preview;

import dev.buildtool.trajectory.preview.api.PreviewEntity;
import dev.buildtool.trajectory.preview.api.PreviewProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class Events {
    static boolean render = true;
    static int counter;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void drawTrajectory(RenderGuiOverlayEvent.Post event) {
        if (render) {
            Minecraft minecraft = Minecraft.getInstance();
            ParticleEngine particleEngine = minecraft.particleEngine;
            Player player = minecraft.player;
            assert player != null;
            Level level = player.level();
            drawPreview(player, level, particleEngine, EquipmentSlot.MAINHAND);
            drawPreview(player, level, particleEngine, EquipmentSlot.OFFHAND);
            counter++;
            if (counter >= 60)
                counter = 0;
        }
    }

    private static void drawPreview(Player player, Level level, ParticleEngine particleEngine, EquipmentSlot hand) {
        ItemStack itemStack = player.getItemBySlot(hand);
        if (!itemStack.isEmpty() && counter == 0) {
            for (PreviewProvider previewProvider : TrajectoryPreview.previewProviders) {
                Class<? extends PreviewEntity<?>> previewEntityClass = previewProvider.getPreviewEntityFor(player, itemStack.getItem());
                if (previewEntityClass != null) {
                    try {
                        PreviewEntity<Entity> previewEntity = (PreviewEntity<Entity>) previewEntityClass.getConstructor(Level.class).newInstance(level);
                        List<Entity> targets = previewEntity.initializeEntities(player, itemStack, hand);
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
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
    }

    @SubscribeEvent
    public static void keyEvents(InputEvent.Key keyInputEvent) {

        if (keyInputEvent.getAction() == GLFW.GLFW_RELEASE && keyInputEvent.getKey() == ClientModSetup.keyMapping.getKey().getValue()) {
            render = !render;
        }
    }
}
