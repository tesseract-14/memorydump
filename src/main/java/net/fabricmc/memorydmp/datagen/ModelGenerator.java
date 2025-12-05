package net.fabricmc.memorydmp.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.memorydmp.MemoryDmpMod;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.entity.ai.brain.Memory;

public class ModelGenerator extends FabricModelProvider {
    public ModelGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // no blocks rn
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(MemoryDmpMod.WEATHER, Models.GENERATED);
        itemModelGenerator.register(MemoryDmpMod.LV1_MAGNET, Models.GENERATED);
        itemModelGenerator.register(MemoryDmpMod.LV2_MAGNET, Models.GENERATED);
        itemModelGenerator.register(MemoryDmpMod.LV3_MAGNET, Models.GENERATED);
    }
}