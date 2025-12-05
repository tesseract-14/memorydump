package net.fabricmc.memorydmp.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;

import java.util.function.Consumer;

public class RecipeGenerator extends FabricRecipeProvider {

    public RecipeGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }




    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {

        ShapedRecipeJsonBuilder.create(MemoryDmpMod.LV1_MAGNET)
                .pattern("I I")
                .pattern("R R")
                .pattern("RRR")
                .input('I', Items.IRON_INGOT)
                .input('R', Items.REDSTONE)
                .criterion("has_redstone", conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter, "magnet_lv1");
        ShapedRecipeJsonBuilder.create(MemoryDmpMod.LV2_MAGNET)
                .pattern("IRI")
                .pattern("I I")
                .pattern("III")
                .input('I', Items.IRON_INGOT)
                .input('R', Items.REDSTONE_BLOCK)
                .criterion("has_redstone", conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter, "magnet_lv2");
        ShapedRecipeJsonBuilder.create(MemoryDmpMod.LV3_MAGNET)
                .pattern("GRG")
                .pattern("I I")
                .pattern("III")
                .input('I', Items.IRON_BLOCK)
                .input('R', Items.REDSTONE_BLOCK)
                .input('G', Items.IRON_INGOT)
                .criterion("has_redstone", conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter, "magnet_lv3");

    }

}
