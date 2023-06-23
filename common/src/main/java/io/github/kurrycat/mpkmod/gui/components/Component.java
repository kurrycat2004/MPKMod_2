package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.github.kurrycat.mpkmod.util.JSONPos2D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class Component extends ComponentHolder {
    public boolean selected = false;
    public boolean highlighted = false;

    protected Vector2D renderOffset = Vector2D.ZERO;

    @JsonCreator
    public Component() {
    }

    public abstract void render(Vector2D mouse);

    @SuppressWarnings("UnusedReturnValue")
    public Component setRenderOffset(Vector2D renderOffset) {
        this.renderOffset = renderOffset.copy();
        return this;
    }

    public Vector2D getRenderOffset() {
        return renderOffset;
    }

    @SuppressWarnings("unused")
    @JsonGetter("pos")
    public JSONPos2D getJsonPos() {
        return new JSONPos2D(this.pos, this.anchor, this.parentAnchor, this.percentFlag);
    }

    @SuppressWarnings("unused")
    @JsonSetter("pos")
    public void setPosFromJson(JSONPos2D pos) {
        this.anchor = pos.getAnchor();
        this.parentAnchor = pos.getParentAnchor();
        this.percentFlag = pos.getPercentFlag();
        this.setPos(pos.getPos());
    }

    @SuppressWarnings("unused")
    @JsonProperty("size")
    public Vector2D getSizeForJson() {
        return this.size;
    }

    @SuppressWarnings("unused")
    @JsonProperty("size")
    public void setSizeForJson(Vector2D size) {
        this.size = size;
    }

    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        return menu;
    }

    public boolean contains(Vector2D testPos) {
        if (testPos == null) return false;
        if (getDisplayedPos() == null) return false;
        return testPos.isInRectBetween(getDisplayedPos(), getDisplayedPos().add(getDisplayedSize()));
    }

    @Override
    public Vector2D getDisplayedPos() {
        if (selected && renderOffset != null)
            return super.getDisplayedPos().add(renderOffset);
        return super.getDisplayedPos();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}
