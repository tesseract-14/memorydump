package net.fabricmc.memorydmp.items.OrbitalTNTCannons;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;

import java.util.Map;
import java.util.WeakHashMap;

public class StabShotTracker {
    // WeakHashMap: automatically removes bobbers when they are GC'd
    public static final Map<FishingBobberEntity, PlayerEntity> STAB_BOBBERS = new WeakHashMap<>();
}
