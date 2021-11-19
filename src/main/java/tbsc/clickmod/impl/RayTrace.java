package tbsc.clickmod.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
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
        return trace instanceof BlockRayTraceResult;
    }

    private BlockRayTraceResult block() {
        return (BlockRayTraceResult) trace;
    }

    @Override
    public boolean isEntityTrace() {
        return trace instanceof EntityRayTraceResult;
    }

    @Override
    public boolean isMissType() {
        return trace.getType() == RayTraceResult.Type.MISS;
    }

    @Override
    public boolean isLookingAtEntity(Minecraft minecraft) {
        return minecraft.crosshairPickEntity != null;
    }

    @Override
    public void leftClickEntity(Minecraft minecraft) {
        if (minecraft.gameMode != null && minecraft.player != null && minecraft.crosshairPickEntity != null) {
            minecraft.gameMode.attack(minecraft.player, minecraft.crosshairPickEntity);
        }
    }

    @Override
    public boolean isEmptyBlock(Minecraft minecraft) {
        return minecraft.level != null && minecraft.level.isEmptyBlock(block().getBlockPos());
    }

    @Override
    public void leftClickBlock(Minecraft minecraft) {
        if (minecraft.gameMode != null) {
            minecraft.gameMode.startDestroyBlock(block().getBlockPos(), block().getDirection());
        }
    }
}
