package net.fabricmc.memorydmp.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class EntityRiderItem extends Item {
    private static final int MAXDUR = 67;

    public EntityRiderItem(Item.Settings settings){
        super(settings.maxDamage(MAXDUR));
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§7Right-click entities to ride them"));
        tooltip.add(txt("§8P.S. you can also ride players"));
    }

    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand){
        World world = user.getWorld();
        ItemStack mainHandStack = user.getMainHandStack();

        if (world.isClient()) return ActionResult.SUCCESS;
        if (!(mainHandStack.getItem() instanceof EntityRiderItem)) return ActionResult.PASS;
        if (entity.hasVehicle()){
            user.sendMessage(txt("target already has a 'vehicle' on them"), true);
            return ActionResult.FAIL;
        }

        boolean didMeatRide = user.startRiding(entity, true);
        stack.damage(1, user, (p) -> p.sendToolBreakStatus(Hand.MAIN_HAND));

        log("Player " + user.getEntityName() + " started riding " + entity.getEntityName());

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        if (entity instanceof HostileEntity) {
            scheduler.schedule(() -> {
                user.sendMessage(txt("Careful, riding a hostile mob rn."), true);
            }, 1, TimeUnit.SECONDS); // 1 second delay
        }


        return didMeatRide? ActionResult.SUCCESS : ActionResult.FAIL;
    }


    @Override
    public int getItemBarColor(ItemStack stack) {
        float durabilityRatio = Math.max(0.0F, (float)(MAXDUR - stack.getDamage()) / (float)MAXDUR);
        return durabilityRatio < 0.5F ? 0xFF0000 : 0x00FF00;
    }

}