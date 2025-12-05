package net.fabricmc.memorydmp;

// Import the interface
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.memorydmp.items.IGroundCollidable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;

public class MemoryDmpEvents {
    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                for (ItemEntity itemEntity : world.getEntitiesByType(EntityType.ITEM, Entity::isAlive)) {
                    if (itemEntity.getStack().getItem() instanceof IGroundCollidable) {
                        ((IGroundCollidable) itemEntity.getStack().getItem())
                                .onGroundCollision(itemEntity);
                    }
                }








//                for (Iterator<Map.Entry<FishingBobberEntity, PlayerEntity>> it = StabShotTracker.STAB_BOBBERS.entrySet().iterator(); it.hasNext(); ) {
//                    Map.Entry<FishingBobberEntity, PlayerEntity> entry = it.next();
//                    FishingBobberEntity bobber = entry.getKey();
//                    PlayerEntity player = entry.getValue();
//
//                    if (bobber.isRemoved()) {
//                        it.remove(); // Clean up
//                        continue;
//                    }
//
//                    // Custom logic: check for block hit, etc.
//                    if (bobber.isOnGround()) {
//                        StabShotBobberEntity.spawnTNT(bobber.getPos(), world);
//
//                        // Damage rod
//                        ItemStack rod = player.getStackInHand(player.getActiveHand());
//                        rod.damage(69, player, (p) -> p.sendToolBreakStatus(player.getActiveHand()));
//
//                        it.remove(); // Done with this bobber
//                    }
//                }


            }
        });

    }


}