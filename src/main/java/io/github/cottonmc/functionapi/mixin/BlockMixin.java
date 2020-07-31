package io.github.cottonmc.functionapi.mixin;

import io.github.cottonmc.functionapi.ServerCommandSourceFactory;
import io.github.cottonmc.functionapi.api.FunctionAPIIdentifier;
import io.github.cottonmc.functionapi.api.commands.CommandSourceExtension;
import io.github.cottonmc.functionapi.api.script.ScriptedObject;
import io.github.cottonmc.functionapi.events.GlobalEventContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * adds scripting functionality to the block class.
 */
@Mixin(value = Block.class, priority = 0)
@Implements(@Interface(iface = ScriptedObject.class, prefix = "api_scripted$", remap = Interface.Remap.NONE))
public abstract class BlockMixin implements ScriptedObject{

    private Identifier thisId = null;

    @Inject(
            at = @At("TAIL"),
            method = "onPlaced"
    )
    private void place(World world_1, BlockPos blockPos_1, BlockState blockState_1, LivingEntity livingEntity_1, ItemStack itemStack_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            GlobalEventContainer.getInstance().executeEventBlocking(this, "after/placed", ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, livingEntity_1));
            GlobalEventContainer.getInstance().executeEvent(this, "placed", ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, livingEntity_1));
        }
    }


    @Inject(
            at = @At("HEAD"),
            method = "onPlaced",
            cancellable = true
    )
    private void placeBefore(World world_1, BlockPos blockPos_1, BlockState blockState_1, LivingEntity livingEntity_1, ItemStack itemStack_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            ServerCommandSource serverCommandSource = GlobalEventContainer.getInstance().executeEventBlocking(this, "before/placed", ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, livingEntity_1));
            if (((CommandSourceExtension) serverCommandSource).isCancelled()) {
                ci.cancel();
            }
        }
    }


    @Inject(
            at = @At("HEAD"),
            method = "onBroken",
            cancellable = true
    )
    private void brokenBefore(WorldAccess world_1, BlockPos blockPos_1, BlockState blockState_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            ServerCommandSource serverCommandSource = GlobalEventContainer.getInstance().executeEventBlocking(this, "before/broken", ServerCommandSourceFactory.INSTANCE.create((ServerWorld) world_1, (Block) (Object) this, blockPos_1));

            if (((CommandSourceExtension) serverCommandSource).isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(
            at = @At("TAIL"),
            method = "onBroken"
    )
    private void broken(WorldAccess world_1, BlockPos blockPos_1, BlockState blockState_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            GlobalEventContainer.getInstance().executeEvent(this, "broken", ServerCommandSourceFactory.INSTANCE.create(((ServerWorld) world_1).getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1));
        }
    }

    @Inject(
            at = @At("TAIL"),
            method = "onDestroyedByExplosion"
    )
    private void exploded(World world_1, BlockPos blockPos_1, Explosion explosion_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            GlobalEventContainer.getInstance().executeEvent(this, "exploded", ServerCommandSourceFactory.INSTANCE.create(((ServerWorld) world_1).getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1));
        }
    }


    @Inject(at = @At("TAIL"), method = "onSteppedOn")
    private void onSteppedOn(World world_1, BlockPos blockPos_1, Entity entity_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            GlobalEventContainer.getInstance().executeEvent(this, "stepped_on", ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, entity_1));
        }
    }


    @Inject(
            at = @At("HEAD"),
            method = "onLandedUpon", cancellable = true
    )
    private void onLandedUponBefore(World world_1, BlockPos blockPos_1, Entity entity_1, float float_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            ServerCommandSource serverCommandSource = GlobalEventContainer.getInstance().executeEventBlocking(this, "before/entity_landed",  ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, entity_1));

            if (((CommandSourceExtension) serverCommandSource).isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(
            at = @At("TAIL"),
            method = "onLandedUpon"
    )
    private void onLandedUpon(World world_1, BlockPos blockPos_1, Entity entity_1, float float_1, CallbackInfo ci) {
        if (world_1 instanceof ServerWorld) {
            GlobalEventContainer.getInstance().executeEvent(this, "entity_landed", ServerCommandSourceFactory.INSTANCE.create(world_1.getServer(), (ServerWorld) world_1, (Block) (Object) this, blockPos_1, entity_1));
        }
    }

    /**
     * Dynamically gets the id of this block instance.
     */
    public FunctionAPIIdentifier api_scripted$getEventID() {
        if (thisId == null) {
            thisId = Registry.BLOCK.getId((Block) (Object) this);
        }
        return (FunctionAPIIdentifier)thisId;
    }

    public String api_scripted$getEventType() {
        return "block";
    }

}
