package dev.buildtool.traj.preview;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface PreviewEntity<E extends Entity> {
    /**
     * Called before simulation; create target entities here. Do not spawn any into world
     *
     * @param associatedItem item held in main hand
     * @return entities to be projected
     */
    List<E> initializeEntities(PlayerEntity player, ItemStack associatedItem);

    /**
     * Simulate a projected entity's tick here - generally, copy-paste relevant code from {@link Entity#tick()} method.
     * This method will be called until the entity is 'dead'
     *
     * @param simulatedEntity projectile
     */
    void simulateShot(E simulatedEntity);
}
