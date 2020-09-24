package dev.buildtool.tp;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

/**
 *
 */
public interface PreviewProvider
{
    /**
     * Is called once before everything else
     */
    default void prepare()
    {

    }

    /**
     * @param shootable selected shootable or throwable item
     * @return entity for appropriate ammo type
     */
    Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(PlayerEntity player, Item shootable);
}
