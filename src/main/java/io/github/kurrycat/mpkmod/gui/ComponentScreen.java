package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.PopupMenu;
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

public abstract class ComponentScreen extends MPKGuiScreen implements PaneHolder, MouseInputListener, MouseScrollListener, KeyInputListener, MessageReceiver {
    public ArrayList<Pane> openPanes = new ArrayList<>();

    public ArrayList<Component> movableComponents = new ArrayList<>();
    public Set<Component> selected = new HashSet<>();
    public Set<Component> holding = new HashSet<>();
    public Set<Component> highlighted = new HashSet<>();

    private Vector2D lastClickedPos = null;
    private Component lastClicked = null;
    private Vector2D holdingSetPosOffset = null;

    public void postMessage(String receiverID, String content) {
        MessageQueue q = MessageQueue.getReceiverFor(receiverID, ArrayListUtil.getAllOfType(MessageQueue.class, movableComponents));
        if (q != null)
            q.postMessage(content);
    }

    public void onGuiInit() {
        super.onGuiInit();
        movableComponents.clear();
        components.clear();
        selected.clear();
        holding.clear();
        highlighted.clear();
        lastClicked = null;
        lastClickedPos = null;
        holdingSetPosOffset = null;
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        movableComponents.forEach(c -> c.setSelected(false));
        movableComponents.forEach(c -> c.setHighlighted(false));
        selected.clear();
        holding.clear();
        highlighted.clear();
    }

    public void onKeyEvent(char keyCode, String key, boolean pressed) {
        super.onKeyEvent(keyCode, key, pressed);

        if (handleKeyInput(keyCode, key, pressed)) return;

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

            selected.forEach(c -> c.setPos(c.getPos().add(c.getParentAnchor().translateMovement(toMove))));
        }
    }

    public void onMouseClicked(Vector2D mouse, int mouseButton) {
        super.onMouseClicked(mouse, mouseButton);

        if (handleMouseInput(Mouse.State.DOWN, mouse, Mouse.Button.fromInt(mouseButton))) return;

        if (movableComponents.isEmpty()) return;

        if (Mouse.Button.LEFT.equals(mouseButton)) {
            highlighted.clear();
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
            highlighted.clear();
            if (lastClickedPos != null && lastClicked == null) {
                lastClickedPos = null;
            }
            Component clicked = findFirstContainPos(mouse);
            if (selected.size() <= 1 && clicked != null) {
                highlighted.add(clicked);
                PopupMenu menu = clicked.getPopupMenu();
                if (menu != null) {
                    Vector2D windowSize = Renderer2D.getScaledSize();
                    menu.pos = new Vector2D(
                            clicked.getDisplayedPos().getX() + clicked.getDisplayedSize().getX() + menu.getDisplayedSize().getX() + 1 < windowSize.getX() ?
                                    clicked.getDisplayedPos().getX() + clicked.getDisplayedSize().getX() + 1 : clicked.getDisplayedPos().getX() - menu.getDisplayedSize().getX() - 1,
                            clicked.getDisplayedPos().getY()
                    );
                    openPane(menu);
                }
            } else if (selected.size() > 0) {
                highlighted.addAll(selected);
                selected.clear();
                PopupMenu menu = new PopupMenu();
                menu.addComponent(
                        new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(30, 11), mButton -> {
                            if (Mouse.Button.LEFT.equals(mButton)) {
                                for (Component c : highlighted) {
                                    menu.paneHolder.removeComponent(c);
                                }
                                menu.paneHolder.closePane(menu);
                            }
                        })
                );
                menu.pos = mouse;
                openPane(menu);
            } else {
                highlighted.clear();
                PopupMenu menu = new PopupMenu();
                Vector2D windowSize = Renderer2D.getScaledSize();
                NewLabelPane newLabelPane = new NewLabelPane(
                        new Vector2D(windowSize.getX() * 0.35, windowSize.getY() * 0.5 - 20),
                        new Vector2D(windowSize.getX() * 0.3, 60)
                );
                newLabelPane.setCreationPos(mouse);
                menu.addComponent(
                        new Button("Add Label", mouse, new Vector2D(42, 11), mButton -> {
                            if (Mouse.Button.LEFT.equals(mButton)) {
                                menu.paneHolder.openPane(newLabelPane);
                                menu.paneHolder.closePane(menu);
                            }
                        })
                );
                menu.pos = mouse;
                openPane(menu);
            }
        }
    }

    public void onMouseClickMove(Vector2D mouse, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouse, mouseButton, timeSinceLastClick);

        if (handleMouseInput(Mouse.State.DRAG, mouse, Mouse.Button.fromInt(mouseButton))) return;

        if (movableComponents.isEmpty()) return;

        selected = selected.stream().filter(c -> holding.contains(c)).collect(Collectors.toCollection(HashSet::new));
    }

    public void onMouseReleased(Vector2D mouse, int mouseButton) {
        super.onMouseReleased(mouse, mouseButton);

        if (handleMouseInput(Mouse.State.UP, mouse, Mouse.Button.fromInt(mouseButton))) return;

        if (movableComponents.isEmpty()) return;

        if (Mouse.Button.LEFT.equals(mouseButton) && lastClickedPos != null) {
            boolean moved = lastClickedPos.sub(mouse).lengthSqr() > 3 * 3;
            if (!moved && lastClicked != null) {
                selected.clear();
                selected.add(lastClicked);
            }

            if (holdingSetPosOffset != null) {
                for (Component c : holding) {
                    c.setPos(c.getPos().add(c.getParentAnchor().translateMovement(holdingSetPosOffset)));
                }
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
        p.setPaneHolder(this);
        p.setLoaded(true);

        selected.clear();
        holding.clear();
        lastClicked = null;
        lastClickedPos = null;
        holdingSetPosOffset = null;
    }

    public void closePane(Pane p) {
        openPanes.remove(p);
        if (openPanes.isEmpty()) {
            highlighted.clear();
        }
        p.setLoaded(false);
    }

    public void removeComponent(Component c) {
        components.remove(c);
    }

    public void addComponent(Component c) {
        components.add(c);
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
        return ArrayListUtil.orMap(
                ArrayListUtil.getAllOfType(MouseScrollListener.class, components, movableComponents),
                b -> b.handleMouseScroll(mousePos, delta)
        );
    }

    public boolean handleKeyInput(char keyCode, String key, boolean pressed) {
        if (!openPanes.isEmpty())
            openPanes.get(openPanes.size() - 1).handleKeyInput(keyCode, key, pressed);
        return ArrayListUtil.orMap(
                ArrayListUtil.getAllOfType(KeyInputListener.class, components, movableComponents),
                b -> b.handleKeyInput(keyCode, key, pressed)
        );
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
                    Vector2D c1 = c.getDisplayedPos();
                    Vector2D c2 = c.getDisplayedPos().add(c.getDisplayedSize());

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
            Vector2D p = c.getDisplayedPos();
            Vector2D p2 = p.add(c.getDisplayedSize());
            if (min == null) min = new Vector2D(p);
            if (max == null) max = new Vector2D(p.add(c.getDisplayedSize()));

            if (p.getX() < min.getX()) min.setX(p.getX());
            if (p2.getX() > max.getX()) max.setX(p2.getX());
            if (p.getY() < min.getY()) min.setY(p.getY());
            if (p2.getY() > max.getY()) max.setY(p2.getY());
        }
        return new BoundingBox2D(min, max);
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        if (openPanes.isEmpty() || openPanes.get(openPanes.size() - 1) instanceof PopupMenu) drawDefaultBackground();
        Vector2D hoverMousePos = openPanes.isEmpty() ? mouse : new Vector2D(-1, -1);

        movableComponents.forEach(c -> c.setSelected(selected.contains(c)));
        movableComponents.forEach(c -> c.setHighlighted(highlighted.contains(c)));

        for (Component component : movableComponents) {
            if (!holding.contains(component))
                component.render(hoverMousePos);
        }

        for (Component b : components) b.render(hoverMousePos);

        if (!holding.isEmpty()) {
            BoundingBox2D containingHolding = boundingBoxContainingAll(new ArrayList<>(holding));

            Vector2D toMove = mouse.sub(lastClickedPos);
            toMove = toMove.constrain(Vector2D.ZERO.sub(containingHolding.getMin()), Renderer2D.getScaledSize().sub(containingHolding.getMax()));
            holdingSetPosOffset = toMove;
            for (Component component : holding) {
                Vector2D p = component.getPos();
                Vector2D relToMove = component.getParentAnchor().translateMovement(toMove);
                component.setPos(p.add(relToMove));
                component.render(hoverMousePos);
                component.setPos(p);
            }
        }

        if (lastClickedPos != null && lastClicked == null && !mouse.equals(lastClickedPos)) {
            Vector2D p = new Vector2D(Math.min(lastClickedPos.getX(), mouse.getX()), Math.min(lastClickedPos.getY(), mouse.getY()));
            Vector2D s = new Vector2D(Math.max(lastClickedPos.getX(), mouse.getX()), Math.max(lastClickedPos.getY(), mouse.getY())).sub(p);
            Renderer2D.drawHollowRect(p, s, 1, Color.RED);
        }

        if (!openPanes.isEmpty()) {
            Pane last = openPanes.get(openPanes.size() - 1);
            if (!(last instanceof PopupMenu))
                drawDefaultBackground();
            for (int i = 0; i < openPanes.size() - 1; i++) {
                openPanes.get(i).render(Vector2D.OFFSCREEN);
            }
            last.render(mouse);
        }
    }
}
