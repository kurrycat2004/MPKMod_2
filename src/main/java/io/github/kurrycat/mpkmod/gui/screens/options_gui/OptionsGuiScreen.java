package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class OptionsGuiScreen extends ComponentScreen {
    private OptionList optionList;

    @Override
    public boolean shouldCreateKeyBind() {
        return true;
    }

    @Override
    public void onGuiInit() {
        super.onGuiInit();
        optionList = new OptionList(
                new Vector2D(0.5, 16),
                new Vector2D(3 / 5D, -40),
                new ArrayList<>(API.optionsMap.values())
        );
        addChild(optionList, true, false, true, false, Component.Anchor.TOP_LEFT);

        optionList.addChild(
                new Button(
                        "x",
                        new Vector2D(3, -13),
                        new Vector2D(11, 11),
                        mouseButton -> close()
                ),
                false, false, false, false, Component.Anchor.TOP_RIGHT
        );

        optionList.addChild(new Button(
                        "Apply",
                        new Vector2D(0.2, -22),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.updateAll()
                ),
                true, false, false, false, Component.Anchor.BOTTOM_LEFT
        );

        optionList.addChild(new Button(
                        "Reset all",
                        new Vector2D(0.2, -22),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.resetAllToDefault()
                ),
                true, false, false, false, Component.Anchor.BOTTOM_RIGHT
        );
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        HashMap<String, String> options = new HashMap<>();
        for (String key : API.optionsMap.keySet()) {
            options.put(key, API.optionsMap.get(key).getValue());
        }
        Serializer.serialize(JSONConfig.optionsFile, options);
    }


    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
    }

    public static class OptionList extends ScrollableList<OptionListItem> {
        private final ArrayList<Option> options;

        public OptionList(Vector2D pos, Vector2D size, ArrayList<Option> options) {
            super(pos, size);
            this.options = options;
            items.clear();
            for (Option option : options) {
                OptionListItem item;
                switch (option.getType()) {
                    case BOOLEAN:
                        item = new OptionListItemBoolean(this, option);
                        break;
                    case STRING:
                        item = new OptionListItemString(this, option);
                        break;
                    case INTEGER:
                        item = new OptionListItemInteger(this, option);
                        break;
                    default:
                        item = new OptionListItemDefault(this, option);
                }
                items.add(item);
            }
            this.scrollBar.constrainScrollAmountToScreen();
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
            components.forEach(c -> c.render(mouse));
        }

        @Override
        public void drawTopCover(Vector2D mouse, Vector2D pos, Vector2D size) {
            super.drawTopCover(mouse, pos, size);
            FontRenderer.drawCenteredString(Colors.UNDERLINE + "Options", pos.add(size.div(2)).add(0, 1), Color.WHITE, false);
        }

        public void resetAllToDefault() {
            for (OptionListItem item : items) {
                item.loadDefaultValue();
            }
        }

        public void updateAll() {
            for (OptionListItem item : items) {
                item.update();
            }
        }
    }
}
