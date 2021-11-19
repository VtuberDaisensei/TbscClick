package tbsc.clickmod.impl;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * @author tbsc on 20/11/2021
 */
public class Config {
    private static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec CONFIG_SPEC;

    public static final String KEY_TICKS_STEP = "ticksStep";
    public static final String COMMENT_TICKS_STEP = "When changing auto click interval, by how much should it change";
    public static final int DEF_TICKS_STEP = 1;
    public static ForgeConfigSpec.IntValue ticksStep;

    public static final String KEY_MAX_TICKS = "maxTicksBetweenClicks";
    public static final String COMMENT_MAX_TICKS = "The maximal auto click interval possible before looping back to the minimum";
    public static final int DEF_MAX_TICKS = 10;
    public static ForgeConfigSpec.IntValue maxTicks;

    public static final String KEY_MIN_TICKS = "minTicksBetweenClicks";
    public static final String COMMENT_MIN_TICKS = "The minimal auto click interval";
    public static final int DEF_MIN_TICKS = 1;
    public static ForgeConfigSpec.IntValue minTicks;

    static {
        ticksStep = CONFIG_BUILDER.comment(COMMENT_TICKS_STEP)
                .defineInRange(KEY_TICKS_STEP, DEF_TICKS_STEP, 1, 9999999);

        maxTicks = CONFIG_BUILDER.comment(COMMENT_MAX_TICKS)
                .defineInRange(KEY_MAX_TICKS, DEF_MAX_TICKS, 1, 9999999);

        minTicks = CONFIG_BUILDER.comment(COMMENT_MIN_TICKS)
                .defineInRange(KEY_MIN_TICKS, DEF_MIN_TICKS, 1, 9999999);

        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .autoreload()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
