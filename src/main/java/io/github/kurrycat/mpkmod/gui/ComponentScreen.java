package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.BoundingBox2D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ComponentScreen extends MPKGuiScreen implements PaneHolder, MouseInputListener, MouseScrollListener {
    public ArrayList<Button> components = new ArrayList<>();
    public ArrayList<Pane> openPanes = new ArrayList<>();

    public ArrayList<Component> movableComponents = new ArrayList<>();
    public Set<Component> selected = new HashSet<>();
    public Set<Component> holding = new HashSet<>();

    private Vector2D lastClickedPos = null;
    private Component lastClicked = null;
    private Vector2D holdingSetPosOffset = null;

    public void onGuiInit() {
        super.onGuiInit();
        movableComponents.clear();
        components.clear();
        selected.clear();
        holding.clear();
        lastClicked = null;
        lastClickedPos = null;
        holdingSetPosOffset = null;
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        movableComponents.forEach(c -> c.setSelected(false));
        selected.clear();
        holding.clear();
    }

    public void onKeyEvent(int keyCode, String key, boolean pressed) {
        super.onKeyEvent(keyCode, key, pressed);

        if (pressed && !selected.isEmpty()) {
            Vector2D arrowKeyMove = Vector2D.ZERO;
            switch (key) {
                case "LEFT":
                    arrowKeyMove = Vector2D.LEFT;
                    break;
                case "RIGHT":
                    arrowKeyMove = Vector2D.RIGHT;
                    break;
                case "UP":
                    arrowKeyMove = Vector2D.UP;
                    break;
                case "DOWN":
                    arrowKeyMove = Vector2D.DOWN;
                    break;
            }
            BoundingBox2D containingSelected = boundingBoxContainingAll(new ArrayList<>(selected));
            Vector2D toMove = arrowKeyMove.constrain(Vector2D.ZERO.sub(containingSelected.getMin()), Renderer2D.getScaledSize().sub(containingSelected.getMax()));

            selected.forEach(c -> c.setPos(c.getPos().add(toMove)));
        }
    }

    public void onMouseClicked(Vector2D mouse, int mouseButton) {
        super.onMouseClicked(mouse, mouseButton);

        if (handleMouseInput(Mouse.State.DOWN, mouse, Mouse.Button.fromInt(mouseButton))) return;

        if (Mouse.Button.LEFT.equals(mouseButton)) {
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
        } else if (Mouse.Button.RIGHT.equals(mouseButton)) {
            if (lastClickedPos != null && lastClicked == null)
                lastClickedPos = null;
        }
    }

    public void onMouseClickMove(Vector2D mouse, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouse, mouseButton, timeSinceLastClick);

        if (handleMouseInput(Mouse.State.DRAG, mouse, Mouse.Button.fromInt(mouseButton))) return;

        selected = selected.stream().filter(c -> holding.contains(c)).collect(Collectors.toCollection(HashSet::new));
    }

    public void onMouseReleased(Vector2D mouse, int mouseButton) {
        super.onMouseReleased(mouse, mouseButton);

        if (handleMouseInput(Mouse.State.UP, mouse, Mouse.Button.fromInt(mouseButton))) return;

        if (Mouse.Button.LEFT.equals(mouseButton) && lastClickedPos != null) {
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

    public void onMouseScroll(Vector2D mousePos, int delta) {
        handleMouseScroll(mousePos, delta);
    }

    public void openPane(Pane p) {
        openPanes.add(p);
        p.setParent(this);
        p.setLoaded(true);

        selected.clear();
        holding.clear();
        lastClicked = null;
        lastClickedPos = null;
        holdingSetPosOffset = null;
    }

    public void closePane(Pane p) {
        openPanes.remove(p);
        p.setLoaded(false);
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (!openPanes.isEmpty()) {
            openPanes.get(openPanes.size() - 1).handleMouseInput(state, mousePos, button);
            return true;
        }
        return ArrayListUtil.orMap(
                ArrayListUtil.getAllOfType(MouseInputListener.class, components, movableComponents),
                b -> b.handleMouseInput(state, mousePos, button)
        );
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (!openPanes.isEmpty())
            openPanes.get(openPanes.size() - 1).handleMouseScroll(mousePos, delta);
        return !openPanes.isEmpty();
    }

    public ArrayList<Component> findContainPos(Vector2D p) {
        return movableComponents.stream().filter(c -> c.contains(p)).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Component> findAreContainedIn(Vector2D pos1, Vector2D pos2) {
        return movableComponents.stream().filter(c -> c.getPos().isInRectBetween(pos1, pos2)).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Component> overlap(Vector2D p1, Vector2D p2) {
        return movableComponents.stream().filter(
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

    public BoundingBox2D boundingBoxContainingAll(ArrayList<Component> components) {
        if (components.isEmpty()) return null;

        Vector2D min = null, max = null;
        for (Component c : components) {
            Vector2D p = c.getDisplayPos();
            Vector2D p2 = p.add(c.getSize());
            if (min == null) min = new Vector2D(p);
            if (max == null) max = new Vector2D(p.add(c.getSize()));

            if (p.getX() < min.getX()) min.setX(p.getX());
            if (p2.getX() > max.getX()) max.setX(p2.getX());
            if (p.getY() < min.getY()) min.setY(p.getY());
            if (p2.getY() > max.getY()) max.setY(p2.getY());
        }
        return new BoundingBox2D(min, max);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        if (openPanes.isEmpty()) drawDefaultBackground();

        movableComponents.forEach(c -> c.setSelected(selected.contains(c)));

        for (Component component : movableComponents) {
            if (!holding.contains(component))
                component.render(mouse);
        }

        Vector2D hoverMousePos = openPanes.isEmpty() ? mouse : Vector2D.OFFSCREEN;
        for (Button b : components) b.render(hoverMousePos);

        if (!holding.isEmpty()) {
            BoundingBox2D containingHolding = boundingBoxContainingAll(new ArrayList<>(holding));

            Vector2D toMove = mouse.sub(lastClickedPos);
            toMove = toMove.constrain(Vector2D.ZERO.sub(containingHolding.getMin()), Renderer2D.getScaledSize().sub(containingHolding.getMax()));
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

        if (!openPanes.isEmpty()) {
            drawDefaultBackground();
            for (int i = 0; i < openPanes.size() - 1; i++) {
                openPanes.get(i).render(Vector2D.OFFSCREEN);
            }
            openPanes.get(openPanes.size() - 1).render(mouse);
        }
    }
}
