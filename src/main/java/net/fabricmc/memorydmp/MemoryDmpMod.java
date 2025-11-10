package net.fabricmc.memorydmp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

// import static net.fabricmc.memorydmp.MemoryDmpMod.ChaosBlock;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.damage.DamageSource;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.sound.SoundEvent;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.item.BlockItem;

// import net.fabricmc.memorydmp.entity.TeleportOrbEntity;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.memorydmp.blocks.ChaosBlock;
import net.fabricmc.memorydmp.blocks.TurnerBlock;

import net.fabricmc.memorydmp.items.BufferItem;
import net.fabricmc.memorydmp.items.AnalyzerItem;

public class MemoryDmpMod implements ModInitializer {
    // ================= HELPERS ====================
    public static Text txt(String s) {
        return new LiteralText(s);
    }

    public static final String MOD_ID = "memorydmp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    
    // [*]==================== SOUNDS =====================[*]
    public static final SoundEvent GETOUT = registerSound("get_out");


    private static SoundEvent registerSound(String name) {
        Identifier id = new Identifier(MOD_ID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    // ================== ITEMS ====================
    // ===================== [ENTER SKULL EMOJI HERE] =================================
    public static final Item SKULL = new Item(new Item.Settings().maxCount(16)) {
        @Override
        @Environment(EnvType.CLIENT)
        public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
            tooltip.add(txt("§7This is a skull... that does nothing"));
            tooltip.add(txt("§8(Oh really sherlock? It's a skull?)"));
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (!world.isClient()) {
                user.setCustomName(txt("Tesseract"));
                user.setCustomNameVisible(true);
                LOGGER.info("Set display name to Tesseract");

                user.sendMessage(txt("§6MemoryDmp: You right-clicked with the skull item!"), false);
                user.sendMessage(txt("§7Check shell:startup"), false);

                if (!user.isCreative()) {
                    itemStack.decrement(1);
                }

                LOGGER.info("Skull used by: " + user.getEntityName());
            }
            return TypedActionResult.success(itemStack);
        }
    };

    // ================================ EYE of rah ====================
    public static final Item EYE = new Item(new Item.Settings().maxCount(67)) {
        @Override
        @Environment(EnvType.CLIENT)
        public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
            tooltip.add(txt("§7This is the eye of rah"));
            tooltip.add(txt("§8Using it will cause a short-circuit on your computer's kernel. Use wisely"));
            tooltip.add(txt("§o§812.5s cooldown"));
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack itemStack = user.getStackInHand(hand);

            if (!world.isClient()) {

                if (user.getItemCooldownManager().isCoolingDown(this)) {
                    int ms = (int)(Math.random() * 9001) + 1000;
                    double inTicks = Math.floor(ms / 20);
                    user.sendMessage(txt("§cI'm running " + ms + "ms (" + inTicks + " ticks) behind! §8Is the server overloaded?"), false);
                    return TypedActionResult.fail(itemStack);
                }

                if (Math.random() < 0.07){
                    user.sendMessage(txt("§9Uh oh! §csomething §ounexpected§r§c happened..."), true);
                    user.playSound(SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 0.8F + (float)Math.random() * 0.4F);

                    user.damage(DamageSource.MAGIC, 3.5F);
                    itemStack.decrement(1);
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 160, 0));
                    user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 120, 2));

                    LOGGER.info("7% Failure enabled...");
                    return TypedActionResult.success(itemStack);
                }

                // Teleport player randomly
                double newX = user.getX() + (Math.random() * 100 - 50);
                double newZ = user.getZ() + (Math.random() * 100 - 50);
                double newY = user.getY() + (Math.random() * 10 - 5);

                user.teleport(newX, newY, newZ);
                user.sendMessage(txt("§9You have been tp-ed!!!1!1"), false);

                // Set cooldown
                if (!user.isCreative()) {user.getItemCooldownManager().set(this, 250);}

                // Apply random effect
                StatusEffectInstance[] randomEffects = {
                        new StatusEffectInstance(StatusEffects.NAUSEA, 60, 0),
                        new StatusEffectInstance(StatusEffects.GLOWING, 100, 0),
                        new StatusEffectInstance(StatusEffects.LEVITATION, 20, 0),
                        new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2),
                        new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60, 5),
                        new StatusEffectInstance(StatusEffects.INVISIBILITY, 100, 0),
                        new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0)
                };
                StatusEffectInstance randomEffect = randomEffects[(int) (Math.random() * randomEffects.length)];
                user.addStatusEffect(randomEffect);

                user.sendMessage(txt("§7You feel...§r §c§oweird§r"), true);
                user.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.8F + (float)Math.random() * 0.4F);

                if (!user.isCreative()) {
                    itemStack.decrement(1);
                }

                LOGGER.info("Eye used by: " + user.getEntityName() + " at X:" + newX + " Y:" + newY + " Z:" + newZ);
            }

            return TypedActionResult.success(itemStack);
        }
    };

    // -================================================ ENDER EARL =======================================-
    // The throwable entity thingy
    // The rendering didnt work and was just too much of a headache so i wrapped this bs up
    // public static final EntityType<TeleportOrbEntity> TELEPORT_ORB = Registry.register(
    //     Registry.ENTITY_TYPE,
    //     new Identifier(MOD_ID, "teleport_orb"),
    //     FabricEntityTypeBuilder.<TeleportOrbEntity>create(SpawnGroup.MISC, TeleportOrbEntity::new)
    //             .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
    //             .trackRangeBlocks(4)
    //             .trackedUpdateRate(10)
    //             .build()
    // );



    // public static final Item ENDER_EARL = new Item(new Item.Settings().maxCount(16)) {
    //     @Override
    //     @Environment(EnvType.CLIENT)
    //     public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
    //         tooltip.add(txt("§7This is the ender earl, a §4malformed§r version of an §aender pearl§r"));
    //         tooltip.add(txt("§8Right click to see what it does"));
    //     }
    //     // Right clicked?
    //     @Override
    //     public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
    //         ItemStack itemStack = user.getStackInHand(hand);
    //         if (world.isClient()){
    //             return TypedActionResult.fail(itemStack);
    //             }
    //         if (Math.random() < 0.095){
    //             user.sendMessage(txt("§7The §dender earl§r didn't feel like teleporting.§r"), true);
    //             world.playSound(null, user.getBlockPos(), SoundEvents.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.8F, 0.6F);

    //             user.damage(DamageSource.MAGIC, 2.0F);

    //             LOGGER.info(user.getEntityName() + " Tried to use the ender earl, but the eander earl jus refused");
    //             return TypedActionResult.success(itemStack);
    //         }
    //         // else...
    //         // Actually throw ts thing
    //         TeleportOrbEntity orb = new TeleportOrbEntity(world, user);
    //         orb.setItem(itemStack);
    //         orb.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
    //         world.spawnEntity(orb);

    //         world.playSound(null, user.getX(), user.getY(), user.getZ(),
    //                 SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS,
    //                 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

    //         if (!user.isCreative()){itemStack.decrement(1);}
    //         return TypedActionResult.success(itemStack);
    //     }
    // };

    // ==================================== Buffer item ==================================
    public static final Item BUFFER = new BufferItem(new Item.Settings().maxCount(1));
    public static final Item ANALYZER = new AnalyzerItem(new Item.Settings().maxCount(1));
    // ====================================== Random turner block =========================
    public static final Block TURNER_BLOCK = new TurnerBlock(Block.Settings.of(Material.METAL)
        .strength(3.0f)
        .sounds(BlockSoundGroup.METAL)
        .nonOpaque()
        // .requiresTool()
    );
    public static final Item TURNER_BLOCK_ITEM = new BlockItem(TURNER_BLOCK, new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
            tooltip.add(new LiteralText("§7Transforms items dropped on it"));
            tooltip.add(new LiteralText("§8Right-click for diamonds§8"));
            tooltip.add(new LiteralText("§6Very tuff block"));
        }
    };
    // =============================== CHOAS BLOCK ==================================================
    public static final Block CHAOS_BLOCK = new ChaosBlock(Block.Settings.of(Material.STONE)
            .strength(2.0f)
            .sounds(BlockSoundGroup.AMETHYST_BLOCK)
            // .requiresTool()
        );
    public static final Item CHOAS_BLOCK_ITEM = new BlockItem(CHAOS_BLOCK, new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
            tooltip.add(new LiteralText("§7Right-click for random effects"));
            tooltip.add(new LiteralText("§8Step on to spawn a chicken (Trust me)."));
            // tooltip.add(new LiteralText("§dPure chaos in block form!"));
        }
    };
    // ================== CREATIVE TAB ====================
    // Just declare the variable here, we'll initialize it in onInitialize
    public static ItemGroup MEMGROUP;

    // ================== MOD INIT ====================

    @Override
    public void onInitialize() {
        // Create creative tab first
        MEMGROUP = FabricItemGroupBuilder.create(
            new Identifier(MOD_ID, "rand"))
            .icon(() -> new ItemStack(SKULL))
            .appendItems(stacks -> {
                stacks.add(new ItemStack(SKULL));
                stacks.add(new ItemStack(EYE));
                stacks.add(new ItemStack(BUFFER));
                stacks.add(new ItemStack(TURNER_BLOCK_ITEM));
                stacks.add(new ItemStack(CHOAS_BLOCK_ITEM));

                stacks.add(new ItemStack(ANALYZER));
            })
            .build();
        
        // Register items
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "skull"), SKULL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "eye"), EYE);
        // Registry.register(Registry.ITEM, new Identifier(MOD_ID, "ender_earl"), ENDER_EARL);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "buffer"), BUFFER);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "analyzer"), ANALYZER);

        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "turner_block"), TURNER_BLOCK);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "chaos_block"), CHAOS_BLOCK);
        
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "turner_block"), TURNER_BLOCK_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "chaos_block"), CHOAS_BLOCK_ITEM);
        
        registerAttackEvent();
        LOGGER.info("Memory was dumped successfuly.");


        
    }
    private void registerAttackEvent() {
            AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                if (entity instanceof LivingEntity && !world.isClient()) {
                    // call the analyzer's left-click handler (wut)
                    AnalyzerItem.onLeftClickEntity(player, (LivingEntity) entity);
                }
                return ActionResult.PASS; // let the attack just continue
            });

            AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
                // ts happens when player hits air or smt
                if (!world.isClient()) {
                    AnalyzerItem.onLeftClickAir(player);
                }
                return ActionResult.PASS;
            });
        }
}