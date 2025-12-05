package net.fabricmc.memorydmp.items;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class BlockPointItem extends Item {
    private static final Map<PlayerEntity, Long> LAST_CLEAR_TIMES = new WeakHashMap<>();



    public BlockPointItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§7Right click on a block to set a §bblockpoint§r there."));
        if (!hasWaypoint(stack)){ tooltip.add(txt("No blockpoint.json set yet.")); }
        else{ tooltip.add(txt("Blockpoint is set.")); }
    }



    public ActionResult useOnBlock(ItemUsageContext ctx){
        PlayerEntity user = ctx.getPlayer();
        World world = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Hand hand = ctx.getHand();
        ItemStack itemStack = ctx.getStack();


        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(itemStack.getItem() instanceof BlockPointItem)) return ActionResult.FAIL;

        itemStack.getOrCreateNbt().putIntArray(
                "TargetPos", new int[]{blockPos.getX(), blockPos.getY(), blockPos.getZ()}
        );

        if (user != null) user.sendMessage(txt("§ablockpoint for §e" + world.getBlockState(blockPos).getBlock().getName().getString() + "§a set successfully!!§r"), true);
        world.playSound(null, blockPos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.7f, world.random.nextFloat() *0.4f);


        log("user " + user.getEntityName() + "set waypoint at " + blockPos + " for " + world.getBlockState(blockPos) + ".");



        return ActionResult.SUCCESS;
    }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient() || !(entity instanceof PlayerEntity) || !hasWaypoint(stack)) return;
        if (!(world.getTime() % 20 == 0)) return;

        PlayerEntity player = (PlayerEntity) entity;

        if (!((player.getMainHandStack().getItem() instanceof BlockPointItem))) return;

        int[] pos = stack.getNbt().getIntArray("TargetPos");
        BlockPos target = new BlockPos(pos[0], pos[1], pos[2]);
        String arrow = getDirectionArrow(calculateAngleToTarget(player, target));
        int dist = (int) player.getPos().distanceTo(Vec3d.ofCenter(target));


        player.sendMessage(txt("§b" + "" + " §7(" + dist + "m)"), true);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (world.isClient()) TypedActionResult.success(itemStack);
        if (!hasWaypoint(itemStack)){
            user.sendMessage(txt("You don't have a blockpoint.json to clear gng."), true);
            return TypedActionResult.pass(itemStack);
        }


        long currentTime = world.getTime();
        Long lastClear = LAST_CLEAR_TIMES.get(user);
        if (lastClear != null && currentTime - lastClear < 10) {
            MemoryDmpMod.LOGGER.info("Cooldown for blockpoint.json active - returning PASS");
            return TypedActionResult.pass(itemStack);
        }
        LAST_CLEAR_TIMES.put(user, currentTime);


        if (user.isSneaking()){
            log("Clearing blockpoint.json...");
            if (itemStack.getNbt() != null) itemStack.getNbt().remove("TargetPos");

            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            user.sendMessage(txt("Cleared all blockpoints"), false);
        }



        return TypedActionResult.success(itemStack);
    }




//    ============ helper methods ==============
    private static double calculateAngleToTarget(PlayerEntity player, BlockPos targetPos) {
        // Get player position
        double playerX = player.getX();
        double playerZ = player.getZ();

        // Calculate angle from player to target
        double deltaX = targetPos.getX() - playerX;
        double deltaZ = targetPos.getZ() - playerZ;

        // Math.atan2 gives angle in radians, convert to degrees
        double angleRad = Math.atan2(deltaZ, deltaX);
        double angleDeg = Math.toDegrees(angleRad);

        // Adjust for Minecraft's coordinate system (0° = South)
        angleDeg -= 90.0; // Rotate so 0° points to target

        // Normalize to 0-360 range
        if (angleDeg < 0) angleDeg += 360.0;

        return angleDeg;
    }


    private String getDirectionArrow(double angle) {
        String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
        int index = (int) Math.round(angle / 45.0) % 8;
        return arrows[index];
    }



    private boolean hasWaypoint(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("TargetPos");
    }

}
