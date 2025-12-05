package net.fabricmc.memorydmp.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class ToolRequiredBlock extends Block {
    private final int requiredMiningLevel;

    public ToolRequiredBlock(Settings settings, int requiredMiningLevel) {
        super(settings);
        this.requiredMiningLevel = requiredMiningLevel;
    }

    // 1.18.2 method signature
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        ItemStack stack = player.getMainHandStack();

        if (stack.getItem() instanceof PickaxeItem pickaxe) {
            if (pickaxe.getMaterial().getMiningLevel() >= requiredMiningLevel) {
                return player.getBlockBreakingSpeed(state) / getHardness() / 30F; // Normal speed
            }
        }

        return player.getBlockBreakingSpeed(state) / getHardness() / 150f; // 5x slower
    }
}