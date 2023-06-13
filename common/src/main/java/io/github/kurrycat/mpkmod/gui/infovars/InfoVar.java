package io.github.kurrycat.mpkmod.gui.infovars;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfoVar {
    private final InfoTree node;
    private final List<Object> objects;
    private final String name;

    public InfoVar(String name, List<Object> objects) {
        this.name = name;
        this.objects = objects;
        this.node = new InfoTree();
    }

    public Set<Map.Entry<String, InfoVar>> getEntries() {
        return node.getEntries();
    }

    public InfoVar getElement(List<String> name) {
        return node.getElement(name);
    }

    public String getName() {
        return name;
    }

    public InfoVar createChild(String name, List<Object> objects) {
        InfoVar child = new InfoVar(name, objects);
        child.setParent(node);
        node.addElement(name, child);
        return child;
    }

    public void setParent(InfoTree infoTree) {
        this.node.setParent(infoTree);
    }

    public Object getObj() {
        Object o = InfoString.getObj(objects);
        return o != null ? o : "undefined";
    }
}
