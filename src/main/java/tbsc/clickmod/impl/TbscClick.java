package tbsc.clickmod.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
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
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;
import tbsc.clickmod.Compat;
import tbsc.clickmod.IClick;
import tbsc.clickmod.IKeyBind;
import tbsc.clickmod.IRayTrace;

@Mod(TbscClick.MODID)
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

    private int ticksStepBetweenClicks = Config.DEF_TICKS_STEP;
    private int maxTicksBetweenClicks = Config.DEF_MAX_TICKS;
    private int minTicksBetweenClicks = Config.DEF_MIN_TICKS;

    private Minecraft minecraft = null;

    private Compat compat;

    public TbscClick() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetupEvent);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReloaded);
    }

    @SubscribeEvent
    public void onClientSetupEvent(FMLClientSetupEvent event) {
        compat = new Compat(this);

        MinecraftForge.EVENT_BUS.register(this);

        keyToggleRight = new KeyBinding("key.tbscclick.toggleright", GLFW.GLFW_KEY_G, "key.categories.tbscclick");
        keyToggleLeft = new KeyBinding("key.tbscclick.toggleleft", GLFW.GLFW_KEY_H, "key.categories.tbscclick");
        keyToggleSmartAttack = new KeyBinding("key.tbscclick.togglesmartattack", GLFW.GLFW_KEY_V, "key.categories.tbscclick");
        keyToggleHoldRight = new KeyBinding("key.tbscclick.toggleholdright", GLFW.GLFW_KEY_B, "key.categories.tbscclick");
        keySpeed = new KeyBinding("key.tbscclick.speed", GLFW.GLFW_KEY_N, "key.categories.tbscclick");

        ClientRegistry.registerKeyBinding(keyToggleRight);
        ClientRegistry.registerKeyBinding(keyToggleLeft);
        ClientRegistry.registerKeyBinding(keyToggleSmartAttack);
        ClientRegistry.registerKeyBinding(keyToggleHoldRight);
        ClientRegistry.registerKeyBinding(keySpeed);

        minecraft = Minecraft.getInstance();

        myKeyUse = new KeyBind(minecraft.options.keyUse);
        myKeyToggleRight = new KeyBind(keyToggleRight);
        myKeyToggleLeft = new KeyBind(keyToggleLeft);
        myKeyToggleSmartAttack = new KeyBind(keyToggleSmartAttack);
        myKeyToggleHoldRight = new KeyBind(keyToggleHoldRight);
        myKeySpeed = new KeyBind(keySpeed);

        Config.loadConfig(Config.CONFIG_SPEC, FMLPaths.CONFIGDIR.get().resolve("TbscClick.toml"));
        processConfig();
    }

    private void processConfig() {
        ticksStepBetweenClicks = Config.ticksStep.get();
        maxTicksBetweenClicks = Config.maxTicks.get();
        minTicksBetweenClicks = Config.minTicks.get();
    }

    public void onConfigReloaded(ModConfig.ModConfigEvent event) {
        if (event instanceof ModConfig.Reloading) {
            processConfig();
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
        return minecraft.isPaused();
    }

    @Override
    public boolean isInGame() {
        return minecraft.screen == null;
    }

    @Override
    public boolean isInPauseMenu() {
        return minecraft.screen instanceof IngameMenuScreen;
    }

    @Override
    public IRayTrace getRayTrace() {
        return new RayTrace(minecraft.hitResult);
    }

    @Override
    public float getSmartAttackCooldown() {
        return minecraft.player.getAttackStrengthScale(0);
    }

    InputEvent.ClickInputEvent clickInputEvent;

    @Override
    public void postClickInputEvent() {
        clickInputEvent = ForgeHooksClient.onClickInput(0, minecraft.options.keyAttack, Hand.MAIN_HAND);
    }

    @Override
    public void swingHandIfShould() {
        if (clickInputEvent.shouldSwingHand() && minecraft.player != null) {
            minecraft.player.swing(Hand.MAIN_HAND);
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
    public int getTicksStepBetweenClicks() {
        return ticksStepBetweenClicks;
    }

    @Override
    public int getMaxTicksBetweenClicks() {
        return maxTicksBetweenClicks;
    }

    @Override
    public int getMinTicksBetweenClicks() {
        return minTicksBetweenClicks;
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
        minecraft.font.draw(new MatrixStack(), TextFormatting.BOLD + text, x, y, color);
    }

    @Override
    public boolean isPlayerHandBusy() {
        return minecraft.player != null && minecraft.player.isHandsBusy();
    }

    @Override
    public void sendMessage(String message) {
        minecraft.gui.getChat().addMessage(new StringTextComponent(message).withStyle(TextFormatting.RED));
    }

    @Override
    public void sendMessageWithId(String message, int id) {
        compat.reflInvokeMethod(NewChatGui.class, minecraft.gui.getChat(), "func_146234_a",
                new Class[] { ITextComponent.class, int.class },
                new Object[] { new StringTextComponent(message), id });
    }

}
