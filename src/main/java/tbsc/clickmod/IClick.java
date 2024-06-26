package tbsc.clickmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * @author tbsc on 02/09/2021
 */
public interface IClick {

    Minecraft getMinecraft();
    boolean isGamePaused();
    boolean isInGame();
    boolean isInPauseMenu();

    boolean isPlayerHandBusy();

    void sendMessage(String message);
    void sendMessageWithId(String message, int id);

    IRayTrace getRayTrace();
    float getSmartAttackCooldown();
    void postClickInputEvent();
    void swingHandIfShould();

    void setHoldButton(IKeyBind key, boolean held);

    IKeyBind getUseKey();
    IKeyBind getToggleRightKey();
    IKeyBind getToggleLeftKey();
    IKeyBind getToggleSmartAttackKey();
    IKeyBind getToggleHoldRightKey();
    IKeyBind getSpeedKey();

    int getTicksStepBetweenClicks();
    int getMaxTicksBetweenClicks();
    int getMinTicksBetweenClicks();

    String getRightClickMouseMethodMapping();
    String getLeftClickCounterFieldMapping();
    String getLeftClickMouseMethodMapping();
    String getRightClickDelayTimerFieldMapping();
    // [update] Grand-Teacher June-26-2024
    void renderTextOnScreen(GuiGraphics guiGraphics, String text, int x, int y, int color);

}
