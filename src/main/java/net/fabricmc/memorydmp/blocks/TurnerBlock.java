package net.fabricmc.memorydmp.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.text.LiteralText;
import net.minecraft.entity.damage.DamageSource;

import java.util.List;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

import java.util.List;

// import org.w3c.dom.Text;

import net.fabricmc.memorydmp.MemoryDmpMod;

public class TurnerBlock extends Block {

    // @Override
    // public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    //     if (!world.isClient()){
    //         item = new ItemStack(MemoryDmpMod.TURNER_BLOCK_ITEM);
    //         double x = BlockPos.getX();
    //         double y = BlockPos.getY();
    //         double z = BlockPos.getZ();

    //         world.spawnEntity(item, x, y, z);
    //     }

    //     super.onBreak(world, pos, state, player);
    // }


    public TurnerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        MemoryDmpMod.LOGGER.info("Collision detected. -Turner block");
        if (!world.isClient() && entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity) entity;
            ItemStack stack = itemEntity.getStack();
            
            // hop off if stack is empty
            if (stack.isEmpty()){ 
                MemoryDmpMod.LOGGER.info("Stack is empty, returning -Turner block");
                return;
            }
            
            // Transform the item
            ItemStack newStack = transformItem(stack, world, itemEntity);
            
            if (!newStack.isEmpty() && !ItemStack.areEqual(stack, newStack)) {
                // Create new item entity with transformed item
                ItemEntity newItemEntity = new ItemEntity(
                    world, 
                    itemEntity.getX(), 
                    itemEntity.getY(), 
                    itemEntity.getZ(), 
                    newStack
                );
                newItemEntity.setVelocity(itemEntity.getVelocity());
                
                // Remove old item and spawn new one
                itemEntity.discard();
                world.spawnEntity(newItemEntity);
                
                // Play transformation sound
                world.playSound(
                    null, 
                    pos, 
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 
                    SoundCategory.BLOCKS, 
                    1.0F, 
                    1.0F
                );
                
                // Particle stuff would go here (client-side)
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    private ItemStack transformItem(ItemStack original, World world, ItemEntity itemEntity) {
        Item originalItem = original.getItem();
        int count = original.getCount();
        
        // transformation logic here
        if (originalItem == Items.COBBLESTONE) {
            return new ItemStack(Items.STONE, count);
        }
        else if (originalItem == Items.STONE) {
            return new ItemStack(Items.SMOOTH_STONE, count);
        }
        else if (originalItem == Items.IRON_INGOT) {
            return new ItemStack(Items.GOLD_INGOT, count);
        }
        else if (originalItem == Items.GOLD_INGOT) {
            return new ItemStack(Items.DIAMOND, count);
        }
        else if (originalItem == Items.ROTTEN_FLESH) {
            return new ItemStack(Items.LEATHER, count);
        }
        else if (originalItem == Items.POISONOUS_POTATO) {
            return new ItemStack(Items.BAKED_POTATO, count);
        }
        else if (originalItem == Items.DIRT) {
            return new ItemStack(Items.GRASS_BLOCK, count);
        }
        else if (originalItem == Items.SAND) {
            return new ItemStack(Items.GLASS, count);
        }
        else if (originalItem == Items.CACTUS) {
            return new ItemStack(Items.GREEN_DYE, count);
        }
        // custom items
        else if (originalItem == MemoryDmpMod.SKULL) {
            return new ItemStack(MemoryDmpMod.EYE, count);
        }
        else if (originalItem == MemoryDmpMod.EYE) {
            return new ItemStack(MemoryDmpMod.BUFFER, count);
        }
        else if (originalItem == MemoryDmpMod.BUFFER) {
            return new ItemStack(MemoryDmpMod.TURNER_BLOCK_ITEM, count);
        }
        else if (originalItem == MemoryDmpMod.TURNER_BLOCK_ITEM) {
            return new ItemStack(MemoryDmpMod.CHOAS_BLOCK_ITEM, count);
        }
        else if (originalItem == MemoryDmpMod.CHOAS_BLOCK_ITEM) {
            return new ItemStack(Items.ROTTEN_FLESH, count);
        }
        
        // If no transformation, just throw the item atp
        if (itemEntity != null) {
            // jump + spin
            itemEntity.addVelocity(
                world.random.nextDouble() * 0.2 - 0.1,
                0.2,
                world.random.nextDouble() * 0.2 - 0.1
            );
            world.playSound(
                null, 
                itemEntity.getBlockPos(), 
                SoundEvents.BLOCK_NOTE_BLOCK_BASS, 
                SoundCategory.BLOCKS, 
                0.5F, 
                0.5F
            );
        }
        return original.copy();
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        MemoryDmpMod.LOGGER.info("Damaged the player because they touched me.");
        player.damage(DamageSource.MAGIC, 3.5F);

        player.sendMessage(txt("Don't touch me."), true);

        return ActionResult.SUCCESS;
    }
}