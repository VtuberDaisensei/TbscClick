package tbsc.clickmod.gui;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import tbsc.clickmod.TbscClick;

import static tbsc.clickmod.TbscClick.config;

/**
 * Created by tbsc on 3/12/2016.
 */
public class GuiModConfig extends GuiConfig {

    public GuiModConfig(GuiScreen parent) {
        super(parent, new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                TbscClick.MODID, TbscClick.MODID, false, false, "TbscClick Settings");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, "Not in use!", width / 2, height / 2, 0xFFFFFF);
    }

}
