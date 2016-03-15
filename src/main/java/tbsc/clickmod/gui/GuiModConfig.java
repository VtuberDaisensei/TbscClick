package tbsc.clickmod.gui;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import tbsc.clickmod.TbscClick;

/**
 * Created by tbsc on 3/12/2016.
 */
public class GuiModConfig extends GuiConfig {

    public GuiModConfig(GuiScreen parent) {
        super(parent, new ConfigElement(TbscClick.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), TbscClick.MODID, TbscClick.MODID, false, false, "TbscClick Settings");
    }

}
