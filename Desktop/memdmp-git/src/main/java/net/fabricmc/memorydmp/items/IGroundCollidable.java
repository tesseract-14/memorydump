package net.fabricmc.memorydmp.items;

import net.minecraft.entity.ItemEntity;

public interface IGroundCollidable {
    void onGroundCollision(ItemEntity itemEntity);
}