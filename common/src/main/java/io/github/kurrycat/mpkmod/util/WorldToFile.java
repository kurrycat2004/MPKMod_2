package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.WorldInteraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class WorldToFile {
    public static final String WORLD_DIR = "config/mpk/pkc/";

    private static String blockName;
    private static String blockColor;

    public static void parseWorld(int radius) {
        File dir = new File(WORLD_DIR);
        if (!dir.exists()) dir.mkdir();
        File csvOutputFile = new File(WORLD_DIR + "test.bcsv");

        Player player = Player.getLatest();
        if (player == null) return;

        Vector3D playerPos = player.getPos().floor();

        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            pw.println("X,Y,Z,TYPE,TIER,COLOR,TOP,BOTTOM,NORTH,EAST,SOUTH,WEST");
            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    for (int k = -radius; k <= radius; k++) {
                        blockName = WorldInteraction.getBlockName(playerPos.add(i, j, k));
                        if (blockName == null) continue;

                        HashMap<String, String> properties = WorldInteraction.getBlockProperties(playerPos.add(i, j, k));
                        if (properties == null) continue;

                        List<BoundingBox3D> bbs = WorldInteraction.getCollisionBoundingBoxes(playerPos.add(i, j, k));
                        if (bbs.isEmpty()) continue;

                        if (blockName.startsWith("minecraft:")) blockName = blockName.substring(10);
                        String wood = "0xc8961eff";
                        String darkIron = "0x272727ff";
                        String stone = "0x999999ff";
                        String plant = "0x6fd77eff";

                        if (blockName.equals("air")) continue;
                        else if (blockName.contains("anvil")) applyValues("Anvil", darkIron);
                        else if (blockName.contains("bed") && !blockName.contains("bedrock"))
                            applyValues("Bed", "0xff6a6aff");
                        else if (blockName.contains("brewing")) applyValues("BrewingStand", "0xfff39aff");
                        else if (blockName.contains("cactus")) applyValues("Cactus", "0x008323ff");
                        else if (blockName.contains("cake")) applyValues("Cake", "0xffc4c4ff");
                        else if (blockName.contains("carpet")) applyValues("Carpet", "0x9b9b9bff");
                        else if (blockName.contains("cauldron")) applyValues("Cauldron", darkIron);
                        else if (blockName.contains("dragon")) applyValues("DragonEgg", "0x7557ffff");
                        else if (blockName.contains("end_portal")) applyValues("EndPortalFrame", "0xd8d92dff");
                        else if (blockName.contains("hopper")) applyValues("Hopper", darkIron);
                        else if (blockName.contains("ice")) applyValues("Ice", "0x97fff2ff");
                        else if (blockName.contains("ladder")) applyValues("Ladder", wood);
                        else if (blockName.contains("lava")) applyValues("Lava", "0xd9343455");
                        else if (blockName.equals("piston_head")) applyValues("PistonHead", wood);
                        else if (blockName.contains("slime")) applyValues("Slime", "0x009915ff");
                        else if (blockName.contains("vine")) applyValues("Vine", plant);
                        else if (blockName.contains("water")) applyValues("Water", "0x0a46a55");
                        else if (blockName.contains("stair")) applyValues("Stair", stone);
                        else if (blockName.contains("piston")) applyValues("PistonBase", stone);
                        else if (blockName.contains("pane")) applyValues("Pane", "0xc3c3c388");
                        else if (blockName.contains("wall")) applyValues("Cobblewall", stone);
                        else if (blockName.equals("waterlily")) applyValues("Lilypad", plant);
                        else if (blockName.equals("snow_layer")) applyValues("Snow", "0xffffffff");
                        else if (blockName.contains("chest")) applyValues("Enderchest", "0x5239c5ff");
                        else if (blockName.equals("soul_sand")) applyValues("Soulsand", "0x523600ff");
                        else if (blockName.contains("skull")) applyValues("Head", "0xb9b9b9ff");
                        else if (blockName.contains("cocoa")) applyValues("CocoaBean", "0x7b5100ff");
                        else if (blockName.equals("web")) applyValues("Cobweb", "0xaaaaaa55");
                        else if (blockName.equals("flower_pot")) applyValues("Flowerpot", "0xd24a00ff");
                        else if (blockName.contains("trapdoor")) applyValues("Trapdoor", wood);
                        else if (blockName.contains("fence")) {
                            if (blockName.contains("fence_gate")) blockName = "FenceGate";
                            else applyValues("Fence", wood);
                        } else if (blockName.contains("slab")) applyValues("Stair", stone);
                        else applyValues("StandardBlock", stone);

                        int tier = 0;
                        if (properties.containsKey("age")) tier = MathUtil.parseInt(properties.get("age"), 0);
                        else if (properties.containsKey("bites")) tier = MathUtil.parseInt(properties.get("bites"), 0);
                        else if (properties.containsKey("layers"))
                            tier = MathUtil.parseInt(properties.get("layers"), 0);

                        boolean top = (!blockName.equals("Trapdoor") && properties.containsKey("half") && properties.get("half").equals("top")) ||
                                properties.containsKey("facing") && properties.get("facing").equals("up");
                        boolean bottom = (!blockName.equals("Trapdoor") && properties.containsKey("half") && properties.get("half").equals("bottom")) ||
                                properties.containsKey("facing") && properties.get("facing").equals("down");
                        boolean north = properties.containsKey("north") && properties.get("north").equals("true") ||
                                properties.containsKey("facing") && properties.get("facing").equals("north");
                        boolean east = properties.containsKey("east") && properties.get("east").equals("true") ||
                                properties.containsKey("facing") && properties.get("facing").equals("east");
                        boolean south = properties.containsKey("south") && properties.get("south").equals("true") ||
                                properties.containsKey("facing") && properties.get("facing").equals("south");
                        boolean west = properties.containsKey("west") && properties.get("west").equals("true") ||
                                properties.containsKey("facing") && properties.get("facing").equals("west");

                        if (blockName.equals("PistonBase")) {
                            if (top) {
                                top = false;
                                bottom = true;
                            } else if (bottom) {
                                top = true;
                                bottom = false;
                            }
                        }

                        if (blockName.equals("FenceGate") && (north || south)) {
                            west = east = true;
                            north = south = false;
                            applyValues("Fence", wood);
                        } else if (blockName.equals("FenceGate") && (west || east)) {
                            north = south = true;
                            west = east = false;
                            applyValues("Fence", wood);
                        }

                        pw.println(
                                String.format(
                                        "%d,%d,%d,%s,%d,%s,%b,%b,%b,%b,%b,%b",
                                        i, j, k, blockName, tier, blockColor, top, bottom, north, east, south, west
                                )
                        );
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyValues(String name, String color) {
        blockName = name;
        blockColor = color;
    }

}
