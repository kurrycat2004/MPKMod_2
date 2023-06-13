package io.github.kurrycat.mpkmod.gui.infovars;

import io.github.kurrycat.mpkmod.util.Debug;

import java.util.*;

public class InfoTree {
    private final HashMap<String, InfoVar> elements = new HashMap<>();
    private InfoTree parentTree = null;
    private int size = 0;

    public void addElement(String name, InfoVar var) {
        if (name.contains(".")) {
            Debug.stacktrace("Expected String without \".\", got: \"" + name + "\"");
            return;
        }

        var.setParent(this);
        elements.put(name, var);
        size++;
        if (this.parentTree != null) this.parentTree.size++;
    }

    public Set<Map.Entry<String, InfoVar>> getEntries() {
        return elements.entrySet();
    }

    public InfoVar getElement(String name) {
        List<String> l = Arrays.asList(name.split("\\."));
        return getElement(l);
    }

    public InfoVar getElement(List<String> path) {
        if (path.size() < 1) return null;
        else if (path.size() == 1) return elements.getOrDefault(path.get(0), null);
        else {
            if (!elements.containsKey(path.get(0))) return null;
            return elements.get(path.get(0))
                    .getElement(path.subList(1, path.size()));
        }
    }

    public int getSize() {
        return size;
    }

    public void setParent(InfoTree infoTree) {
        this.parentTree = infoTree;
    }
}
