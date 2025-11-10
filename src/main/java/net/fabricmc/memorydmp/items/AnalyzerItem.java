package net.fabricmc.memorydmp.items;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class AnalyzerItem extends Item {

    boolean analyze = true;
    @Override
    // @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§7Analyzer..."));
        tooltip.add(txt("§k???idfkeitherman"));
    }

    public AnalyzerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (world.isClient()) {
            return TypedActionResult.fail(itemStack);
        }
        if (analyze) {
            user.sendMessage(txt("Human (prolly) detected."), false);
            user.sendMessage(txt("Username is: §e" + user.getEntityName()), false);
            user.sendMessage(txt("HP: §e" + user.getHealth()), false);
            user.sendMessage(txt("max health: §e" + user.getMaxHealth()), false);
            user.sendMessage(txt("Velocity data: §e" + user.getVelocity()), false);
            
            analyze = false;
        }else{
            user.sendMessage(txt("Nvm, not gonna analyze."), false); 
            analyze = true;
        }
        
        return TypedActionResult.success(itemStack);
    }

    public static void onLeftClickEntity(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();
        
        if (!world.isClient()) {
            // Check if player is holding analyzer in main hand
            ItemStack mainHandStack = player.getMainHandStack();
            
            if (mainHandStack.getItem() instanceof AnalyzerItem) {
                // left clicked w/ main hand
                player.sendMessage(txt("§6Analyzer: Scanning " + target.getType().getName().getString() + "..."), true);
                
                // analysys
                
                // HP
                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                player.sendMessage(txt("Health: " + String.format("%.1f", health) + "/" + String.format("%.1f", maxHealth)), false);
                
                // effects
                if (!target.getActiveStatusEffects().isEmpty()) {
                    player.sendMessage(txt("Active effects: " + target.getActiveStatusEffects().size()), false);
                }
                
                // is it a boss?
                // if (target.isAiDisabled()) {
                //     player.sendMessage(txt("§cBOSS ENTITY DETECTED!!!1!!"), false);
                // }
                
                // custom name, perhaps?
                if (target.hasCustomName()) {
                    player.sendMessage(txt("Custom name: " + target.getCustomName().getString()), false);
                }
                
                // noise
                world.playSound(null, player.getBlockPos(), 
                    net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BELL, 
                    net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 1.5F);
                
                MemoryDmpMod.LOGGER.info("Analyzer used on: " + target.getType() + " by " + player.getEntityName());
            }
        }
    }

    public static void onLeftClickAir(PlayerEntity player) {
        World world = player.getWorld();
        
        if (!world.isClient()) {
            ItemStack mainHandStack = player.getMainHandStack();
            
            if (mainHandStack.getItem() instanceof AnalyzerItem) {
                // Reset analyze variable
                // Since this is static, we need to get the instance from the item stack
                if (mainHandStack.getItem() instanceof AnalyzerItem analyzerItem) {
                    analyzerItem.analyze = false;
                    player.sendMessage(txt("§7Analysis reset. Ready for new scan."), true);
                    
                    world.playSound(null, player.getBlockPos(), 
                        net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BIT, 
                        net.minecraft.sound.SoundCategory.PLAYERS, 1.0F, 0.8F);
                        
                    MemoryDmpMod.LOGGER.info("Analyzer reset by: " + player.getEntityName());
                }
            }
        }
    }
}