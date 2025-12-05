package net.fabricmc.memorydmp.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        System.out.println("[+][*] DataGen Registered, GEN-ING STUFF");


        // --- gen resources ---
        fabricDataGenerator.addProvider(ModelGenerator::new);
        // fabricDataGenerator.addProvider(RecipeGenerator::new); <- done w/ python instead. better imo



        System.out.println("[+] DataGen COMPLETE");
    }
}