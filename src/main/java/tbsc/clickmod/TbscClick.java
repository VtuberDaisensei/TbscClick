package tbsc.clickmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(modid = TbscClick.MODID, useMetadata = true)
public class TbscClick {

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

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);

            keyToggleRight = new KeyBinding("key.tbscclick.toggleright", Keyboard.KEY_G, "key.categories.tbscclick");
            keyToggleLeft = new KeyBinding("key.tbscclick.toggleleft", Keyboard.KEY_H, "key.categories.tbscclick");
            keyToggleSmartAttack = new KeyBinding("key.tbscclick.togglesmartattack", Keyboard.KEY_V, "key.categories.tbscclick");
            keyToggleHoldRight = new KeyBinding("key.tbscclick.toggleholdright", Keyboard.KEY_B, "key.categories.tbscclick");
//        keyToggleHoldLeft = new KeyBinding("key.tbscclick.toggleholdleft", Keyboard.KEY_N, "key.categories.tbscclick");

            ClientRegistry.registerKeyBinding(keyToggleRight);
            ClientRegistry.registerKeyBinding(keyToggleLeft);
            ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
            ClientRegistry.registerKeyBinding(keyToggleHoldRight);
//        ClientRegistry.registerKeyBinding(keyToggleHoldLeft);

            minecraft = Minecraft.getMinecraft();
        }
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
            if (!smart || minecraft.player.getCooledAttackStrength(0) == 1.0F) {
                if (rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                    if (minecraft.world != null && !minecraft.world.isAirBlock(rayTrace.getBlockPos()))
                        minecraft.playerController.clickBlock(rayTrace.getBlockPos(), rayTrace.sideHit);
                } else if (rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && minecraft.pointedEntity != null) {
                    minecraft.playerController.attackEntity(minecraft.player, minecraft.pointedEntity);
                }
                minecraft.player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    private void setHoldButton(KeyBinding key, boolean held) {
        KeyBinding.setKeyBindState(key.getKeyCode(), held);
    }

    /**
     * Calls Minecraft.rightClickMouse using reflection.
     */
    private void mcReflRightClick() {
        mcReflInvokeMethod("func_147121_ag"/*"rightClickMouse"*/);
    }

    private Field leftClickCounterField = null;

    private void mcReflSetLeftClickCounter(int leftClickCounter) {
        try {
            if (leftClickCounterField == null) {
                leftClickCounterField = ObfuscationReflectionHelper.findField(minecraft.getClass(), "field_71429_W"); // leftClickCounter
            }
            leftClickCounterField.setAccessible(true);
            leftClickCounterField.set(minecraft, leftClickCounter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void mcReflLeftClick(int leftClickCounter) {
        mcReflSetLeftClickCounter(leftClickCounter);
        mcReflInvokeMethod("func_147116_af"/*"clickMouse"*/);
    }

    private Field rightClickDelayTimerField = null;

    private int mcReflRightClickDelayTimer() {
        try {
            if (rightClickDelayTimerField == null) {
                rightClickDelayTimerField = ObfuscationReflectionHelper.findField(minecraft.getClass(), "field_71467_ac"); // rightClickDelayTimer
            }
            rightClickDelayTimerField.setAccessible(true);
            return (int) rightClickDelayTimerField.get(minecraft);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void mcReflInvokeMethod(String name) {
        mcReflInvokeMethod(name, new Class[0], new Object[0]);
    }

    private final Map<String, Method> methods = new HashMap<>();

    private void mcReflInvokeMethod(String name, Class<?>[] types, Object[] args) {
        try {
            if (methods.get(name) == null) {
                methods.put(name, ObfuscationReflectionHelper.findMethod(minecraft.getClass(), name, Void.TYPE, types));
            }
            Method method = methods.get(name);
            method.setAccessible(true);
            method.invoke(minecraft, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
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
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + message));
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
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
            minecraft.fontRenderer.drawString(TextFormatting.BOLD + renderString, 6, 6 + 10 * i, 0xFF0000);
        }
    }

}
