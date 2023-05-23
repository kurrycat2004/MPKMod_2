package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadConfigPane extends Pane<MainGuiScreen> {
    private InputField filename;
    private ConfigFileList presets;
    private ConfigFileList savedConfigs;

    public static Color edgeColor = new Color(255, 255, 255, 95);

    public LoadConfigPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        initComponents();
    }

    public void reload() {
        presets.reloadItems();
        presets.updateItems();
        savedConfigs.reloadItems();
        savedConfigs.updateItems();
    }

    private void initComponents() {
        filename = new InputField(
                "",
                new Vector2D(0.5D, 1/3D),
                0.9D
        );
        filename.setFilter(InputField.FILTER_FILENAME);
        filename.setOnContentChange(content -> {
            presets.updateItems();
            savedConfigs.updateItems();
        });

        presets = new ConfigFileList(LabelConfiguration.presets, new Vector2D(0.2 / 3, 0.07), new Vector2D(4 / 10D, 0.8));
        presets.title = "Presets";
        addChild(presets, true, true, Anchor.TOP_LEFT, true);

        savedConfigs = new ConfigFileList(LabelConfiguration.savedConfigs, new Vector2D(0.2 / 3, 0.07), new Vector2D(4 / 10D, 0.83));
        savedConfigs.title = "Saved Configurations";
        addChild(savedConfigs, true, true, Anchor.TOP_RIGHT, true);

        Button reloadCurrent = new Button(
                "Reload from file",
                new Vector2D(0.5, 0.5),
                new Vector2D(0.8, 20),
                b -> {
                    if(b != Mouse.Button.LEFT) return;
                    LabelConfiguration.currentConfig.reloadFromFile();
                    paneHolder.reloadConfig();
                }
        );
        Div reloadDiv = new Div(
                new Vector2D(0.2 / 3, 0),
                new Vector2D(4 / 10D, 0.1)
        );
        reloadDiv.addChild(reloadCurrent, true, true, true, false, Anchor.TOP_LEFT);
        addChild(reloadDiv, true, true, true, true, Anchor.BOTTOM_RIGHT, true);

        TextRectangle r = new TextRectangle(
                new Vector2D(0.5, 1/6D),
                new Vector2D(1, filename.getDisplayedSize().getY()),
                "Search for file",
                new Color(0,0,0,0),
                Color.WHITE
        );
        Div fileDiv = new Div(
                new Vector2D(0.2 / 3, 0),
                new Vector2D(4 / 10D, 0.13)
        );
        fileDiv.addChild(r, true, true, true, false, Anchor.TOP_LEFT);
        fileDiv.addChild(filename, true, true, true, false, Anchor.BOTTOM_LEFT);
        addChild(fileDiv, true, true, true, true, Anchor.BOTTOM_LEFT, true);
    }

    private class ConfigFileList extends ScrollableList<ConfigFileListItem> {
        public Map<String, LabelConfiguration> configurationMap;
        public List<ConfigFileListItem> allItems;

        public ConfigFileList(Map<String, LabelConfiguration> configurationMap, Vector2D pos, Vector2D size) {
            super(pos, size);
            this.configurationMap = configurationMap;
            reloadItems();
            items = new ArrayList<>(allItems);
        }

        public void reloadItems() {
            allItems = configurationMap.entrySet().stream()
                    .map(e -> new ConfigFileListItem(this, e.getKey(), e.getValue()))
                    .sorted(Comparator.comparing(i -> i.file))
                    .collect(Collectors.toList());
        }

        @Override
        public void render(Vector2D mouse) {
            super.render(mouse);
        }

        public void updateItems() {
            items = allItems.stream()
                    .filter(i -> i.file.toLowerCase().contains(filename.content.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    private class ConfigFileListItem extends ScrollableListItem<ConfigFileListItem> {
        private final Button load;
        public String file;

        public ConfigFileListItem(ScrollableList<ConfigFileListItem> parent, String file, LabelConfiguration configuration) {
            super(parent);
            load = new Button("Load", Vector2D.OFFSCREEN, new Vector2D(30, 20), (mouseButton) -> {
                if (mouseButton != Mouse.Button.LEFT) return;
                configuration.selectAsCurrent();
                paneHolder.reloadConfig();
                close();
            });
            this.file = file;

            height = 25;
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawHollowRect(pos.add(1), size.sub(2), 1, edgeColor);
            FontRenderer.drawLeftCenteredString(this.file, pos.add(5, size.getY() / 2), Color.WHITE, true);
            load.pos = pos.add(size.getX() - load.getDisplayedSize().getX() - 5, size.getY() / 2 - load.getDisplayedSize().getY() / 2);
            load.render(mouse);
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return load.handleMouseInput(state, mousePos, button);
        }
    }
}
