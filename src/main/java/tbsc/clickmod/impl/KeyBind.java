package tbsc.clickmod.impl;

import net.minecraft.client.settings.KeyBinding;
import tbsc.clickmod.IKeyBind;

/**
 * @author tbsc on 02/09/2021
 */
public class KeyBind implements IKeyBind {

    private final KeyBinding key;

    public KeyBind(KeyBinding key) {
        this.key = key;
    }

    @Override
    public void setHeld(boolean held) {
        KeyBinding.setKeyBindState(key.getKey(), held);
    }

    @Override
    public boolean isDown() {
        return key.isKeyDown();
    }
}
