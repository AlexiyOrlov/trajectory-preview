package dev.buildtool.traj.preview;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

public class BasicPlugin implements PreviewProvider {
    @Override
    public Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(PlayerEntity player, Item shootable) {
        if (shootable instanceof BowItem)
            return BowArrowPreview.class;
        else if (shootable instanceof CrossbowItem)
            return CrossbowArrowPreview.class;
        else if (shootable instanceof SnowballItem || shootable instanceof SplashPotionItem || shootable instanceof LingeringPotionItem || shootable instanceof ExperienceBottleItem || shootable instanceof EggItem || shootable instanceof EnderPearlItem)
            return ThrowablePreview.class;
        else if (shootable instanceof TridentItem)
            return TridentPreview.class;
        return null;
    }
}
