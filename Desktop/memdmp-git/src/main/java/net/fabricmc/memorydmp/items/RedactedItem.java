package net.fabricmc.memorydmp.items;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.*;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class RedactedItem extends Item implements IGroundCollidable {
    private static final SoundEvent[] randSounds = {
            SoundEvents.AMBIENT_CAVE,
            SoundEvents.BLOCK_ANVIL_FALL,
            SoundEvents.BLOCK_CONDUIT_ACTIVATE,
            SoundEvents.BLOCK_AZALEA_LEAVES_BREAK,
            SoundEvents.BLOCK_AZALEA_LEAVES_PLACE,
            SoundEvents.BLOCK_NETHER_ORE_STEP,
            // ...
    };


    private static List<Item> ALL_ITEMS_CACHE;
    private static final Map<UUID, Boolean> PROCESSED_GROUND_ITEMS = new WeakHashMap<>();

    private List<Item> getAllItems(World world) {
        if (ALL_ITEMS_CACHE == null) {
            Registry<Item> registry = world.getRegistryManager().get(Registry.ITEM_KEY);
            ALL_ITEMS_CACHE = new ArrayList<>();
            registry.iterator().forEachRemaining(ALL_ITEMS_CACHE::add);
        }
        return ALL_ITEMS_CACHE;
    }


    public RedactedItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context){
        tooltip.add(txt("You should not have accessed this item."));
        tooltip.add(txt("§aGives you a random item, but it comes at a §l§c§ocost§r"));
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) return TypedActionResult.success(itemStack);
        if (!(itemStack.getItem() instanceof RedactedItem)) return TypedActionResult.fail(itemStack);

        Inventory inv = user.getInventory();
        if (inv.getStack(40).getItem() instanceof RedactedItem && !(user.getMainHandStack().getItem() instanceof RedactedItem)){
            // pull up a random enchant
            // ItemStack offhand = inv.getStack(40);
            ItemStack main = user.getMainHandStack();

            for (int i = 0; i < world.random.nextInt(7) +1; i++){
                addRandEnchant(world, main);
            }
            log("Enchanted item: " + main.getTranslationKey());

            inv.setStack(40, Items.AIR.getDefaultStack());
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 0.9f, 1.2f);

            return TypedActionResult.success(itemStack);
        }

        // get the rand item ==========
        ItemStack newItem = new ItemStack(getAllItems(world).get(world.random.nextInt(ALL_ITEMS_CACHE.size())));
        // get the rand item =========



        user.setStackInHand(hand, newItem);
        world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 1.6f, world.random.nextFloat() * 0.4f);
        if (!user.isCreative()) {
            user.damage(DamageSource.OUT_OF_WORLD, world.random.nextFloat() * 4.5f + 2.0f);
        }else{
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_IRON_GOLEM_DAMAGE, SoundCategory.PLAYERS, 1.5f, 1.0f);
        }

        if (world.random.nextFloat() < 0.7f){
            for (int i = 0; i < world.random.nextInt(5) +1; i++){
                addRandEnchant(world, newItem);
            }

        }

        newItem.setCount(world.random.nextInt(64) +1);

        log("!!REDACTED ITEM USED!!: " + newItem.getTranslationKey());



        user.sendMessage(txt("§kbrochachipsactuallyusedtheitem"), true);



        return TypedActionResult.success(itemStack);
    }



    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        if (world.random.nextFloat() < 0.001f){
            player.setStackInHand(player.getActiveHand(), Items.AIR.getDefaultStack());

            log("oh noes. the [redacted] item vanished.");
            return;
        }
        if (world.random.nextFloat() < 0.003f){
            ItemStack thisSlot = player.getInventory().getStack(slot);

            int rand = world.random.nextInt(41);
            ItemStack randSlot = player.getInventory().getStack(rand);


            if (!thisSlot.isEmpty() && !randSlot.isEmpty()) {
                player.getInventory().setStack(slot, randSlot);
                player.getInventory().setStack(rand, thisSlot);

                log("Switched slots " + slot + " with " + rand);
            }
            return;
        }


        SoundEvent randSound = randSounds[world.random.nextInt(randSounds.length)];

        if (world.random.nextFloat() < 0.1f) player.playSound(randSound, 0.5f, 1.0f);


        super.inventoryTick(stack, world, player, slot, selected);
    }


    private void addRandEnchant(World world, ItemStack stack){
        List<Enchantment> all = new ArrayList<>();

        for (Enchantment enchant : Registry.ENCHANTMENT){
            all.add(enchant);
        }
        Enchantment rande = all.get(world.random.nextInt(all.size()));

        int maxlvl = 12;
        int lvl = world.random.nextInt(maxlvl) +1;

        stack.addEnchantment(rande, lvl);

        log("Added enchant: " + rande.getTranslationKey() + ", lvl: " + lvl);


    }



    // IGrindCollisionable
    @Override
    public void onGroundCollision(ItemEntity itemEntity) {
        World world = itemEntity.getWorld();


        boolean isOnGround = itemEntity.isOnGround() ||
                itemEntity.getVelocity().y == 0 ||
                world.getBlockState(itemEntity.getBlockPos().down()).isSolidBlock(world, itemEntity.getBlockPos().down());


        if (isOnGround && !PROCESSED_GROUND_ITEMS.containsKey(itemEntity.getUuid())) {
            exec(itemEntity);
            PROCESSED_GROUND_ITEMS.put(itemEntity.getUuid(), true);

            MemoryDmpMod.LOGGER.info("GROUND COLLISION DETECTED by [REDACTED]item.");
        }
    }


    private void exec(ItemEntity item){
        World world = item.getWorld();
        Vec3d itemPos = item.getPos();
        double itemX = item.getX();
        double itemY = item.getY();
        double itemZ = item.getZ();

        BlockPos itemBlockPos = item.getBlockPos();


        if (world.random.nextFloat() < 0.4f){
            world.createExplosion(null, null, null, itemX, itemY, itemZ, 4.5f, true, Explosion.DestructionType.BREAK);
            item.discard();

            log("Created explosion bc of groundCollision of a redacted item, at: " + itemPos);
        }else{
            /*
            so there's no fill for some reason??
            I gotta like iterate thru itemY all the way till itemy -60.
            nvm actually it aint that hard
             */

            int height = world.random.nextInt(53) + 1;
            int minY = Math.max(world.getBottomY(), itemBlockPos.getY() - height);

            for (int y = itemBlockPos.getY(); y >= minY; y--) {
                BlockPos pos = new BlockPos(itemBlockPos.getX(), y, itemBlockPos.getZ());
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }

            log("Created air column of " + height + " blocks at: " + itemBlockPos);
        }


    }

}