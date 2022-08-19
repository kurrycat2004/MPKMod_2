package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ComponentScreen extends MPKGuiScreen {
    public ArrayList<Component> components = new ArrayList<>();
    public Set<Component> selected = new HashSet<>();
    public Set<Component> holding = new HashSet<>();
    private Vector2D lastClickedPos = null;
    private Component lastClicked = null;
    private Vector2D holdingSetPosOffset = null;

    public void onGuiInit() {
        super.onGuiInit();
        components.clear();
        selected.clear();
        holding.clear();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        for (Component c : components)
            c.setSelected(false);
        selected.clear();
        holding.clear();
    }

    public void onKeyEvent(int keyCode, String key, boolean pressed) {
        super.onKeyEvent(keyCode, key, pressed);
    }

    public void onMouseClicked(Vector2D mouse, int mouseButton) {
        super.onMouseClicked(mouse, mouseButton);
        if (mouseButton == Mouse.BUTTON_LEFT) {
            lastClickedPos = mouse;

            Component clicked = findFirstContainPos(lastClickedPos);
            lastClicked = clicked;

            if (clicked != null) {
                holding.clear();
                if (selected.contains(clicked))
                    holding.addAll(selected);
                selected.add(clicked);
                holding.add(clicked);
            }
        }
    }

    public void onMouseClickMove(Vector2D mouse, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouse, mouseButton, timeSinceLastClick);
        selected = selected.stream().filter(c -> holding.contains(c)).collect(Collectors.toCollection(HashSet::new));
    }

    public void onMouseReleased(Vector2D mouse, int mouseButton) {
        super.onMouseReleased(mouse, mouseButton);
        if (mouseButton == Mouse.BUTTON_LEFT) {
            boolean moved = lastClickedPos.sub(mouse).lengthSqr() > 3 * 3;
            if (!moved && lastClicked != null) {
                selected.clear();
                selected.add(lastClicked);
            }

            for (Component c : holding) {
                c.setPos(c.getPos().add(holdingSetPosOffset));
            }
            holding.clear();
            holdingSetPosOffset = null;

            if (lastClickedPos != null && lastClicked == null) {
                selected.clear();
                selected.addAll(overlap(
                        new Vector2D(Math.min(lastClickedPos.getX(), mouse.getX()), Math.min(lastClickedPos.getY(), mouse.getY())),
                        new Vector2D(Math.max(lastClickedPos.getX(), mouse.getX()), Math.max(lastClickedPos.getY(), mouse.getY()))
                ));
            }
            lastClickedPos = null;
            lastClicked = null;
        }
    }

    public ArrayList<Component> findContainPos(Vector2D p) {
        return components.stream().filter(c -> c.contains(p)).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Component> findAreContainedIn(Vector2D pos1, Vector2D pos2) {
        return components.stream().filter(c -> c.getPos().isInRectBetween(pos1, pos2)).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Component> overlap(Vector2D p1, Vector2D p2) {
        return components.stream().filter(
                c -> {
                    Vector2D c1 = c.getDisplayPos();
                    Vector2D c2 = c.getDisplayPos().add(c.getSize());

                    if (c1.getX() > p2.getX() || c2.getX() < p1.getX()) return false;
                    if (c1.getY() > p2.getY() || c2.getY() < p1.getY()) return false;
                    return true;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
    }

    public Component findFirstContainPos(Vector2D p) {
        ArrayList<Component> containPos = findContainPos(p);
        if (containPos.isEmpty()) return null;
        return containPos.get(0);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        super.drawScreen(mouse, partialTicks);
        for (Component c : components) c.setSelected(selected.contains(c));

        for (Component component : components) {
            if (!holding.contains(component))
                component.render(mouse);
        }

        if (!holding.isEmpty()) {
            Vector2D min = null, max = null;
            for (Component c : holding) {
                Vector2D p = c.getDisplayPos();
                Vector2D p2 = p.add(c.getSize());
                if (min == null) min = new Vector2D(p);
                if (max == null) max = new Vector2D(p.add(c.getSize()));

                if (p.getX() < min.getX()) min.setX(p.getX());
                if (p2.getX() > max.getX()) max.setX(p2.getX());
                if (p.getY() < min.getY()) min.setY(p.getY());
                if (p2.getY() > max.getY()) max.setY(p2.getY());
            }

            Vector2D toMove = mouse.sub(lastClickedPos);
            toMove = toMove.constrain(Vector2D.ZERO.sub(min), Renderer2D.getScaledSize().sub(max));
            holdingSetPosOffset = toMove;
            for (Component component : holding) {
                Vector2D p = component.getPos();
                component.setPos(p.add(toMove));
                component.render(mouse);
                component.setPos(p);
            }
        }

        if (lastClickedPos != null && lastClicked == null && !mouse.equals(lastClickedPos)) {
            Vector2D p = new Vector2D(Math.min(lastClickedPos.getX(), mouse.getX()), Math.min(lastClickedPos.getY(), mouse.getY()));
            Vector2D s = new Vector2D(Math.max(lastClickedPos.getX(), mouse.getX()), Math.max(lastClickedPos.getY(), mouse.getY())).sub(p);
            Renderer2D.drawHollowRect(p, s, 1, Color.RED);
        }
    }
}
