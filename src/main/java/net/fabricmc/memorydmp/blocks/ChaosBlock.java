package net.fabricmc.memorydmp.blocks;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

// import org.w3c.dom.Text;

import net.fabricmc.memorydmp.MemoryDmpMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.LiteralText;
import net.minecraft.entity.Entity;

import java.util.List;

public class ChaosBlock extends Block {

    public ChaosBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // onUse PARAMETERS:
        // - state: The block's current state
        // - world: The world the block is in
        // - pos: The block's position (x, y, z)
        // - player: The player who right-clicked
        // - hand: Which hand was used (MAIN_HAND or OFF_HAND)
        // - hit: Where exactly the player clicked on the block
        MemoryDmpMod.LOGGER.info("Someone right clicked on me bro. -chaos block");
        
        if (!world.isClient()) {
            // launch or not?
            if (Math.random() < 0.1){
                MemoryDmpMod.LOGGER.info("Throwing the player away.");
                player.sendMessage(txt("I have decided that you are not worthy enough."), true);

                player.setOnGround(false);
                float yaw = player.getYaw();
                double x = -Math.sin(Math.toRadians(yaw)) * 1.0;
                double z = Math.cos(Math.toRadians(yaw)) * 1.0;
                player.setVelocity(x, 2.3, z);
                player.velocityModified = true;

                world.playSound(null, pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.BLOCKS, 1.0F, 0.5F);
                return ActionResult.SUCCESS;
            }
            // dont launch, but...
            if (Math.random() < 0.2){
                MemoryDmpMod.LOGGER.info("Cleared player's effects");
                player.sendMessage(txt("Atp, ill just clear your effects!"), true);

                player.clearStatusEffects();
                world.playSound(null, pos, MemoryDmpMod.GETOUT, SoundCategory.PLAYERS, 1.0F, 0.5F);
                return ActionResult.SUCCESS;
            }
            // Effect list
            StatusEffectInstance[] negative = {
                        new StatusEffectInstance(StatusEffects.NAUSEA, world.random.nextInt(3500), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.SLOWNESS, world.random.nextInt(3500), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.UNLUCK, world.random.nextInt(100000), world.random.nextInt(4)),
                        new StatusEffectInstance(StatusEffects.BLINDNESS, world.random.nextInt(3600), 0),
                        new StatusEffectInstance(StatusEffects.SLOWNESS, world.random.nextInt(3500), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.WEAKNESS, world.random.nextInt(4000), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.MINING_FATIGUE, world.random.nextInt(5000), world.random.nextInt(4)),
                        new StatusEffectInstance(StatusEffects.POISON, world.random.nextInt(2000), world.random.nextInt(2)),
                        new StatusEffectInstance(StatusEffects.WITHER, world.random.nextInt(1500), world.random.nextInt(1)),
                        new StatusEffectInstance(StatusEffects.HUNGER, world.random.nextInt(6000), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, world.random.nextInt(3)), // Instant damage
                        new StatusEffectInstance(StatusEffects.LEVITATION, 30, 255), // float upwards a little...
                        new StatusEffectInstance(StatusEffects.GLOWING, world.random.nextInt(12000), 0),
                        // new StatusEffectInstance(StatusEffects.DARKNESS, world.random.nextInt(3000), 0),
                        new StatusEffectInstance(StatusEffects.JUMP_BOOST, world.random.nextInt(3000), 128),
                        new StatusEffectInstance(StatusEffects.BAD_OMEN, world.random.nextInt(100000), 0),
                        new StatusEffectInstance(StatusEffects.BLINDNESS, world.random.nextInt(5000), 0)
                };
            
            StatusEffectInstance[] positive = {
                        new StatusEffectInstance(StatusEffects.JUMP_BOOST, world.random.nextInt(2000), world.random.nextInt(4)),
                        new StatusEffectInstance(StatusEffects.HEALTH_BOOST, world.random.nextInt(2000), world.random.nextInt(4)),
                        new StatusEffectInstance(StatusEffects.ABSORPTION, world.random.nextInt(2000), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.LUCK, 72000, world.random.nextInt(1)),
                        new StatusEffectInstance(StatusEffects.HEALTH_BOOST, world.random.nextInt(2000), world.random.nextInt(3)),
                        new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, world.random.nextInt(5000), 0),
                        new StatusEffectInstance(StatusEffects.NIGHT_VISION, 72000, 0),
                        new StatusEffectInstance(StatusEffects.SATURATION, world.random.nextInt(4000), world.random.nextInt(5))
                };

            // apply the actual effects
            boolean applyBad = world.random.nextBoolean();
            if (applyBad){
                MemoryDmpMod.LOGGER.info("Gave player a bad potion effect.");

                StatusEffectInstance randomEffect = negative[(int) (Math.random() * negative.length)];
                player.addStatusEffect(randomEffect);

                String[] msgs = {
                    "I have rewarded you with a §cVery Baad§r potion effect...",
                    "I have rewarded you with a §cBad§r potion effect..."
                };
                player.sendMessage(txt(msgs[(int) (Math.random() * msgs.length)]), true);
                world.playSound(
                    null, 
                    pos, 
                    SoundEvents.BLOCK_GLASS_BREAK, 
                    SoundCategory.BLOCKS, 
                    1.0F, 
                    (float)Math.random() * 0.4F
                );
            }else{
                MemoryDmpMod.LOGGER.info("Gave player a good potion effect.");

                StatusEffectInstance randomEffect = positive[(int) (Math.random() * positive.length)];
                player.addStatusEffect(randomEffect);

                String[] msgs = {
                    "I have rewarded you with a §aGood§r potion effect...!",
                    "§aHere§r, now can you leave me alone?"
                };
                player.sendMessage(txt(msgs[(int) (Math.random() * msgs.length)]), true);
                world.playSound(
                    null, 
                    pos, 
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 
                    SoundCategory.BLOCKS, 
                    1.0F, 
                    (float)Math.random() * 0.4F
                );
            }
        }
        
        return ActionResult.SUCCESS;
    }
    private final Map<PlayerEntity, Long> lastLaunchTime = new WeakHashMap<>();

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient()) {
            long currentTime = world.getTime();
            
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                Long lastTime = lastLaunchTime.get(player);
                
                if (lastTime == null || currentTime - lastTime >= 60) {
                    launchEntity(entity, world, pos);
                    
                    world.playSound(
                        null, 
                        pos, 
                        MemoryDmpMod.GETOUT, 
                        SoundCategory.PLAYERS, 
                        1.0F, 
                        1.0F
                    );
                    
                    lastLaunchTime.put(player, currentTime);
                }
            } else {
                // For other entities, use a different cooldown or no cooldown
                launchEntity(entity, world, pos);
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    private void launchEntity(Entity entity, World world, BlockPos pos) {
        entity.setOnGround(false);
        entity.setVelocity(0, 2, 0);
        
        if (entity instanceof PlayerEntity) {
            ((PlayerEntity) entity).velocityModified = true;
        }
        
        world.playSound(null, pos, SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.BLOCKS, 1.0F, 0.5F);
        MemoryDmpMod.LOGGER.info("Launched " + entity.getType() + " up the sky~");
    }
}