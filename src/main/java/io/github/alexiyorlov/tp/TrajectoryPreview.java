package io.github.alexiyorlov.tp;

import io.github.alexiyorlov.tp.api.IntColor;
import io.github.alexiyorlov.tp.api.PreviewEntity;
import io.github.alexiyorlov.tp.api.PreviewPlugin;
import io.github.alexiyorlov.tp.api.PreviewProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 12/13/19.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
@Mod(TrajectoryPreview.ID)
public class TrajectoryPreview
{
    static final String ID = "trajectory_preview";
    static HashSet<PreviewProvider> previewProviders = new HashSet<>(2);
    static PType particleType;

    static ForgeConfigSpec.ConfigValue<String> primaryDotColor;
    static ForgeConfigSpec.ConfigValue<String> secondaryDotColor;
    static ForgeConfigSpec.DoubleValue pathStart;

    public TrajectoryPreview()
    {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus eventBus = context.getModEventBus();
        eventBus.register(this);
        Pair<Object,ForgeConfigSpec> pair= new ForgeConfigSpec.Builder().configure(builder -> {
            primaryDotColor=builder.define("Primary preview dot color","75aaff");
            secondaryDotColor=builder.define("Secondary preview dot color","e7ed49");
            pathStart=builder.defineInRange("Start preview after such distance",2d,1d,256d);
            return builder.build();
        });
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,pair.getRight());

        Type type = Type.getType(PreviewPlugin.class);
        Set<ModFileScanData.AnnotationData> list = ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .filter(a -> type.equals(a.getAnnotationType()))
                .collect(Collectors.toSet());
        list.forEach(annotationData -> {
//             System.out.println(annotationData.getClassType().getClassName());
            try
            {
                Class<?> plugin = Class.forName(annotationData.getClassType().getClassName());
                Object object = plugin.newInstance();
                if (object instanceof PreviewProvider)
                {
                    PreviewProvider previewProvider = (PreviewProvider) object;
                    previewProvider.prepare();
                    previewProviders.add(previewProvider);
                }
            }
            catch (ClassNotFoundException | InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        });

    }



    @SubscribeEvent
    public static void renderTrajectory(RenderGameOverlayEvent.Post renderGameOverlayEvent)
    {
        if (renderGameOverlayEvent.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
        {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPlayerEntity playerEntity = minecraft.player;
            ParticleManager particleManager = minecraft.particles;
            World world = minecraft.world;
            ItemStack itemStack = playerEntity.getHeldItemMainhand();
            Item item = itemStack.getItem();
            if (!itemStack.isEmpty())
            {
                Class<? extends PreviewEntity> previewEntity = null;
                for (PreviewProvider previewProvider : previewProviders)
                {
                    Class<? extends PreviewEntity> cl = previewProvider.getPreviewEntityFor(playerEntity, item);
                    if (cl != null)
                    {
                        previewEntity = cl;
                        break;
                    }
                }
                if (previewEntity != null)
                {
                    try
                    {
                        PreviewEntity<Entity> entity = previewEntity.getConstructor(World.class).newInstance(world);
                        Entity target = entity.initializeEntity(playerEntity, itemStack);
                        if (target != null)
                        {
                            Entity e = (Entity) entity;
                            e.setPosition(target.getPosX(), target.getPosY(), target.getPosZ());
                            e.setMotion(target.getMotion());
                            e.rotationYaw = target.rotationYaw;
                            e.rotationPitch = target.rotationPitch;
                            e.prevRotationPitch = target.prevRotationPitch;
                            e.prevRotationYaw = target.prevRotationYaw;

                            world.addEntity(e);
                            ArrayList<Vec3d> trajectory = new ArrayList<>(128);
                            short cycle = 0;
                            while (e.isAlive())
                            {
                                entity.simulateShot(target);
                                if (cycle > 512)
                                {
                                    break;
                                }
                                Vec3d newPoint=new Vec3d(e.getPosX(), e.getPosY(), e.getPosZ());
                                if(MathHelper.sqrt(playerEntity.getDistanceSq(newPoint))>pathStart.get())
                                {
                                    trajectory.add(newPoint);
                                }
                                cycle++;
                            }
                            IntColor colorFirst = new IntColor(Integer.parseInt(primaryDotColor.get(), 16));
                            IntColor color2 = new IntColor(Integer.parseInt(secondaryDotColor.get(), 16));
//                            System.out.println(colorFirst.getPackedColor());
                            float pointScale;
                            for (Vec3d vec3d : trajectory)
                            {
                                double distanceFromPlayer=Math.sqrt(playerEntity.getDistanceSq(vec3d));
                                Vec3d end=trajectory.get(trajectory.size()-1);
                                double totalDistance=Math.sqrt(playerEntity.getDistanceSq(end));
                                pointScale=(float) (distanceFromPlayer/totalDistance);
                                //possible particles: bubble
                                Particle point = particleManager.addParticle(ParticleTypes.END_ROD, vec3d.x, vec3d.y, vec3d.z, 0, 0, 0);
                                if (point != null)
                                {
                                    if (trajectory.indexOf(vec3d) % 2 == 0)
                                    {
                                        point.setColor(colorFirst.getRed() / 255f, colorFirst.getGreen() / 255f, colorFirst.getBlue() / 255f);
                                    }
                                    else
                                    {
                                        point.setColor(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f);
                                    }
                                    point.multiplyParticleScaleBy(pointScale);
                                    point.setExpired();
                                }
                            }
                        }
                    }
                    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public void onSetup(FMLClientSetupEvent clientSetupEvent)
    {

        Minecraft.getInstance().particles.registerFactory(particleType, new PathParticle.Factory());

        ModLoadingContext.get().getActiveContainer().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY,() -> (minecraft, screen) -> new ConfigScreen(new StringTextComponent(ID),screen));
    }

    @SubscribeEvent
    public void registerPaticle(RegistryEvent.Register<ParticleType<?>> registryEvent)
    {
        particleType = Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(ID, "preview_point"), new PType(false));
    }
}
