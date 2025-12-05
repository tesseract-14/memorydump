package net.fabricmc.memorydmp.items.OrbitalTNTCannons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static net.fabricmc.memorydmp.items.OrbitalTNTCannons.StabShot.ACTIVE_BOBBERS;

public class StabShotBobberEntity extends Entity {
    private PlayerEntity owner;
    private boolean hitGround = false;

    public StabShotBobberEntity(PlayerEntity type, World world) {
        super(type.getType(), world);
    }





    @Override
    public void tick() {
        super.tick();

        // Simple gravity & motion
        this.setVelocity(this.getVelocity().add(0, -0.03, 0)); // gravity
        this.move(MovementType.SELF, this.getVelocity());

        // Check collision with ground
        if (!hitGround && this.onGround) {
            hitGround = true;
            // Optionally play a sound or particle effect
        }

        if (this.getOwner() == null) return;
        if (this.distanceTo(this.getOwner()) > 15.0f){ this.discard(); }

    }

    public static void onRetract(Vec3d pos, World world, StabShotBobberEntity bobber) {

        ItemStack rod = ACTIVE_BOBBERS.get(bobber);
        rod.damage(69, bobber.getOwner(), (player) -> player.sendToolBreakStatus(Hand.MAIN_HAND));

        spawnTNT(pos, world);

        // Remove bobber
        ACTIVE_BOBBERS.remove(bobber);
        bobber.discard();
    }

    // Optional: store owner
    public void setOwner(PlayerEntity owner, ItemStack rod) {
        this.owner = owner;
        ACTIVE_BOBBERS.put(this, rod);
    }


    public PlayerEntity getOwner() {
        return this.owner;
    }

    public static boolean didAlreadyThrow(StabShotBobberEntity bobber, ItemStack rod){
        return ACTIVE_BOBBERS.get(bobber) == rod;
    }






    @Override
    protected void initDataTracker() {

    }



    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    public static void spawnTNT(Vec3d pos, World world) {
        TntEntity tnt = new TntEntity(EntityType.TNT, world);
        tnt.setFuse(5);
        tnt.setPosition(pos);
        world.spawnEntity(tnt);
    }

}
