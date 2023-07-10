package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadConfigPane extends Pane<MainGuiScreen> {
    public static Color edgeColor = Theme.lightEdge;
    private InputField filename;
    private ConfigFileList presets;
    private ConfigFileList savedConfigs;

    public LoadConfigPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        initComponents();
    }

    private void initComponents() {
        filename = new InputField(
                "",
                new Vector2D(0, 1 / 3D),
                0.9D
        );
        filename.setFilter(InputField.FILTER_FILENAME);
        filename.setOnContentChange(content -> {
            presets.updateItems();
            savedConfigs.updateItems();
        });

        TextRectangle r = new TextRectangle(
                new Vector2D(0, 1 / 4D),
                new Vector2D(1, filename.getDisplayedSize().getY()),
                "Search for file",
                null,
                Color.WHITE
        );

        presets = new ConfigFileList(
                LabelConfiguration.presets,
                new Vector2D(0.2 / 3, 0.05),
                new Vector2D(0.4, 0.9));
        presets.setTitle("Presets");
        addChild(presets, PERCENT.ALL, Anchor.TOP_LEFT);

        presets.bottomCover.setHeight(0.13, true);
        presets.bottomCover.backgroundColor = null;
        presets.bottomCover.addChild(r, PERCENT.POS_Y | PERCENT.SIZE_X, Anchor.CENTER, Anchor.TOP_CENTER);
        presets.bottomCover.addChild(filename, PERCENT.POS_Y | PERCENT.SIZE_X, Anchor.CENTER, Anchor.BOTTOM_CENTER);

        savedConfigs = new ConfigFileList(
                LabelConfiguration.savedConfigs,
                new Vector2D(0.2 / 3, 0.05),
                new Vector2D(0.4, 0.9));
        savedConfigs.setTitle("Saved Configurations");
        addChild(savedConfigs, PERCENT.ALL, Anchor.TOP_RIGHT);

        Button reloadCurrent = new Button(
                "Reload from file",
                Vector2D.ZERO,
                new Vector2D(0.8, 20),
                b -> {
                    if (b != Mouse.Button.LEFT) return;
                    LabelConfiguration.currentConfig.reloadFromFile();
                    paneHolder.reloadConfig();
                }
        );

        savedConfigs.bottomCover.setHeight(0.1, true);
        savedConfigs.bottomCover.backgroundColor = null;
        savedConfigs.bottomCover.addChild(reloadCurrent, PERCENT.SIZE_X, Anchor.CENTER);

        /*Div fileDiv = new Div(
                new Vector2D(0.2 / 3, 0),
                new Vector2D(4 / 10D, 0.13)
        );
        fileDiv.addChild(r, PERCENT.POS_Y | PERCENT.SIZE_X, Anchor.CENTER, Anchor.TOP_CENTER);
        fileDiv.addChild(filename, PERCENT.POS_Y | PERCENT.SIZE_X, Anchor.CENTER, Anchor.BOTTOM_CENTER);
        addChild(fileDiv, PERCENT.ALL, Anchor.BOTTOM_LEFT);*/
    }

    public void reload() {
        presets.reloadItems();
        presets.updateItems();
        savedConfigs.reloadItems();
        savedConfigs.updateItems();
    }

    private class ConfigFileList extends ScrollableList<ConfigFileListItem> {
        public Map<String, LabelConfiguration> configurationMap;
        public List<ConfigFileListItem> allItems;

        public ConfigFileList(Map<String, LabelConfiguration> configurationMap, Vector2D pos, Vector2D size) {
            this.setPos(pos);
            this.setSize(size);
            this.configurationMap = configurationMap;
            reloadItems();
            items.clear();
            items.addAll(allItems);
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
            items.clear();
            for (ConfigFileListItem item : allItems)
                if (item.file.toLowerCase().contains(filename.content.toLowerCase()))
                    items.add(item);
        }
    }

    private class ConfigFileListItem extends ScrollableListItem<ConfigFileListItem> {
        public String file;

        public ConfigFileListItem(ScrollableList<ConfigFileListItem> parent, String file, LabelConfiguration configuration) {
            super(parent);
            this.setHeight(25);
            this.file = file;

            addChild(new Button("Load",
                    new Vector2D(5, 0), new Vector2D(30, 20),
                    (mouseButton) -> {
                        if (mouseButton != Mouse.Button.LEFT) return;
                        configuration.selectAsCurrent();
                        paneHolder.reloadConfig();
                        close();
                    }
            ), PERCENT.POS_Y, Anchor.CENTER_RIGHT);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawHollowRect(pos.add(1), size.sub(2), 1, edgeColor);
            FontRenderer.drawLeftCenteredString(this.file, pos.add(5, size.getY() / 2), Color.WHITE, true);

            renderComponents(mouse);
        }
    }
}
