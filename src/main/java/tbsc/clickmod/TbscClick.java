package tbsc.clickmod;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.lwjgl.input.Keyboard;

@Mod(modid = TbscClick.MODID, version = TbscClick.VERSION, guiFactory = TbscClick.GUI_FACTORY)
public class TbscClick extends Gui {

    public static final String MODID = "TbscClick";
    public static final String VERSION = "1.0.0";
    public static final String GUI_FACTORY = "tbsc.clickmod.gui.TCGuiFactory";
    public static boolean shouldClick = false;
    public static KeyBinding keyToggle;
    public static Configuration config;
    public static float clickDelay = 0.5F;
    public static boolean shouldLeftClick = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.SERVER) {
            FMLLog.bigWarning("CLIENT-SIDE MOD INSTALLED ON SERVER. REMOVE FROM SERVER.");
            FMLLog.bigWarning("You are lucky I am adding a safety-check, or you'll crash.");
            FMLCommonHandler.instance().exitJava(1, false);
        }
        FMLCommonHandler.instance().bus().register(this);
        config = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();

        keyToggle = new KeyBinding("key.tbscclick.toggle", Keyboard.KEY_G, "key.categories.misc");
        ClientRegistry.registerKeyBinding(keyToggle);
    }

    public static void syncConfig() {
        try {
            // Load config
            config.load();

            // Read props from config
            Property clickDelayProp = config.get(Configuration.CATEGORY_GENERAL, "clickDelay", 0.5, "Delay between auto-clicks.");
            Property leftClickProp = config.get(Configuration.CATEGORY_GENERAL, "autoClickButton", "rightClick", "Changes what button will be auto-clicked.");
            clickDelay = (float) clickDelayProp.getDouble();
            shouldLeftClick = getBooleanFromClickSide(leftClickProp.getString());
        } catch (Exception e) {
            // Exception
        } finally {
            // Save props to config
            if (config.hasChanged()) config.save();
        }
    }

    private static boolean getBooleanFromClickSide(String side) {
        return !(side.equalsIgnoreCase("rightClick") || side.equalsIgnoreCase("rightButton") || side.equalsIgnoreCase("right")) && (side.equalsIgnoreCase("leftClick") || side.equalsIgnoreCase("leftButton") || side.equalsIgnoreCase("left"));
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event) {
        syncConfig();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (keyToggle.isPressed()) {
            shouldClick = !shouldClick;
        }

        if (shouldClick) {
            Minecraft minecraft = Minecraft.getMinecraft();
            MovingObjectPosition rayTrace = minecraft.thePlayer.rayTrace(5, clickDelay);
            if (shouldLeftClick) {
                minecraft.playerController.clickBlock(rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ, rayTrace.sideHit);
            } else {
                minecraft.playerController.onPlayerRightClick(minecraft.thePlayer, minecraft.theWorld,
                        minecraft.thePlayer.getHeldItem(), rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ,
                        rayTrace.sideHit, rayTrace.hitVec);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(TickEvent.RenderTickEvent event) {
        if (shouldClick) {
            Minecraft minecraft = Minecraft.getMinecraft();
            drawString(minecraft.fontRenderer, EnumChatFormatting.BOLD + "Auto-Clicker Enabled!", 6, 6, 0xFF0000);
        }
    }

}
