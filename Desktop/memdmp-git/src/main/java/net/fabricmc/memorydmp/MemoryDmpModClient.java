package net.fabricmc.memorydmp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.memorydmp.client.renderer.StabShotBobberRenderer;
import net.fabricmc.memorydmp.items.OrbitalTNTCannons.StabShotBobberEntity;

import java.util.logging.Logger;

public class MemoryDmpModClient implements ClientModInitializer {
//    Logger LOGGER = Logger.getLogger("memorydmp client");


    @Override
    public void onInitializeClient() {
//        EntityRendererRegistry.register(MemoryDmpMod.STABSHOT_BOBBER, StabShotBobberRenderer::new);


        print("memorydmp client init-ed!!1!1");
    }

    public static void print(String msg){ System.out.println("[memorydmp] " + msg); }

}
