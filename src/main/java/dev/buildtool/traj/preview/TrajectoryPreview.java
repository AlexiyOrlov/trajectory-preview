package dev.buildtool.traj.preview;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("trajectory_preview")
public class TrajectoryPreview {
    static ForgeConfigSpec.ConfigValue<String> firstColor, secondColor;
    static ForgeConfigSpec.IntValue trajectoryStart;
    private static final Logger LOGGER = LogManager.getLogger();

    public TrajectoryPreview() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, new ForgeConfigSpec.Builder().configure(builder -> {
            firstColor = builder.define("Odd point color", "75aaff");
            secondColor = builder.define("Even point color", "e7ed49");
            trajectoryStart = builder.defineInRange("Start preview after such distance", 2, 1, 256);
            return builder.build();
        }).getRight());
    }
}
