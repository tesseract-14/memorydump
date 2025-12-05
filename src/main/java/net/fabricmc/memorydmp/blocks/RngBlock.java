package net.fabricmc.memorydmp.blocks;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class RngBlock extends ToolRequiredBlock{


    private long lastProcess = 0;

    public RngBlock(Settings settings) {
        super(settings, 0);
        this.setDefaultState(this.stateManager.getDefaultState());
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        MemoryDmpMod.LOGGER.info("Collision detected. -Turner block");
        if (!world.isClient() && entity instanceof ItemEntity) {
            long currentTime = world.getTime();
            if (currentTime - lastProcess < 20){
                // since I don't want spamming im just not gonna log
                return;
            }
            lastProcess = currentTime;

            ItemEntity itemEntity = (ItemEntity) entity;
            ItemStack stack = itemEntity.getStack();

            // hop off if stack is empty
            if (stack.isEmpty()) {
                MemoryDmpMod.LOGGER.info("Stack is empty, returning -gambling block");
                return;
            }

            // Transform the item
            boolean dupe = world.random.nextFloat() < 0.4f;
            if (dupe){
                ItemStack newStack = itemEntity.getStack().copy();
                ItemEntity dupeEntity = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), newStack);
                dupeEntity.setVelocity(itemEntity.getVelocity());

                world.spawnEntity(dupeEntity);
                // itemEntity.getOwner().sendMessage(txt("duped item for ya"), true);
                world.playSound(null, itemEntity.getX(), itemEntity.getY(), itemEntity.getX(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.5f, 1.0f);

                MemoryDmpMod.LOGGER.info("Duped {}", itemEntity.getStack().getName());
            }else{
                boolean turnFlesh = world.random.nextFloat() < 0.7f;
                if (turnFlesh){
                    ItemStack fleshStack = new ItemStack(Items.ROTTEN_FLESH, itemEntity.getStack().getCount());
                    ItemEntity fleshEntity = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), fleshStack);
                    fleshEntity.setVelocity(itemEntity.getVelocity());

                    world.spawnEntity(fleshEntity);
                    itemEntity.discard();

                    MemoryDmpMod.LOGGER.info("turned " + itemEntity.getStack().getName() + " into FLESH");
                }else{
                    itemEntity.discard();
                    MemoryDmpMod.LOGGER.info("discarded of " + itemEntity.getStack().getName());
                }
            }

            super.onSteppedOn(world, pos, state, entity);
        }
    }

    private int dialogue = 0;
    private long lastYap = 0;
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        long currentTime = world.getTime();
        if (currentTime - lastYap < 25){
            return ActionResult.PASS;
        }
        lastYap = currentTime;

        String[] dialogues = {
                "Do you love gambling?",
                "I do.",
                "It's tuff",
                "cuz...",
                "idk...",
                "it's just tuff bro",
                "watcha lookin' at?",
                "get out.",
                "stop right clicking me.",
                "hey.",
                "stop!!!",
                "I'll use a trick of my own. restarting my dialogue to piss you off :D"
        };
        String[] decorators = {
          "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "l", "m", "n", "o"
        };
        String msg = "§" + decorators[world.random.nextInt(decorators.length)] + dialogues[dialogue];

        player.sendMessage(txt(msg), true);
        player.playSound(SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.5f, 1.0f);

        MemoryDmpMod.LOGGER.info("player right clicked. dialogue to be shown: " + msg);

        dialogue++;
        if (dialogue >= dialogues.length) {
            dialogue = 0;
        }


        return ActionResult.SUCCESS;
    }
}