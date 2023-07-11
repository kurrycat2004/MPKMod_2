package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.gui.ComponentScreen;
import io.github.kurrycat.mpkmod.gui.components.Anchor;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.ScrollableList;
import io.github.kurrycat.mpkmod.save.Serializer;
import io.github.kurrycat.mpkmod.util.JSONConfig;
import io.github.kurrycat.mpkmod.util.Vector2D;

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
                new Vector2D(0, 16),
                new Vector2D(3 / 5D, -40),
                new ArrayList<>(API.optionsMap.values())
        );
        addChild(optionList, PERCENT.SIZE_X, Anchor.TOP_CENTER);

        optionList.topCover.addChild(
                new Button(
                        "x",
                        new Vector2D(5, 1),
                        new Vector2D(11, 11),
                        mouseButton -> close()
                ),
                PERCENT.NONE, Anchor.CENTER_RIGHT
        );

        optionList.bottomCover.setHeight(24, false);
        optionList.bottomCover.backgroundColor = null;

        optionList.bottomCover.addChild(new Button(
                        "Apply",
                        new Vector2D(-2, 2),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.updateAll()
                ),
                PERCENT.NONE, Anchor.BOTTOM_RIGHT, Anchor.BOTTOM_CENTER
        );

        optionList.bottomCover.addChild(new Button(
                        "Reset all",
                        new Vector2D(2, 2),
                        new Vector2D(100, 20),
                        mouseButton -> optionList.resetAllToDefault()
                ),
                PERCENT.NONE, Anchor.BOTTOM_LEFT, Anchor.BOTTOM_CENTER
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


    public void render(Vector2D mouse, float partialTicks) {
        super.render(mouse, partialTicks);
        optionList.renderHover(mouse);
    }

    public static class OptionList extends ScrollableList<OptionListItem> {
        public OptionList(Vector2D pos, Vector2D size, ArrayList<Option> options) {
            this.setPos(pos);
            this.setSize(size);
            this.setTitle("Options");
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
            renderComponents(mouse);
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
