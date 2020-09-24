package dev.buildtool.tp.minecraft;

import dev.buildtool.tp.PreviewEntity;
import dev.buildtool.tp.PreviewPlugin;
import dev.buildtool.tp.PreviewProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

/**
 * Created on 2/24/18.
 */
@PreviewPlugin(mod = "minecraft")
public class BasicPlugin implements PreviewProvider
{

    @Override
    public Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(PlayerEntity player, Item shootable)
    {
        if (shootable == Items.BOW)
        {
            return BowArrowPreview.class;
        }
        else if (shootable == Items.SNOWBALL || shootable == Items.ENDER_PEARL || shootable == Items.EGG ||
                shootable == Items.SPLASH_POTION || shootable == Items.LINGERING_POTION) {
            return ThrowablePreview.class;
        } else if (shootable == Items.CROSSBOW) {
            return CrossbowProjectilePreview.class;
        } else if (shootable == Items.TRIDENT)
            return TridentPreview.class;
        return null;
    }
}
