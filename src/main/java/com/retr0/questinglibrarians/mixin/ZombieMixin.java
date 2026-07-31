package com.retr0.questinglibrarians.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin {

    @Shadow
    public abstract boolean convertVillagerToZombieVillager(ServerLevel level, Villager villager);

    /**
     * Guarantees 100% zombification when a Zombie kills a Villager across all game difficulties.
     */
    @Inject(method = "killedEntity", at = @At("HEAD"), cancellable = true)
    private void guaranteeZombification(ServerLevel level, LivingEntity victim, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (!com.retr0.questinglibrarians.config.QuestingLibrariansConfig.guaranteeZombification) {
            return;
        }
        if (victim instanceof Villager villager) {
            Zombie zombie = (Zombie) (Object) this;
            boolean converted = zombie.convertVillagerToZombieVillager(level, villager);
            cir.setReturnValue(converted);
        }
    }
}
