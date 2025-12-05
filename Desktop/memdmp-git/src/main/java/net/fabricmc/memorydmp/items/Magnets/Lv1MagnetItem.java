package net.fabricmc.memorydmp.items.Magnets;

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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class Lv1MagnetItem extends Item {
    private static final int RANGE = 5;
    private static final String KEY = "DoPull";
    private static final int MAX_ITEMS = 20;
    private static final int INTERVAL = 5;

    private static final Map<PlayerEntity, Long> LAST_PULL_TIMES = new WeakHashMap<>();
    private static final Map<PlayerEntity, Long> LAST_ROTO_TIMES = new WeakHashMap<>();

    public Lv1MagnetItem(Item.Settings settings) {
        super(settings);
    }


    // HELPER FUNCS ==========================

    private static boolean doPull(ItemStack stack){
        if (stack.hasNbt() && stack.getNbt().contains(KEY)){
            return stack.getNbt().getBoolean(KEY);
        }
        return true;
    }


    private static String rotatePull(ItemStack stack){
        boolean current = doPull(stack);
        boolean next = !current;

        stack.getOrCreateNbt().putBoolean(KEY, next);


        if (next){
            return "§aOn";
        }else{
            return "§cOff";
        }

    }

    // HELPER FUNCS ==========================



    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§7Pulls items towards u (oh really sherlock?)"));

        String state = doPull(stack) ? "§aON" : "§cOFF";
        tooltip.add(txt(state));
        tooltip.add(txt("§8(Shift-click to toggle)"));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient()) return;
        if (!doPull(stack)) return;
        if (slot > 9) return;
        if (!(entity instanceof PlayerEntity player)) return;

        long currentTime = world.getTime();
        long last = LAST_PULL_TIMES.getOrDefault(player, 0L);
        if (currentTime - last < INTERVAL) return;
        LAST_PULL_TIMES.put(player, currentTime);
        // player.getItemCooldownManager().set(this, 10);



        // actually like start pulling now
        BlockPos playerBlockPos = player.getBlockPos();
        Box box = new Box(
                playerBlockPos.getX() - RANGE, playerBlockPos.getY() - RANGE, playerBlockPos.getZ() - RANGE,
                playerBlockPos.getX() + RANGE, playerBlockPos.getY() + RANGE, playerBlockPos.getZ() + RANGE
        );
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, box, Entity::isAlive);

        for (int i = 0; i < Math.min(items.size(), MAX_ITEMS); i++){
            ItemEntity item = items.get(i);

            Vec3d playerPos = player.getPos();
            Vec3d itemPos = item.getPos();
            Vec3d direction = playerPos.subtract(itemPos).normalize();

            double speed = 0.1;
            Vec3d vel = direction.multiply(speed);
            item.setVelocity(vel); //pull w/ maniping velocity

            item.addVelocity(0, 0.05, 0);
        }




        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) return TypedActionResult.success(itemStack);
        if (!(itemStack.getItem() instanceof Lv1MagnetItem)) return TypedActionResult.pass(itemStack);

        long currentTime = world.getTime();
        Long lastClear = LAST_ROTO_TIMES.get(user);
        if (lastClear != null && currentTime - lastClear < 10) {
            log("Kewldown active, passing - lv1magnet");
            return TypedActionResult.pass(itemStack);
        }
        LAST_ROTO_TIMES.put(user, currentTime);


        if (user.isSneaking()){
            String turnedInto = rotatePull(itemStack);

            user.sendMessage(txt("Magnet is now: " + turnedInto), true);
            user.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.4f);

            // log("Magnet activity for " + user.getEntityName() + " is now " + turnedInto);
        }


        return TypedActionResult.success(itemStack);
    }

}