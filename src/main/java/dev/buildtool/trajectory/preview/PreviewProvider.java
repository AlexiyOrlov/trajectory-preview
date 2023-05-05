package dev.buildtool.trajectory.preview;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public interface PreviewProvider {
    /**
     * Is called once before everything else
     */
    default void prepare() {
    }

    /**
     * @param shootable selected shootable or throwable item
     * @return entity for appropriate ammo type
     */
    Class<? extends PreviewEntity<? extends Entity>> getPreviewEntityFor(Player player, Item shootable);
}
