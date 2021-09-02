package tbsc.clickmod.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import tbsc.clickmod.Compat;
import tbsc.clickmod.IClick;
import tbsc.clickmod.IKeyBind;
import tbsc.clickmod.IRayTrace;

@Mod(modid = TbscClick.MODID, useMetadata = true)
public class TbscClick implements IClick {

    public static final String MODID = "tbscclick";

    public static KeyBinding keyToggleRight;
    public static KeyBinding keyToggleLeft;
    public static KeyBinding keyToggleSmartAttack;
    public static KeyBinding keyToggleHoldRight;
    public static KeyBinding keySpeed;
    private IKeyBind myKeyUse;
    private IKeyBind myKeyToggleRight;
    private IKeyBind myKeyToggleLeft;
    private IKeyBind myKeyToggleSmartAttack;
    private IKeyBind myKeyToggleHoldRight;
    private IKeyBind myKeySpeed;

    private Minecraft minecraft = null;

    private Compat compat;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            compat = new Compat(this);

            MinecraftForge.EVENT_BUS.register(this);

            keyToggleRight = new KeyBinding("key.tbscclick.toggleright", Keyboard.KEY_G, "key.categories.tbscclick");
            keyToggleLeft = new KeyBinding("key.tbscclick.toggleleft", Keyboard.KEY_H, "key.categories.tbscclick");
            keyToggleSmartAttack = new KeyBinding("key.tbscclick.togglesmartattack", Keyboard.KEY_V, "key.categories.tbscclick");
            keyToggleHoldRight = new KeyBinding("key.tbscclick.toggleholdright", Keyboard.KEY_B, "key.categories.tbscclick");
            keySpeed = new KeyBinding("key.tbscclick.speed", Keyboard.KEY_N, "key.categories.tbscclick");

            ClientRegistry.registerKeyBinding(keyToggleRight);
            ClientRegistry.registerKeyBinding(keyToggleLeft);
            ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
            ClientRegistry.registerKeyBinding(keyToggleHoldRight);
            ClientRegistry.registerKeyBinding(keySpeed);

            minecraft = Minecraft.getMinecraft();

            myKeyUse = new KeyBind(minecraft.gameSettings.keyBindUseItem);
            myKeyToggleRight = new KeyBind(keyToggleRight);
            myKeyToggleLeft = new KeyBind(keyToggleLeft);
            myKeyToggleSmartAttack = new KeyBind(keyToggleSmartAttack);
            myKeyToggleHoldRight = new KeyBind(keyToggleHoldRight);
            myKeySpeed = new KeyBind(keySpeed);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        compat.onTick();
    }

    @SubscribeEvent
    public void onInitGuiPre(GuiScreenEvent.InitGuiEvent.Pre event) {
        compat.onInitGuiPre();
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        compat.onKeyPressed();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            compat.onRenderGameOverlay();
        }
    }

    @Override
    public Minecraft getMinecraft() {
        return minecraft;
    }

    @Override
    public boolean isGamePaused() {
        return minecraft.isGamePaused();
    }

    @Override
    public boolean isInGame() {
        return minecraft.currentScreen == null;
    }

    @Override
    public IRayTrace getRayTrace() {
        return new RayTrace(minecraft.objectMouseOver);
    }

    @Override
    public float getSmartAttackCooldown() {
        return minecraft.player.getCooledAttackStrength(0);
    }

    @Override
    public void postClickInputEvent() {
        // No-op in this version
    }

    @Override
    public void swingHandIfShould() {
        if (minecraft.player != null) {
            minecraft.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    @Override
    public void setHoldButton(IKeyBind key, boolean held) {
        key.setHeld(held);
    }

    @Override
    public IKeyBind getUseKey() {
        return myKeyUse;
    }

    @Override
    public IKeyBind getToggleRightKey() {
        return myKeyToggleRight;
    }

    @Override
    public IKeyBind getToggleLeftKey() {
        return myKeyToggleLeft;
    }

    @Override
    public IKeyBind getToggleSmartAttackKey() {
        return myKeyToggleSmartAttack;
    }

    @Override
    public IKeyBind getToggleHoldRightKey() {
        return myKeyToggleHoldRight;
    }

    @Override
    public IKeyBind getSpeedKey() {
        return myKeySpeed;
    }

    @Override
    public String getRightClickMouseMethodMapping() {
        return "func_147121_ag";
    }

    @Override
    public String getLeftClickCounterFieldMapping() {
        return "field_71429_W";
    }

    @Override
    public String getLeftClickMouseMethodMapping() {
        return "func_147116_af";
    }

    @Override
    public String getRightClickDelayTimerFieldMapping() {
        return "field_71467_ac";
    }

    @Override
    public void renderTextOnScreen(String text, float x, float y, int color) {
        minecraft.fontRenderer.drawString(TextFormatting.BOLD + text, x, y, color, false);
    }

    @Override
    public boolean isPlayerHandBusy() {
        return minecraft.player != null && !minecraft.player.isHandActive();
    }

    @Override
    public void sendMessage(String message) {
        minecraft.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(TextFormatting.RED + message));
    }

    @Override
    public void sendMessageWithId(String message, int id) {
        compat.reflInvokeMethod(GuiNewChat.class, minecraft.ingameGUI.getChatGUI(), "func_146234_a",
                new Class[] { ITextComponent.class, int.class },
                new Object[] { new TextComponentString(message), id });
    }

}
