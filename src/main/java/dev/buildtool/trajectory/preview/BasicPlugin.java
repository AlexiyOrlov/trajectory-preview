package dev.buildtool.trajectory.preview;

import dev.buildtool.trajectory.preview.api.PreviewEntity;
import dev.buildtool.trajectory.preview.api.PreviewProvider;
import dev.buildtool.trajectory.preview.api.TrajectoryPlugin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/**
 * Main plugin
 */
@TrajectoryPlugin
public class BasicPlugin implements PreviewProvider {
    @Override
    public Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(Player player, Item shootable) {
        if (shootable instanceof BowItem)
            return BowArrowPreview.class;
        else if (shootable instanceof CrossbowItem)
            return CrossbowArrowPreview.class;
        else if (shootable instanceof SnowballItem || shootable instanceof SplashPotionItem || shootable instanceof LingeringPotionItem || shootable instanceof ExperienceBottleItem || shootable instanceof EggItem || shootable instanceof EnderpearlItem)
            return ThrowablePreview.class;
        else if (shootable instanceof TridentItem)
            return TridentPreview.class;
        return null;
    }
}
