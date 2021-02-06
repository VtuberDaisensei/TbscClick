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
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Mod(TbscClick.MODID)
public class TbscClick {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tbscclick";
    public static boolean shouldLeftClick = false;
    public static boolean shouldSmartAttack = false;
    public static boolean shouldRightClick = false;
//    public static boolean holdingLeftButton = false;
    public static boolean holdingRightButton = false;
    public static KeyBinding keyToggleRight;
    public static KeyBinding keyToggleLeft;
    public static KeyBinding keyToggleSmartAttack;
    public static KeyBinding keyToggleHoldRight;
//    public static KeyBinding keyToggleHoldLeft;

    private Minecraft minecraft = null;

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
//        keyToggleHoldLeft = new KeyBinding("key.tbscclick.toggleholdleft", GLFW.GLFW_KEY_N, "key.categories.tbscclick");

        ClientRegistry.registerKeyBinding(keyToggleRight);
        ClientRegistry.registerKeyBinding(keyToggleLeft);
        ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
        ClientRegistry.registerKeyBinding(keyToggleHoldRight);
//        ClientRegistry.registerKeyBinding(keyToggleHoldLeft);

        minecraft = Minecraft.getInstance();
    }

    boolean holdLeftWasPressed = false;
    boolean holdRightWasPressed = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!minecraft.isGamePaused()) {
            /* AUTO LEFT CLICK */

            if (shouldLeftClick) {
                leftClick(false);
            }

            /* SMART ATTACK */

            if (shouldSmartAttack) {
                leftClick(true);
            }

            /* AUTO RIGHT CLICK */

            if (shouldRightClick) {
                mcReflRightClick();
            }

//            /* HOLD LEFT CLICK */
//
//            if (holdingLeftButton /*&& !isInGame()*/) {
//                mcReflSetLeftClickCounter(0);
//                mcReflSendClickBlockToController(true);
//            }
//
//            // Init, begin pressing the key when toggled on
//            if (holdLeftWasPressed && !keyToggleHoldLeft.isPressed()) {
//                holdingLeftButton = !holdingLeftButton;
////                setHoldButton(minecraft.gameSettings.keyBindAttack, holdingLeftButton);
//                holdLeftWasPressed = false;
//            }
//
//            // If returned to game from another screen, key should be held and isn't pressed, make it pressed again
////            if (holdingLeftButton && isInGame() && !minecraft.gameSettings.keyBindAttack.isPressed()) {
////                setHoldButton(minecraft.gameSettings.keyBindAttack, true);
////            }

            /* HOLD RIGHT CLICK */

            // When in another screen, key should be held and right click cooldown is over, right click
            if (holdingRightButton && !isInGame() && mcReflRightClickDelayTimer() == 0 && minecraft.player != null && !minecraft.player.isHandActive()) {
                mcReflRightClick();
            }

            if (holdRightWasPressed && !keyToggleHoldRight.isPressed()) {
                holdingRightButton = !holdingRightButton;
                setHoldButton(minecraft.gameSettings.keyBindUseItem, holdingRightButton);
                holdRightWasPressed = false;
            }
        }
    }

    public boolean isInGame() {
        return minecraft.currentScreen == null;
    }

    @SubscribeEvent
    public void onInitGuiPre(GuiScreenEvent.InitGuiEvent.Pre event) {
        // Triggered when opening any screen that isn't the game
        // Opening a screen clears all pressed keybinds, so make it pressed again
        // This will persist when exiting the screen
        if (holdingRightButton) {
            setHoldButton(minecraft.gameSettings.keyBindUseItem, true);
        }
    }

    /**
     * @param smart If true, waits until the attack cooldown is over before clicking again, despite being called.
     */
    private void leftClick(boolean smart) {
        if (minecraft.playerController != null && minecraft.player != null) {
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

    private void setHoldButton(KeyBinding key, boolean held) {
        KeyBinding.setKeyBindState(key.getKey(), held);
    }

    /**
     * Calls Minecraft.rightClickMouse using reflection.
     */
    private void mcReflRightClick() {
        mcReflInvokeMethod("rightClickMouse");
    }

    private void mcReflSetLeftClickCounter(int leftClickCounter) {
        try {
            Field field = minecraft.getClass().getDeclaredField("leftClickCounter");
            field.setAccessible(true);
            field.set(minecraft, leftClickCounter);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void mcReflLeftClick(int leftClickCounter) {
        mcReflSetLeftClickCounter(leftClickCounter);
        mcReflInvokeMethod("clickMouse");
    }

    private int mcReflRightClickDelayTimer() {
        try {
            Field timer = minecraft.getClass().getDeclaredField("rightClickDelayTimer");
            timer.setAccessible(true);
            return (int) timer.get(minecraft);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

//    private void mcReflSendClickBlockToController(boolean leftClick) {
//        mcReflInvokeMethod("sendClickBlockToController", boolean.class, leftClick);
//    }

    private void mcReflInvokeMethod(String name) {
        mcReflInvokeMethod(name, new Class[0], new Object[0]);
    }

//    private void mcReflInvokeMethod(String name, Class<?> type, Object arg) {
//        mcReflInvokeMethod(name, new Class[] { type }, new Object[] { arg });
//    }

    private void mcReflInvokeMethod(String name, Class<?>[] types, Object[] args) {
        try {
            Method method = minecraft.getClass().getDeclaredMethod(name, types);
            method.setAccessible(true);
            method.invoke(minecraft, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (keyToggleLeft.isPressed()) {
//            disableHoldLeftByConflict();
            disableSmartAttackByConflict();
            shouldLeftClick = !shouldLeftClick;
        }
        if (keyToggleSmartAttack.isPressed()) {
//            disableHoldLeftByConflict();
            disableAutoLeftByConflict();
            shouldSmartAttack = !shouldSmartAttack;
        }
        if (keyToggleRight.isPressed()) {
            disableHoldRightByConflict();
            shouldRightClick = !shouldRightClick;
        }

//        if (keyToggleHoldLeft.isPressed()) {
//            disableAutoLeftByConflict();
//            disableSmartAttackByConflict();
//            holdLeftWasPressed = true;
//        }
        if (keyToggleHoldRight.isPressed()) {
            disableAutoRightByConflict();
            holdRightWasPressed = true;
        }
    }

//    private void disableHoldLeftByConflict() {
//        if (holdingLeftButton) {
//            holdLeftWasPressed = true;
//            sendMessage("Conflict: Disabled holding left button.");
//        }
//    }

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
//        if (holdingLeftButton) {
//            renderList.add("Holding Left Button");
//        }
        if (holdingRightButton) {
            renderList.add("Holding Right Button");
        }

        for (int i = 0; i < renderList.size(); ++i) {
            String renderString = renderList.get(i);
            minecraft.fontRenderer.drawString(new MatrixStack(), TextFormatting.BOLD + renderString, 6, 6 + 10 * i, 0xFF0000);
        }
    }

}
