package dev.buildtool.traj.preview;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class TridentPreview extends BowArrowPreview {

    public TridentPreview(World level) {
        super(level);
    }

    @Override
    public List<AbstractArrowEntity> initializeEntities(PlayerEntity player, ItemStack associatedItem) {
        if (associatedItem.getItem() instanceof TridentItem) {
            int timeLeft = player.getUseItemRemainingTicks();
            if (timeLeft > 0) {
                int maxDuration = player.getMainHandItem().getUseDuration();
                int difference = maxDuration - timeLeft;
                if (difference >= 10) {
                    TridentEntity trident = new TridentEntity(level, player, associatedItem);
                    trident.shootFromRotation(player, player.xRot, player.yRot, 0.0F, 2.5F + EnchantmentHelper.getRiptide(associatedItem) * 0.5F, 0);
                    return Collections.singletonList(trident);
                }
            }
        }
        return null;
    }
}
