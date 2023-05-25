package io.github.kurrycat.mpkmod.ticks;

import io.github.kurrycat.mpkmod.util.Copyable;

import java.util.ArrayList;

public class ButtonMSList extends ArrayList<ButtonMS> implements Copyable<ButtonMSList> {
    public ButtonMS forKey(ButtonMS.Button button, boolean state) {
        return forKey(button, true, state);
    }

    public ButtonMS forKey(ButtonMS.Button button, boolean checkState, boolean state) {
        return stream()
                .filter(b -> b.getButton() == button && (!checkState || b.isState(state)))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public ButtonMS forKey(ButtonMS.Button button) {
        return forKey(button, false, false);
    }

    @Override
    public ButtonMSList copy() {
        ButtonMSList copy = new ButtonMSList();
        copy.addAll(this);
        return copy;
    }
}
