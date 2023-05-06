package dev.buildtool.trajectory.preview;

import dev.buildtool.trajectory.preview.api.PreviewProvider;
import dev.buildtool.trajectory.preview.api.TrajectoryPlugin;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod("trajectory_preview")
public class TrajectoryPreview {
    static ForgeConfigSpec.ConfigValue<String> firstColor, secondColor;
    static ForgeConfigSpec.IntValue trajectoryStart;
    private static final Logger LOGGER = LogManager.getLogger();

    static Set<PreviewProvider> previewProviders = new HashSet<>();

    public TrajectoryPreview() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, new ForgeConfigSpec.Builder().configure(builder -> {
            firstColor = builder.define("Odd point color", "75aaff");
            secondColor = builder.define("Even point color", "e7ed49");
            trajectoryStart = builder.defineInRange("Start preview after such distance", 2, 1, 256);
            return builder.build();
        }).getRight());
        List<ModFileScanData> modFileScanData = ModList.get().getAllScanData();
        Set<String> classNames = new HashSet<>();
        modFileScanData.forEach(modFileScanData1 -> {
            modFileScanData1.getAnnotations().stream().filter(annotationData -> annotationData.annotationType().getClassName().equals(TrajectoryPlugin.class.getName())).forEach(annotationData -> {
                classNames.add(annotationData.memberName());
            });
        });
        classNames.forEach(s -> {
            try {
                Class<?> clss = Class.forName(s);
                Constructor<?> constructor = clss.getConstructor();
                PreviewProvider previewProvider = (PreviewProvider) constructor.newInstance();
                previewProviders.add(previewProvider);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        previewProviders.forEach(PreviewProvider::prepare);
        LOGGER.info("Loaded and prepared {} plugin(s)", previewProviders.size());
    }
}
