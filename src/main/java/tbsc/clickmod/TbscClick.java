package tbsc.clickmod;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

@Mod(modid = TbscClick.MODID, version = TbscClick.VERSION, guiFactory = TbscClick.GUI_FACTORY, canBeDeactivated = true)
public class TbscClick {

    public static final String MODID = "TbscClick";
    public static final String VERSION = "2.0.1";
    public static final String GUI_FACTORY = "tbsc.clickmod.gui.TCGuiFactory";
    public static boolean shouldLeftClick = false;
    public static boolean shouldRightClick = false;
    public static boolean holdingLeftButton = false;
    public static boolean holdingRightButton = false;
    public static KeyBinding keyToggleRight;
    public static KeyBinding keyToggleLeft;
    public static KeyBinding keyToggleHoldRight;
    public static KeyBinding keyToggleHoldLeft;
    public static Configuration config;
    public static float clickDelay = 0.1F;

    @Mod.EventHandler
    public void construction(FMLConstructionEvent event) {
        if (event.getSide() == Side.SERVER) {
            FMLLog.bigWarning("****************************************************************");
            FMLLog.bigWarning("*** CLIENT-SIDE MOD INSTALLED ON SERVER. REMOVE FROM SERVER. ***");
            FMLLog.bigWarning("****************************************************************");
            FMLCommonHandler.instance().exitJava(1, false);
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();

        keyToggleRight = new KeyBinding("key.tbscclick.toggleright", Keyboard.KEY_G, "key.categories.tbscclick");
        keyToggleLeft = new KeyBinding("key.tbscclick.toggleleft", Keyboard.KEY_H, "key.categories.tbscclick");
        keyToggleHoldRight = new KeyBinding("key.tbscclick.toggleholdright", Keyboard.KEY_B, "key.categories.tbscclick");
        keyToggleHoldLeft = new KeyBinding("key.tbscclick.toggleholdleft", Keyboard.KEY_N, "key.categories.tbscclick");

        ClientRegistry.registerKeyBinding(keyToggleRight);
        ClientRegistry.registerKeyBinding(keyToggleLeft);
        ClientRegistry.registerKeyBinding(keyToggleHoldRight);
        ClientRegistry.registerKeyBinding(keyToggleHoldLeft);
    }

    public static void syncConfig() {
        try {
            // Load config
            config.load();

            // Read props from config (when there will be some)
        } catch (Exception e) {
            // Exception
        } finally {
            // Save props to config
            if (config.hasChanged()) config.save();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        syncConfig();
    }

    boolean holdLeftWasPressed = false;
    boolean holdRightWasPressed = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (!minecraft.isGamePaused()) {
            if (shouldLeftClick) {
                MovingObjectPosition rayTrace = minecraft.thePlayer.rayTrace(5, clickDelay);
                if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) // Safe check
                    minecraft.playerController.clickBlock(rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ, rayTrace.sideHit);
            }

            if (shouldRightClick) {
                MovingObjectPosition rayTrace = minecraft.thePlayer.rayTrace(5, clickDelay);
                if (rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.MISS)
                    minecraft.playerController.onPlayerRightClick(minecraft.thePlayer, minecraft.theWorld,
                        minecraft.thePlayer.getHeldItem(), rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ, rayTrace.sideHit, rayTrace.hitVec);
            }

            if (holdLeftWasPressed && !keyToggleHoldLeft.isPressed()) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindAttack.getKeyCode(), !holdingLeftButton);
                holdingLeftButton = !holdingLeftButton;
                holdLeftWasPressed = false;
            }
            if (holdRightWasPressed && !keyToggleHoldRight.isPressed()) {
                KeyBinding.setKeyBindState(minecraft.gameSettings.keyBindUseItem.getKeyCode(), !holdingRightButton);
                holdingRightButton = !holdingRightButton;
                holdRightWasPressed = false;
            }
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if (keyToggleLeft.isPressed()) {
            if (holdingLeftButton) {
                holdLeftWasPressed = true;
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unable to hold left button and auto click left button at the same time! Disabled holding left button."));
            }
            shouldLeftClick = !shouldLeftClick;
        }
        if (keyToggleRight.isPressed()) {
            if (holdingRightButton) {
                holdRightWasPressed = true;
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unable to hold right button and auto click right button at the same time! Disabled holding right button."));
            }
            shouldRightClick = !shouldRightClick;
        }

        if (keyToggleHoldLeft.isPressed()) {
            if (shouldLeftClick) {
                shouldLeftClick = false;
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unable to hold left button and auto click left button at the same time! Disabled auto clicking left button."));
            }
            holdLeftWasPressed = true;
        }
        if (keyToggleHoldRight.isPressed()) {
            if (shouldRightClick) {
                shouldRightClick = false;
                sendMessage(new ChatComponentText(EnumChatFormatting.RED + "Unable to hold right button and auto click right button at the same time! Disabled auto clicking right button."));
            }
            holdRightWasPressed = true;
        }
    }

    private void sendMessage(IChatComponent message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        List<String> renderList = new ArrayList<String>();

        if (shouldLeftClick) {
            renderList.add("Auto Left Clicking");
        }
        if (shouldRightClick) {
            renderList.add("Auto Right Clicking");
        }
        if (holdingLeftButton) {
            renderList.add("Holding Left Button");
        }
        if (holdingRightButton) {
            renderList.add("Holding Right Button");
        }

        for (int i = 0; i < renderList.size(); ++i) {
            String renderString = renderList.get(i);
            minecraft.fontRenderer.drawString(EnumChatFormatting.BOLD + renderString, 6, 6 + 10 * i, 0xFF0000);
        }
    }

}
