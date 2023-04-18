package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.compatability.MCClasses.WorldInteraction;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.StringUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapOverviewPane extends Pane {
    public MapOverviewPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = Color.DARK_GRAY;
        initComponents();
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
    }

    private void initComponents() {
        int padding = 10;

        components.add(new TextLabel("Test Label", this.getDisplayedPos().add(50)));
        components.add(new Button("TEST", getDisplayedPos().add(50, 100), new Vector2D(50, 20), mouseButton -> {
            File dir = new File("config/mpk/test/");
            if (!dir.exists()) dir.mkdir();
            File csvOutputFile = new File("config/mpk/test/test.bcsv");

            Player player = Player.getLatest();
            if (player == null) return;

            Vector3D playerPos = player.getPos().floor();

            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                pw.println("X,Y,Z,TYPE,TIER,COLOR,TOP,BOTTOM,NORTH,EAST,SOUTH,WEST");
                for (int i = -5; i <= 5; i++) {
                    for (int j = -5; j <= 5; j++) {
                        for (int k = -5; k <= 5; k++) {
                            String blockName = WorldInteraction.getBlockName(playerPos.add(i, j, k));
                            if (blockName == null) continue;

                            HashMap<String, String> properties = WorldInteraction.getBlockProperties(playerPos.add(i, j, k));
                            if (properties == null) continue;

                            if (blockName.startsWith("minecraft:")) blockName = blockName.substring(10);

                            if (blockName.equals("air")) continue;
                            else if (blockName.contains("stair")) blockName = "Stair";
                            else if (blockName.equals("piston")) blockName = "PistonBase";
                            else if (blockName.contains("pane")) blockName = "Pane";
                            else if (blockName.contains("wall")) blockName = "Cobblewall";
                            else if (blockName.equals("waterlily")) blockName = "Lilypad";
                            else if (blockName.equals("snow_layer")) blockName = "Snow";
                            else if (blockName.contains("chest")) blockName = "Enderchest";
                            else if (blockName.equals("soul_sand")) blockName = "Soulsand";
                            else if (blockName.contains("skull")) blockName = "Head";
                            else if (blockName.equals("cocoa")) blockName = "CocoaBean";
                            else if (blockName.equals("web")) blockName = "Cobweb";
                            else if (blockName.equals("flower_pot")) blockName = "Flowerpot";

                            else
                                blockName = Arrays.stream(blockName.split("_")).map(StringUtil::capitalize).collect(Collectors.joining(""));

                            List<String> blocks = Arrays.asList("Stair", "PistonHead", "PistonBase", "CocoaBean", "Snow", "Cake", "EndPortalFrame", "Head", "Water", "Lava", "Cobweb", "Slime", "Enderchest", "Anvil", "Bed", "BrewingStand", "Cactus", "Carpet", "Cauldron", "Cobblewall", "DragonEgg", "Fence", "Flowerpot", "Hopper", "Ice", "Lilypad", "Soulsand", "Trapdoor", "Pane", "Ladder", "Vine", "StandardBlock");

                            if (!blocks.contains(blockName)) {
                                blockName = "StandardBlock";
                            }

                            int tier = 0;
                            if (properties.containsKey("age")) tier = MathUtil.parseInt(properties.get("age"), 0);
                            else if(properties.containsKey("bites")) tier = MathUtil.parseInt(properties.get("bites"), 0);
                            else if(properties.containsKey("layers")) tier = MathUtil.parseInt(properties.get("layers"), 0);

                            boolean top = properties.containsKey("top") && properties.get("top").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("top");
                            boolean bottom = properties.containsKey("bottom") && properties.get("bottom").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("bottom");
                            boolean north = properties.containsKey("north") && properties.get("north").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("north");
                            boolean east = properties.containsKey("east") && properties.get("east").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("east");
                            boolean south = properties.containsKey("south") && properties.get("south").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("south");
                            boolean west = properties.containsKey("west") && properties.get("west").equals("true") ||
                                    properties.containsKey("facing") && properties.get("facing").equals("west");

                            pw.println(
                                    String.format(
                                            "%d,%d,%d,%s,%d,0xd3d3d3ff,%b,%b,%b,%b,%b,%b",
                                            i, j, k, blockName, tier, top, bottom, north, east, south, west
                                    )
                            );
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

        }));

        components.add(new CheckButton(getDisplayedPos().add(110, 100), System.out::println));

        components.add(new NumberSlider(0, 5, 1, 3, getDisplayedPos().add(50, 150), new Vector2D(100, 20), System.out::println));

        double sizeX = getDisplayedSize().getX() - getDisplayedSize().getX() / 2 - padding * 2;
        double sizeY = getDisplayedSize().getY() - padding * 2;

        ScrollableList<MapItem> mapItemList = new ScrollableList<>(getDisplayedPos().add(getDisplayedSize().getX() - sizeX - padding, padding), new Vector2D(sizeX, sizeY));

        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        mapItemList.addItem(new MapItem(mapItemList));
        components.add(mapItemList);
    }

    public static class MapItem extends ScrollableListItem<MapItem> {
        public MapItem(ScrollableList<MapItem> parent) {
            super(parent);
        }

        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            Renderer2D.drawRectWithEdge(pos, size, 1, Color.GRAY, new Color(68, 86, 152, 128));
            FontRenderer.drawCenteredString("TEST", pos.add(size.div(2)), Color.WHITE, false);
        }
    }
}
