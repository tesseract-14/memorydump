package net.fabricmc.memorydmp.items.OrbitalTNTCannons;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class StabShot extends FishingRodItem {
    // public static final Map<StabShot, Boolean> BOBBER_STATES = new WeakHashMap<>();
    public static final Map<StabShotBobberEntity, ItemStack> ACTIVE_BOBBERS = new WeakHashMap<>();


    public StabShot(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(txt("§8Use the fishing rod to see it's functionality"));
    }




    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient()) {
            ItemStack stack = user.getStackInHand(hand);

            for (StabShotBobberEntity bob : ACTIVE_BOBBERS.keySet()) {
                if (StabShotBobberEntity.didAlreadyThrow(bob, stack)) {
                    StabShotBobberEntity.onRetract(bob.getPos(), world, bob);
                    return TypedActionResult.success(stack);
                }
            }

            // No existing bobber → throw a new one
            StabShotBobberEntity bobber = new StabShotBobberEntity(user, world);
            bobber.setPosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
            bobber.setOwner(user, stack);
            world.spawnEntity(bobber);

            // Track this bobber with the rod that threw it
            ACTIVE_BOBBERS.put(bobber, stack);
        }

        // Play throw sound
        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS,
                0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

        return TypedActionResult.success(user.getStackInHand(hand));
    }








}
