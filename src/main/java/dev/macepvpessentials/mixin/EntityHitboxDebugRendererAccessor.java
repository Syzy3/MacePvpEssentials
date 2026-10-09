package dev.macepvpessentials.mixin;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets the mod draw one entity's hitbox with the game's own hitbox code. */
@Mixin(EntityHitboxDebugRenderer.class)
public interface EntityHitboxDebugRendererAccessor {

    @Invoker("showHitboxes")
    void macepvpessentials$showHitboxes(Entity entity, float partialTicks, boolean isServerEntity);
}
