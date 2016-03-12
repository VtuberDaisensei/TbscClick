package tbsc.clickmod;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

@Mod(modid = TbscClick.MODID, version = TbscClick.VERSION)
public class TbscClick extends Gui {

    public static final String MODID = "TbscClick";
    public static final String VERSION = "1.0.0";
    public static boolean shouldClick = false;
    public static KeyBinding keyToggle;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(this);

        keyToggle = new KeyBinding("key.tbscclick.toggle", Keyboard.KEY_G, "key.categories.misc");
        ClientRegistry.registerKeyBinding(keyToggle);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (keyToggle.isPressed()) {
            shouldClick = !shouldClick;
        }

        if (shouldClick) {
            Minecraft minecraft = Minecraft.getMinecraft();
            MovingObjectPosition rayTrace = minecraft.thePlayer.rayTrace(5, 0.5F);
            minecraft.playerController.onPlayerRightClick(minecraft.thePlayer, minecraft.theWorld,
                    minecraft.thePlayer.getHeldItem(), rayTrace.blockX, rayTrace.blockY, rayTrace.blockZ,
                    rayTrace.sideHit, rayTrace.hitVec);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (shouldClick && event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            Minecraft minecraft = Minecraft.getMinecraft();
            drawString(minecraft.fontRenderer, EnumChatFormatting.BOLD + "Auto-Clicker Enabled!", 6, 6, 0xFF0000);
        }
    }

}
