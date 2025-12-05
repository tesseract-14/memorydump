package net.fabricmc.memorydmp.mixin;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.fabricmc.memorydmp.items.Magnets.Lv3MagnetItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ShulkerBoxItemMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void onInventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {

        // Check if this ItemStack IS a shulker box item
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) return;

        if (!(entity instanceof PlayerEntity player)) return;
        if (world.isClient()) return;

        // Now read the contents of the shulker
        NbtCompound beTag = stack.getSubNbt("BlockEntityTag");
        if (beTag == null || !beTag.contains("Items")) return;

        DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
        Inventories.readNbt(beTag, items);

        // Tick each magnet inside the shulker
        for (ItemStack inner : items) {
            if (inner.getItem() instanceof Lv3MagnetItem) {
                Lv3MagnetItem.tickWhileInsideShulker(inner, world, player);
            }
        }
    }
}
