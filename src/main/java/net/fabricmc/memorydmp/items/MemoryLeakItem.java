package net.fabricmc.memorydmp.items;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.block.Blocks;

import java.util.*;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class MemoryLeakItem extends Item implements IGroundCollidable {
    private static final Map<PlayerEntity, List<byte[]>> LEAK_STORAGE = new WeakHashMap<>();
    private static final Map<UUID, Boolean> PROCESSED_GROUND_ITEMS = new WeakHashMap<>();
    private static final Map<PlayerEntity, Long> LAST_CLEAR_TIMES = new WeakHashMap<>();
    private static final Map<PlayerEntity, Long> LAST_LEVEL_UP_TIMES = new WeakHashMap<>();

    public MemoryLeakItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        int level = getLevel(stack);
        int leakAmount = getLeakAmount(stack);

        tooltip.add(txt("§7Don't hold it in your inventory too long, causes §cmemory leaks"));
        tooltip.add(txt("§eLevel: " + level + " | Leak Size: " + (leakAmount/1024) + "KB"));
        tooltip.add(txt("§8Shift+Right-click to level up"));
        tooltip.add(txt("§4WARNING: Will crash your game!"));
    }

    // ==== HELPER METHODS ====
    private static int getLevel(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("LeakLevel")) {
            return stack.getNbt().getInt("LeakLevel");
        }
        return 1; // default
    }

    private static int getLeakAmount(ItemStack stack) {
        int level = getLevel(stack);
        return (int) Math.pow(2, level - 1) * 1024;
    }

    private static void setLevel(ItemStack stack, int level) {
        stack.getOrCreateNbt().putInt("LeakLevel", level);
    }
    // ==== HELPER METHODS ====

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        boolean isHolding = player.getMainHandStack().getItem() == this ||
                player.getOffHandStack().getItem() == this;

        if (isHolding && world.getTime() % 10 == 0) {
            List<byte[]> leaks = LEAK_STORAGE.computeIfAbsent(player, k -> new ArrayList<>());
            int leakAmount = getLeakAmount(stack);

            byte[] garbage = new byte[leakAmount];
            world.random.nextBytes(garbage);
            leaks.add(garbage);

            int totalKB = (leaks.size() * leakAmount) / 1024;
            player.sendMessage(txt("§cmemory leaked: " + totalKB + "KB (Lvl " + getLevel(stack) + ")"), true);

            if (leaks.size() % 50 == 0) {
                MemoryDmpMod.LOGGER.info(player.getEntityName() + " memory leakage: " + totalKB + "KB");
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        MemoryDmpMod.LOGGER.info("Right click on memoryleakitem detected.");
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()){
            return TypedActionResult.success(itemStack);
        }

        long currentTime = world.getTime();

        // USE PER-PLAYER COOLDOWNS
        Long lastClear = LAST_CLEAR_TIMES.get(user);
        if (lastClear != null && currentTime - lastClear < 10) {
            MemoryDmpMod.LOGGER.info("Cooldown active - returning PASS");
            return TypedActionResult.pass(itemStack);
        }
        LAST_CLEAR_TIMES.put(user, currentTime);

        if (user.isSneaking()) {
            // USE PER-PLAYER LEVEL-UP COOLDOWNS
            MemoryDmpMod.LOGGER.info("reached isSneaking branch");

            updLevel(user, itemStack);
            return TypedActionResult.success(itemStack);
        }

        // Clear leaks logic
        List<byte[]> playerLeaks = LEAK_STORAGE.get(user);
        if (playerLeaks != null && !playerLeaks.isEmpty()) {
            int leakAmount = getLeakAmount(itemStack);
            int totalBytes = playerLeaks.size() * leakAmount;

            String first32BytesHex = "";
            if (!playerLeaks.isEmpty()) {
                byte[] firstLeak = playerLeaks.get(0);
                StringBuilder hexBuilder = new StringBuilder();
                for (int i = 0; i < Math.min(32, firstLeak.length); i++) {
                    hexBuilder.append(String.format("%02X ", firstLeak[i]));
                }
                first32BytesHex = hexBuilder.toString().trim();
            }

            LEAK_STORAGE.remove(user);
            int currentlvl = getLevel(itemStack);

            user.sendMessage(txt("§aMemory leaks cleared! (Level: " + currentlvl + ")"), false);
            user.sendMessage(txt("Total leaked: " + totalBytes + " bytes (" + (totalBytes / 1024) + "KB)"), false);
            user.sendMessage(txt("First 32 bytes: " + first32BytesHex), false);
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, world.random.nextFloat() * 0.4f);
        } else {
            user.sendMessage(txt("§7No memory leaks to clear..."), false);
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_FALL, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.4f);
        }

        return TypedActionResult.success(itemStack);
    }

    private static void updLevel(PlayerEntity player, ItemStack itemStack) {
        World world = player.getWorld();

        if (world.isClient()) return;
        if (!(itemStack.getItem() instanceof MemoryLeakItem)) return;

        int currentLevel = getLevel(itemStack);

        if (currentLevel >= 10) {
            setLevel(itemStack, 1);
            player.sendMessage(txt("§dReset level to 1. Leak size: §e1024 bytes"), false);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_CONDUIT_ACTIVATE, SoundCategory.BLOCKS, 1.2f, 1.0f);
            MemoryDmpMod.LOGGER.info(player.getEntityName() + " reset leak item level.");
        } else {
            int newLevel = currentLevel + 1;
            setLevel(itemStack, newLevel);
            int newLeakAmount = getLeakAmount(itemStack);

            player.sendMessage(txt("New level: §a" + newLevel + "§r, Leak size: §e" + newLeakAmount + " bytes"), false);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1.2f, 1.0f);
            MemoryDmpMod.LOGGER.info(player.getEntityName() + " leveled up item to " + newLevel + ", leakAmount: " + newLeakAmount);
        }
    }




    @Override
    public void onGroundCollision(ItemEntity itemEntity) {
        World world = itemEntity.getWorld();


        boolean isOnGround = itemEntity.isOnGround() ||
                itemEntity.getVelocity().y == 0 ||
                world.getBlockState(itemEntity.getBlockPos().down()).isSolidBlock(world, itemEntity.getBlockPos().down());


        if (isOnGround && !PROCESSED_GROUND_ITEMS.containsKey(itemEntity.getUuid())) {
            executeGroundEffect(itemEntity);
            PROCESSED_GROUND_ITEMS.put(itemEntity.getUuid(), true);

            MemoryDmpMod.LOGGER.info("GROUND COLLISION DETECTED.");
        }
    }

    private void executeGroundEffect(ItemEntity itemEntity) {
        World world = itemEntity.getWorld();
        BlockPos itemPos = itemEntity.getBlockPos();
        BlockPos blockBelow = itemPos.down();
        Block blockBelowInstance = world.getBlockState(blockBelow).getBlock();

        if (
                blockBelowInstance == Blocks.OBSIDIAN ||
                blockBelowInstance == Blocks.DIAMOND_BLOCK ||
                blockBelowInstance == Blocks.NETHERITE_BLOCK
        ){
            log("either obsidian, diamond block, or netherite block below. skipping conversion.");
            return;
        }

        // tuff sfx
        world.playSound(null, itemPos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                SoundCategory.BLOCKS, 0.5f, 0.7f);

        // particles::
        for (int i = 0; i < 15; i++) {
            world.addParticle(ParticleTypes.EXPLOSION,
                    itemPos.getX() + world.random.nextDouble(),
                    itemPos.getY() + 0.2,
                    itemPos.getZ() + world.random.nextDouble(),
                    0, 0.1, 0);
        }

        boolean secondBranch = true;
        if (world.random.nextFloat() < 0.5f) {
            secondBranch = false;
            world.setBlockState(blockBelow, Blocks.OBSIDIAN.getDefaultState());
        }

        if (world.random.nextFloat() < 0.6f && secondBranch) {
            world.setBlockState(blockBelow, Blocks.DIAMOND_BLOCK.getDefaultState());
            world.playSound(null, blockBelow, SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK,
                    SoundCategory.BLOCKS, 1.2f, 1.0f);
            MemoryDmpMod.LOGGER.info("MemoryLeakItem created DIAMOND at " + blockBelow);
        } else if (secondBranch) {
            world.setBlockState(blockBelow, Blocks.NETHERITE_BLOCK.getDefaultState());
            world.playSound(null, blockBelow, SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK,
                    SoundCategory.BLOCKS, 1.2f, 0.8f);
            MemoryDmpMod.LOGGER.info("MemoryLeakItem created NETHERITE at " + blockBelow);
        }

        itemEntity.addVelocity(
                world.random.nextDouble() * 0.2 - 0.1,
                0.2,
                world.random.nextDouble() * 0.2 - 0.1
        );
    }
}