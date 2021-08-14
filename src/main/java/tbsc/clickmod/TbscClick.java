package tbsc.clickmod;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(TbscClick.MODID)
public class TbscClick {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "tbscclick";
    public static boolean shouldLeftClick = false;
    public static boolean shouldSmartAttack = false;
    public static boolean shouldRightClick = false;
//    public static boolean holdingLeftButton = false;
    public static boolean holdingRightButton = false;
    public static int clickTickInterval = 1;
    public static KeyMapping keyToggleRight;
    public static KeyMapping keyToggleLeft;
    public static KeyMapping keyToggleSmartAttack;
    public static KeyMapping keyToggleHoldRight;
//    public static KeyMapping keyToggleHoldLeft;
    public static KeyMapping keySpeed;

    private Minecraft minecraft = null;

    public TbscClick() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetupEvent);
    }

    @SubscribeEvent
    public void onClientSetupEvent(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        keyToggleRight = new KeyMapping("key.tbscclick.toggleright", GLFW.GLFW_KEY_G, "key.categories.tbscclick");
        keyToggleLeft = new KeyMapping("key.tbscclick.toggleleft", GLFW.GLFW_KEY_H, "key.categories.tbscclick");
        keyToggleSmartAttack = new KeyMapping("key.tbscclick.togglesmartattack", GLFW.GLFW_KEY_V, "key.categories.tbscclick");
        keyToggleHoldRight = new KeyMapping("key.tbscclick.toggleholdright", GLFW.GLFW_KEY_B, "key.categories.tbscclick");
//        keyToggleHoldLeft = new KeyMapping("key.tbscclick.toggleholdleft", GLFW.GLFW_KEY_N, "key.categories.tbscclick");
        keySpeed = new KeyMapping("key.tbscclick.speed", GLFW.GLFW_KEY_N, "key.categories.tbscclick");

        ClientRegistry.registerKeyBinding(keyToggleRight);
        ClientRegistry.registerKeyBinding(keyToggleLeft);
        ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
        ClientRegistry.registerKeyBinding(keyToggleHoldRight);
//        ClientRegistry.registerKeyBinding(keyToggleHoldLeft);
        ClientRegistry.registerKeyBinding(keySpeed);

        minecraft = Minecraft.getInstance();
    }

    boolean holdLeftWasPressed = false;
    boolean holdRightWasPressed = false;

    private int leftCooldown = 0;
    private int rightCooldown = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!minecraft.isPaused()) {
            /* AUTO LEFT CLICK */

            if (shouldLeftClick) {
                if (leftCooldown == 0) {
                    leftClick(false);
                    leftCooldown = clickTickInterval - 1;
                } else {
                    leftCooldown--;
                }
            }

            /* SMART ATTACK */

            if (shouldSmartAttack) {
                leftClick(true);
            }

            /* AUTO RIGHT CLICK */

            if (shouldRightClick) {
                if (rightCooldown == 0) {
                    mcReflRightClick();
                    rightCooldown = clickTickInterval - 1;
                } else {
                    rightCooldown--;
                }
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
            if (holdingRightButton && !isInGame() && mcReflRightClickDelayTimer() == 0 && minecraft.player != null && !minecraft.player.isHandsBusy()) {
                mcReflRightClick();
            }

            if (holdRightWasPressed && !keyToggleHoldRight.isDown()) {
                holdingRightButton = !holdingRightButton;
                setHoldButton(minecraft.options.keyUse, holdingRightButton);
                holdRightWasPressed = false;
            }
        }
    }

    public boolean isInGame() {
        return minecraft.screen == null;
    }

    @SubscribeEvent
    public void onInitGuiPre(GuiScreenEvent.InitGuiEvent.Pre event) {
        // Triggered when opening any screen that isn't the game
        // Opening a screen clears all pressed keybinds, so make it pressed again
        // This will persist when exiting the screen
        if (holdingRightButton) {
            setHoldButton(minecraft.options.keyUse, true);
        }
    }

    /**
     * @param smart If true, waits until the attack cooldown is over before clicking again, despite being called.
     */
    private void leftClick(boolean smart) {
        if (minecraft.gameMode != null && minecraft.player != null) {
            HitResult rayTrace = minecraft.hitResult;
            InputEvent.ClickInputEvent inputEvent = ForgeHooksClient.onClickInput(0, minecraft.options.keyAttack, InteractionHand.MAIN_HAND);
            if (!smart || minecraft.player.getAttackStrengthScale(0) == 1.0F) {
                if (rayTrace instanceof BlockHitResult blockRayTrace && rayTrace.getType() != HitResult.Type.MISS) {
                    if (minecraft.level != null && !minecraft.level.isEmptyBlock(blockRayTrace.getBlockPos()))
                        minecraft.gameMode.startDestroyBlock(blockRayTrace.getBlockPos(), blockRayTrace.getDirection());
                } else if (rayTrace instanceof EntityHitResult && minecraft.crosshairPickEntity != null) {
                    minecraft.gameMode.attack(minecraft.player, minecraft.crosshairPickEntity);
                }
                if (inputEvent.shouldSwingHand())
                    minecraft.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    private void setHoldButton(KeyMapping key, boolean held) {
        KeyMapping.set(key.getKey(), held);
    }

    /**
     * Calls Minecraft.startUseItem using reflection.
     */
    private void mcReflRightClick() {
        mcReflInvokeMethod("m_91277_"/*"startUseItem"*/);
    }

    private Field leftClickCounterField = null;

    private void mcReflSetLeftClickCounter(int leftClickCounter) {
        try {
            if (leftClickCounterField == null) {
                leftClickCounterField = ObfuscationReflectionHelper.findField(Minecraft.class, "f_91078_"); // missTime
            }
            leftClickCounterField.setAccessible(true);
            leftClickCounterField.set(minecraft, leftClickCounter);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void mcReflLeftClick(int leftClickCounter) {
        mcReflSetLeftClickCounter(leftClickCounter);
        mcReflInvokeMethod("m_91276_"/*"startAttack"*/);
    }

    private Field rightClickDelayTimerField = null;

    private int mcReflRightClickDelayTimer() {
        try {
            if (rightClickDelayTimerField == null) {
                rightClickDelayTimerField = ObfuscationReflectionHelper.findField(Minecraft.class, "f_91011_"); // rightClickDelay
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

    private void mcReflInvokeMethod(String name, Class<?>[] types, Object[] args) {
        reflInvokeMethod(Minecraft.class, minecraft, name, types, args);
    }

    private <T> void reflInvokeMethod(Class<T> clazz, T instance, String name) {
        reflInvokeMethod(clazz, instance, name, new Class[0], new Object[0]);
    }

    private final Map<String, Method> methods = new HashMap<>();

    private <T> void reflInvokeMethod(Class<T> clazz, T instance, String name, Class<?>[] types, Object[] args) {
        try {
            if (methods.get(clazz.getName() + name) == null) {
                methods.put(clazz.getName() + name, ObfuscationReflectionHelper.findMethod(clazz, name, types));
            }
            Method method = methods.get(clazz.getName() + name);
            method.setAccessible(true);
            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (keyToggleLeft.isDown()) {
//            disableHoldLeftByConflict();
            disableSmartAttackByConflict();
            shouldLeftClick = !shouldLeftClick;
        }
        if (keyToggleSmartAttack.isDown()) {
//            disableHoldLeftByConflict();
            disableAutoLeftByConflict();
            shouldSmartAttack = !shouldSmartAttack;
        }
        if (keyToggleRight.isDown()) {
            disableHoldRightByConflict();
            shouldRightClick = !shouldRightClick;
        }

//        if (keyToggleHoldLeft.isPressed()) {
//            disableAutoLeftByConflict();
//            disableSmartAttackByConflict();
//            holdLeftWasPressed = true;
//        }
        if (keyToggleHoldRight.isDown()) {
            disableAutoRightByConflict();
            holdRightWasPressed = true;
        }

        if (keySpeed.isDown()) {
            int chatId = 8327; // magic number
            String plural = "s";
            if (++clickTickInterval == 11) {
                plural = "";
                clickTickInterval = 1;
            }
            sendMessage("New auto click interval: every " + clickTickInterval + " tick" + plural, chatId);
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
        Minecraft.getInstance().gui.getChat().addMessage(new TextComponent(message).withStyle(ChatFormatting.RED));
    }

    private void sendMessage(String message, int id) {
        reflInvokeMethod(ChatComponent.class, Minecraft.getInstance().gui.getChat(), "m_93787_",
                new Class[] { Component.class, int.class },
                new Object[] { new TextComponent(message), id });
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
            minecraft.font.draw(new PoseStack(), ChatFormatting.BOLD + renderString, 6, 6 + 10 * i, 0xFF0000);
        }
    }

}
