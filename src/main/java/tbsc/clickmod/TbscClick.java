package tbsc.clickmod;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mod(TbscClick.MODID)
public class TbscClick {

    public static final String MODID = "tbscclick";
    public static boolean shouldLeftClick = false;
    public static boolean shouldSmartAttack = false;
    public static boolean shouldRightClick = false;
    public static boolean holdingLeftButton = false;
    public static boolean holdingRightButton = false;
    public static KeyBinding keyToggleRight;
    public static KeyBinding keyToggleLeft;
    public static KeyBinding keyToggleSmartAttack;
    public static KeyBinding keyToggleHoldRight;
    public static KeyBinding keyToggleHoldLeft;

    public TbscClick() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetupEvent);
    }

    @SubscribeEvent
    public void onClientSetupEvent(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        keyToggleRight = new KeyBinding("key.tbscclick.toggleright", GLFW.GLFW_KEY_G, "key.categories.tbscclick");
        keyToggleLeft = new KeyBinding("key.tbscclick.toggleleft", GLFW.GLFW_KEY_H, "key.categories.tbscclick");
        keyToggleSmartAttack = new KeyBinding("key.tbscclick.togglesmartattack", GLFW.GLFW_KEY_V, "key.categories.tbscclick");
        keyToggleHoldRight = new KeyBinding("key.tbscclick.toggleholdright", GLFW.GLFW_KEY_B, "key.categories.tbscclick");
        keyToggleHoldLeft = new KeyBinding("key.tbscclick.toggleholdleft", GLFW.GLFW_KEY_N, "key.categories.tbscclick");

        ClientRegistry.registerKeyBinding(keyToggleRight);
        ClientRegistry.registerKeyBinding(keyToggleLeft);
        ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
        ClientRegistry.registerKeyBinding(keyToggleHoldRight);
        ClientRegistry.registerKeyBinding(keyToggleHoldLeft);
    }

    boolean holdLeftWasPressed = false;
    boolean holdRightWasPressed = false;

    private Minecraft minecraft = null;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (minecraft == null) {
            minecraft = Minecraft.getInstance();
        }

        if (!minecraft.isGamePaused()) {
            checkLeftClick(shouldLeftClick, false);

            checkLeftClick(shouldSmartAttack, true);

            if (shouldRightClick) {
                try {
                    Method click = minecraft.getClass().getDeclaredMethod("rightClickMouse");
                    click.setAccessible(true);
                    click.invoke(minecraft);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            if (holdLeftWasPressed && !keyToggleHoldLeft.isPressed()) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindAttack.getKey(), !holdingLeftButton);
                holdingLeftButton = !holdingLeftButton;
                holdLeftWasPressed = false;
            }

            if (holdRightWasPressed && !keyToggleHoldRight.isPressed()) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindUseItem.getKey(), !holdingRightButton);
                holdingRightButton = !holdingRightButton;
                holdRightWasPressed = false;
            }
        }
    }

    /**
     * @param isEnabled Clicks the left button if true
     * @param smart Waits until the attack cooldown is over before clicking again, despite isEnabled being true.
     */
    private void checkLeftClick(boolean isEnabled, boolean smart) {
        if (isEnabled && minecraft.playerController != null && minecraft.player != null) {
            RayTraceResult rayTrace = minecraft.objectMouseOver;
            InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, minecraft.gameSettings.keyBindAttack, Hand.MAIN_HAND);
            if (!smart || minecraft.player.getCooledAttackStrength(0) == 1.0F) {
                if (rayTrace instanceof BlockRayTraceResult && rayTrace.getType() != RayTraceResult.Type.MISS) {
                    BlockRayTraceResult blockRayTrace = (BlockRayTraceResult) rayTrace;
                    if (minecraft.world != null && !minecraft.world.isAirBlock(blockRayTrace.getPos()))
                        minecraft.playerController.clickBlock(blockRayTrace.getPos(), blockRayTrace.getFace());
                } else if (rayTrace instanceof EntityRayTraceResult && minecraft.pointedEntity != null) {
                    minecraft.playerController.attackEntity(minecraft.player, minecraft.pointedEntity);
                }
                if (inputEvent.shouldSwingHand())
                    minecraft.player.swingArm(Hand.MAIN_HAND);
            }
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (keyToggleLeft.isPressed()) {
            disableHoldLeftByConflict();
            disableSmartAttackByConflict();
            shouldLeftClick = !shouldLeftClick;
        }
        if (keyToggleSmartAttack.isPressed()) {
            disableHoldLeftByConflict();
            disableAutoLeftByConflict();
            shouldSmartAttack = !shouldSmartAttack;
        }
        if (keyToggleRight.isPressed()) {
            disableHoldRightByConflict();
            shouldRightClick = !shouldRightClick;
        }

        if (keyToggleHoldLeft.isPressed()) {
            disableAutoLeftByConflict();
            disableSmartAttackByConflict();
            holdLeftWasPressed = true;
        }
        if (keyToggleHoldRight.isPressed()) {
            disableAutoRightByConflict();
            holdRightWasPressed = true;
        }
    }

    private void disableHoldLeftByConflict() {
        if (holdingLeftButton) {
            holdLeftWasPressed = true;
            sendMessage("Conflict: Disabled holding left button.");
        }
    }

    private void disableAutoLeftByConflict() {
        if (shouldLeftClick) {
            shouldLeftClick = false;
            sendMessage("Conflict: Disabled auto click left button.");
        }
    }

    private void disableSmartAttackByConflict() {
        if (shouldSmartAttack) {
            shouldSmartAttack = false;
            sendMessage("Conflict: Disabled auto smart attack.");
        }
    }

    private void disableHoldRightByConflict() {
        if (holdingRightButton) {
            holdRightWasPressed = true;
            sendMessage("Conflict: Disabled holding right button.");
        }
    }

    private void disableAutoRightByConflict() {
        if (shouldRightClick) {
            shouldRightClick = false;
            sendMessage("Conflict: Disabled auto clicking right button.");
        }
    }

    private void sendMessage(String message) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(TextFormatting.RED + message));
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) return;

        List<String> renderList = new ArrayList<>();

        if (shouldLeftClick) {
            renderList.add("Auto Left Clicking");
        }
        if (shouldRightClick) {
            renderList.add("Auto Right Clicking");
        }
        if (shouldSmartAttack) {
            renderList.add("Auto Smart Attacking");
        }
        if (holdingLeftButton) {
            renderList.add("Holding Left Button");
        }
        if (holdingRightButton) {
            renderList.add("Holding Right Button");
        }

        for (int i = 0; i < renderList.size(); ++i) {
            String renderString = renderList.get(i);
            minecraft.fontRenderer.drawString(new MatrixStack(), TextFormatting.BOLD + renderString, 6, 6 + 10 * i, 0xFF0000);
        }
    }

}
