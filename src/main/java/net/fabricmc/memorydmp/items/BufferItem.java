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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class BufferItem extends Item {

    private static final Map<PlayerEntity, Boolean> hasSeenMessage = new WeakHashMap<>();
    private static final int MAX_DURABILITY = 32;

    public BufferItem(Settings settings) {
        super(settings.maxDamage(MAX_DURABILITY));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§7It's a... buffer."));
        tooltip.add(txt("§8Grants §k???idfkeitherman"));
        tooltip.add(txt("§6Durability: " + (MAX_DURABILITY - stack.getDamage()) + "/" + MAX_DURABILITY));
        tooltip.add(txt("§eMain Hand: Strength IV + Durability Drain"));
        tooltip.add(txt("§eOff Hand: Strength III (still, Durability Drain)"));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        if (!world.isClient()) {
            user.sendMessage(txt("§6The Buffer hums with ?..."), true);
            user.playSound(SoundEvents.BLOCK_CONDUIT_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        
        return TypedActionResult.success(itemStack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // This only gets called when item is in MAIN HAND and used to attack
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) attacker;
            World world = player.getWorld();
            
            if (!world.isClient()) {
                // Damage the item (reduce durability by 1) - ONLY for main hand attacks
                stack.damage(1, player, (p) -> {
                    p.sendToolBreakStatus(Hand.MAIN_HAND);
                });
                
                // Play sound effect
                world.playSound(null, player.getBlockPos(), 
                    SoundEvents.BLOCK_NOTE_BLOCK_BIT, 
                    SoundCategory.PLAYERS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
                
                if (world.random.nextFloat() < 0.3F) {
                    player.sendMessage(txt("§7Buffer: Processing attack..."), true);
                }
                
                MemoryDmpMod.LOGGER.info("Buffer used in MAIN HAND attack by: " + player.getEntityName() + 
                                         " Durability: " + (MAX_DURABILITY - stack.getDamage()) + "/" + MAX_DURABILITY);
            }
        }
        return true;
    }

    // offhand stuhf
    public static void onPlayerAttack(PlayerEntity player, LivingEntity target) {
        World world = player.getWorld();
        ItemStack offhandStack = player.getOffHandStack();
        
        // Check if buffer is in offhand and player is attacking
        if (offhandStack.getItem() instanceof BufferItem && !world.isClient()) {
            // Offhand doesn't drain durability, just log it
            MemoryDmpMod.LOGGER.info("Buffer in OFFHAND during attack by: " + player.getEntityName() + 
                                     " (No durability drain)");
            
            // Optional: Play different sound for offhand
            world.playSound(null, player.getBlockPos(), 
                SoundEvents.BLOCK_NOTE_BLOCK_BELL, 
                SoundCategory.PLAYERS, 0.7F, 1.2F);
            
            // stack.damage(1, player, (p) -> {
            //         p.sendToolBreakStatus(Hand.MAIN_HAND);
            //     });
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient() && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            
            ItemStack mainHandStack = player.getMainHandStack();
            ItemStack offhandStack = player.getOffHandStack();
            
            boolean inMainHand = mainHandStack.getItem() == this;
            boolean inOffhand = offhandStack.getItem() == this;
            
            if (inMainHand || inOffhand) {
                // Only give effects if the item isn't broken
                if (stack.getDamage() < MAX_DURABILITY) {
                    // DIFFERENT STRENGTH LEVELS BASED ON HAND
                    int strengthLevel = inMainHand ? 4 : 3; // Main hand: Strength III, Offhand: Strength I
                    int duration = inMainHand ? 2 : 3; // Slightly longer duration for offhand
                    
                    player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.STRENGTH,
                        duration,
                        strengthLevel,
                        true,
                        true
                    ));
                    
                    // Show message only once per holding session
                    if (!hasSeenMessage.getOrDefault(player, false)) {
                        String hand = inMainHand ? "main hand" : "offhand";
                        player.sendMessage(txt("§cYou feel your sanity drain (" + hand + ")"), true);
                        player.playSound(SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        MemoryDmpMod.LOGGER.info(player.getEntityName() + " is holding the buffer in " + hand);
                        hasSeenMessage.put(player, true);
                    }
                } else {
                    // Item is broken - remove effects
                    player.removeStatusEffect(StatusEffects.STRENGTH);
                    if (hasSeenMessage.getOrDefault(player, false)) {
                        player.sendMessage(txt("§8The buffer has been depleted..."), true);
                        hasSeenMessage.put(player, false);
                    }
                }
            } else {
                // Only reset if player was previously holding it
                if (hasSeenMessage.getOrDefault(player, false)) {
                    hasSeenMessage.put(player, false);
                    MemoryDmpMod.LOGGER.info(player.getEntityName() + " stopped holding the buffer.");
                }
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamage() * 13.0F / (float)MAX_DURABILITY);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float durabilityRatio = Math.max(0.0F, (float)(MAX_DURABILITY - stack.getDamage()) / (float)MAX_DURABILITY);
        return durabilityRatio < 0.5F ? 0xFF0000 : 0x00FF00;
    }
}