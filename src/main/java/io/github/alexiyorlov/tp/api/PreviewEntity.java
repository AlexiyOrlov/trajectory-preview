package io.github.alexiyorlov.tp.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Implement on an entity that will simulate projectile(s). It must have a default constructor (World)
 */
public interface PreviewEntity<E extends Entity>
{

    /**
     * Called before simulation; create target entity here. Do not spawn any into world
     *
     * @param associatedItem item held in main hand
     * @return entity to be projected
     */
    E initializeEntity(PlayerEntity player, ItemStack associatedItem);

    /**
     * Simulate a projected entity's tick here - generally, copy-paste relevant code from {@link Entity#tick()} method.
     * This method will be called until the entity is 'dead'
     *
     * @param simulatedEntity projectile
     */
    void simulateShot(E simulatedEntity);

}
