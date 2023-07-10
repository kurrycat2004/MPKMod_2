package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SaveConfigPane extends Pane<MainGuiScreen> {
    public static Color edgeColor = new Color(255, 255, 255, 95);
    private InputField filename;
    private ConfigFileList savedConfigs;

    public SaveConfigPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        initComponents();
    }

    private void initComponents() {
        savedConfigs = new ConfigFileList(
                LabelConfiguration.savedConfigs,
                new Vector2D(0.2 / 3, 0.05),
                new Vector2D(4 / 10D, 0.9));
        savedConfigs.setTitle("Saved Configurations");
        addChild(savedConfigs, PERCENT.ALL, Anchor.TOP_RIGHT);

        TextRectangle r = new TextRectangle(
                new Vector2D(0, 40),
                new Vector2D(1, 15),
                "Filename",
                new Color(0, 0, 0, 0),
                Color.WHITE
        );
        filename = new InputField(
                "",
                new Vector2D(0, 25),
                0.9D)
                .setFilter(InputField.FILTER_FILENAME)
                .setOnContentChange(content -> savedConfigs.updateItems());
        Button b = new Button(
                "Save",
                new Vector2D(0, 0),
                new Vector2D(0.9D, 20),
                mouseButton -> {
                    if (mouseButton != Mouse.Button.LEFT) return;
                    if (filename.content.isEmpty()) return;
                    LabelConfiguration.currentConfig.save(filename.content);
                    filename.clear();
                    savedConfigs.reloadItems();
                    savedConfigs.updateItems();
                    paneHolder.loadConfigPane.reload();
                }
        );
        Div fileDiv = new Div(
                new Vector2D(0.2 / 3D, 0),
                new Vector2D(4 / 10D, 50)
        );
        fileDiv.addChild(r, PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);
        fileDiv.addChild(filename, PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);
        fileDiv.addChild(b, PERCENT.SIZE_X, Anchor.BOTTOM_CENTER);
        addChild(fileDiv, PERCENT.POS_X | PERCENT.SIZE_X, Anchor.CENTER_LEFT);
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
            allItems = configurationMap.keySet().stream()
                    .map(configuration -> new ConfigFileListItem(this, configuration))
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
        private final Button delete;
        public String file;

        public ConfigFileListItem(ConfigFileList parent, String file) {
            super(parent);
            this.setHeight(25);
            this.file = file;

            delete = new Button("Delete", new Vector2D(5, 0), new Vector2D(30, 20), (mouseButton) -> {
                if (mouseButton != Mouse.Button.LEFT) return;
                LabelConfiguration.delete(file);
                parent.reloadItems();
                parent.updateItems();
                paneHolder.loadConfigPane.reload();
            });
            delete.textColor = Color.RED;
            addChild(delete, PERCENT.NONE, Anchor.CENTER_RIGHT);
        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawHollowRect(pos.add(1), size.sub(2), 1, edgeColor);
            FontRenderer.drawLeftCenteredString(this.file, pos.add(5, size.getY() / 2), Color.WHITE, true);
            delete.render(mouse);
        }

        @Override
        public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
            return delete.handleMouseInput(state, mousePos, button);
        }
    }
}
