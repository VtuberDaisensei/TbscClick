package tbsc.clickmod.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.RayTraceResult;
import tbsc.clickmod.IRayTrace;

/**
 * @author tbsc on 02/09/2021
 */
public class RayTrace implements IRayTrace {

    private final RayTraceResult trace;

    public RayTrace(RayTraceResult trace) {
        this.trace = trace;
    }

    @Override
    public boolean isBlockTrace() {
        return trace.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    @Override
    public boolean isEntityTrace() {
        return trace.typeOfHit == RayTraceResult.Type.ENTITY;
    }

    @Override
    public boolean isMissType() {
        return trace.typeOfHit == RayTraceResult.Type.MISS;
    }

    @Override
    public boolean isLookingAtEntity(Minecraft minecraft) {
        return minecraft.pointedEntity != null;
    }

    @Override
    public void leftClickEntity(Minecraft minecraft) {
        if (minecraft.playerController != null && minecraft.player != null) {
            minecraft.playerController.attackEntity(minecraft.player, minecraft.pointedEntity);
        }
    }

    @Override
    public boolean isEmptyBlock(Minecraft minecraft) {
        return minecraft.world != null && minecraft.world.isAirBlock(trace.getBlockPos());
    }

    @Override
    public void leftClickBlock(Minecraft minecraft) {
        if (minecraft.playerController != null) {
            minecraft.playerController.clickBlock(trace.getBlockPos(), trace.sideHit);
        }
    }
}
