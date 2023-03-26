package io.github.kurrycat.mpkmod.gui.screens.options_gui;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
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
    public static Color optionListColorItemEdge = new Color(255, 255, 255, 95);
    public static Color optionListColorBg = new Color(31, 31, 31, 150);

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

    private ArrayList<Option> initOptions() {
        return new ArrayList<>(API.optionsMap.values());
    }

    public static class OptionList extends ScrollableList<OptionListItem> {
        private final ArrayList<Option> options;

        public OptionList(Vector2D pos, Vector2D size, ArrayList<Option> options) {
            super(pos, size);
            this.options = options;
            items.clear();
            for (Option option : options) {
                items.add(new OptionListItem(this, option));
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
            for (Option o : options) {
                o.setValue(o.getDefaultValue());
            }
        }

        public void updateAll() {
            for (OptionListItem item : items) {
                item.update();
            }
        }
    }

    public static class OptionListItem extends ScrollableListItem<OptionListItem> {
        private final Button resetButton;
        private final CheckButton checkButton;
        private final ArrayList<Component> updateComponents = new ArrayList<>();
        public Option option;
        private String value;

        public OptionListItem(ScrollableList<OptionListItem> parent, Option option) {
            super(parent);
            this.option = option;
            this.value = option.getValue();

            resetButton = new Button("Reset", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                if (mouseButton == Mouse.Button.LEFT) {
                    value = option.getDefaultValue();
                    updateEditComponent();
                }
            });
            updateComponents.add(resetButton);

            checkButton = new CheckButton(Vector2D.OFFSCREEN, checked -> value = String.valueOf(checked));
            checkButton.enabled = option.getType() == Option.ValueType.BOOLEAN;
            if (checkButton.enabled) {
                checkButton.setChecked(option.getBoolean());
                updateComponents.add(checkButton);
            }
        }

        private void updateEditComponent() {
            if(checkButton.enabled) {
                checkButton.setChecked(Boolean.parseBoolean(value));
            }
        }

        public void update() {
            option.setValue(value);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, optionListColorBg, optionListColorItemEdge);

            FontRenderer.drawLeftCenteredString(
                    option.getName(),
                    pos.add(5, size.getY() / 2),
                    Color.WHITE,
                    false
            );

            switch (option.getType()) {
                case BOOLEAN:
                    checkButton.pos = pos.add(size.getX() - 40 - checkButton.getDisplayedSize().getX(), size.getY() / 2 - 5);
                    checkButton.render(mouse);
                    break;
                default:
                    FontRenderer.drawRightCenteredString(
                            value,
                            pos.add(size.getX() - 40, size.getY() / 2),
                            Color.WHITE,
                            false
                    );
            }

            resetButton.enabled = !option.getDefaultValue().equals(value);
            resetButton.pos = pos.add(size.getX() - 35, size.getY() / 2 - 5);
            resetButton.render(mouse);
        }

        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(MouseInputListener.class, updateComponents),
                    ele -> ele.handleMouseInput(state, mousePos, button)
            );
        }

        public boolean handleKeyInput(char keyCode, String key, boolean pressed) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(KeyInputListener.class, updateComponents),
                    ele -> ele.handleKeyInput(keyCode, key, pressed)
            );
        }

        public int getHeight() {
            return 21;
        }
    }
}
