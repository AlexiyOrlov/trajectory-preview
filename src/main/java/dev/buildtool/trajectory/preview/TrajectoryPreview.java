package dev.buildtool.trajectory.preview;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("trajectory_preview")
public class TrajectoryPreview {
    static ForgeConfigSpec.ConfigValue<String> firstColor, secondColor;
    static ForgeConfigSpec.IntValue trajectoryStart;
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public TrajectoryPreview() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, new ForgeConfigSpec.Builder().configure(builder -> {
            firstColor = builder.define("Odd point color", "75aaff");
            secondColor = builder.define("Even point color", "e7ed49");
            trajectoryStart = builder.defineInRange("Start preview after such distance", 2, 1, 256);
            return builder.build();
        }).getRight());
    }

    private void setup(final FMLCommonSetupEvent event) {

    }
}
