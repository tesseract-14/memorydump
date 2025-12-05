package net.fabricmc.memorydmp.items;

import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.fabricmc.memorydmp.MemoryDmpMod.log;
import static net.fabricmc.memorydmp.MemoryDmpMod.txt;

public class WeatherItem extends Item {
    private static final Map<PlayerEntity, Long> LAST_USE_TIMES = new WeakHashMap<>();


    public WeatherItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context){
        tooltip.add(txt("§7Changes weather, to put it simply."));

        String current = getWeather(stack);
        String weatherName = switch(current) {
            case "c" -> "§eClear";
            case "r" -> "§1Rain";
            case "s" -> "§6Thunder";
            default -> "§cUnknown";
        };

        tooltip.add(txt("Current: " + weatherName));
        tooltip.add(txt("§8Shift-click to cycle | Click to apply"));
    }



    // ==== HELPER METHODS ==================
    private static String getWeather(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("Weather")) {
            return stack.getNbt().getString("Weather");
        }
        return "c";
    }

    private static void setWeather(ItemStack stack, String weather) {
        stack.getOrCreateNbt().putString("Weather", weather);
    }

    private static String getNext(String cur){
        String returnThis = "";

        switch (cur){
            case "c" -> returnThis = "r";
            case "r" -> returnThis = "s";
            case "s" -> returnThis = "c";
        }

        return returnThis;
    }
    // ==== HELPER METHODS ================



    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
        ItemStack itemStack = user.getMainHandStack();

        if (world.isClient()) return TypedActionResult.success(itemStack);
        long currentTime = world.getTime();

        Long lastClear = LAST_USE_TIMES.get(user);
        if (lastClear != null && currentTime - lastClear < 10) {
            MemoryDmpMod.LOGGER.info("Cooldown active - passing | WeatherItem");
            return TypedActionResult.pass(itemStack);
        }
        LAST_USE_TIMES.put(user, currentTime);


        if (user.isSneaking()){
            String currentWeather = getWeather(itemStack);
            String next = getNext(currentWeather);

            setWeather(itemStack, next);
            log("Next weather set to: " + next);


            switch (next){
                case "c" -> user.sendMessage(txt("Weather is: §l§eClear"), true);
                case "r" -> user.sendMessage(txt("Weather is: §l§1Rain"), true);
                case "s" -> user.sendMessage(txt("Weather is: §l§6Thunder"), true);
                default -> user.sendMessage(txt("§l???"), true);
            }

            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, world.random.nextFloat() * 0.4f);

            return TypedActionResult.success(itemStack);
        }

        ServerWorld serverWorld = (ServerWorld) world;
        String currentWeather = getWeather(itemStack);

        switch (currentWeather){
            case "c" -> {
                serverWorld.setWeather(20000, 0, false, false);
                log("Set weather to clear");
            }
            case "r" -> {
                serverWorld.setWeather(0, 20000, true, false);
                log("Set weather to rain");
            }
            case "s" -> {
                serverWorld.setWeather(0, 20000, true, true);
                log("Set weather to thunder");
            }

        }
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.WEATHER, 0.5f, 1.0f);


        return TypedActionResult.success(itemStack);
    }
}
