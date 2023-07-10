package io.github.kurrycat.mpkmod.gui.infovars;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class InfoVar {
    private final InfoTree childTree;
    private final List<Object> objects;
    private final String name;

    public InfoVar(String name, List<Object> objects) {
        this.name = name;
        this.objects = objects;
        this.childTree = new InfoTree(this);
    }

    public Set<Map.Entry<String, InfoVar>> getEntries() {
        return childTree.getEntries();
    }

    public InfoVar getElement(List<String> name) {
        return childTree.getElement(name);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        if (this.childTree.getParent() == null || this.childTree.getParent().getNode() == null) return this.name;
        return this.childTree.getParent().getNode().getFullName() + "." + this.name;
    }

    public InfoVar createChild(String name, List<Object> objects) {
        InfoVar child = new InfoVar(name, objects);
        child.setParent(childTree);
        childTree.addElement(name, child);
        return child;
    }

    public void setParent(InfoTree infoTree) {
        this.childTree.setParent(infoTree);
    }

    public Object getObj() {
        Object o = InfoString.getObj(objects);
        return o != null ? o : "undefined";
    }
}
