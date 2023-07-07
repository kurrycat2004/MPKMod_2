package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.InputConstants;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Player;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.Component;
import io.github.kurrycat.mpkmod.gui.components.PopupMenu;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseScrollListener;
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
    public ArrayList<Pane<?>> openPanes = new ArrayList<>();

    public ArrayList<Component> movableComponents = new ArrayList<>();
    public Set<Component> selected = new HashSet<>();
    public Set<Component> holding = new HashSet<>();
    public Set<Component> highlighted = new HashSet<>();

    private Vector2D lastClickedPos = null;
    private Component lastClicked = null;
    private Vector2D holdingSetPosOffset = null;

    public void postMessage(String receiverID, String content, boolean highlighted) {
        MessageQueue q = MessageQueue.getReceiverFor(receiverID, ArrayListUtil.getAllOfType(MessageQueue.class, movableComponents));
        if (q != null)
            q.postMessage(content, highlighted);
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

    public void onKeyEvent(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        super.onKeyEvent(keyCode, keyCode, modifiers, isCharTyped);

        if (handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)) return;

        if (!isCharTyped && !selected.isEmpty()) {
            Vector2D arrowKeyMove = Vector2D.ZERO;
            switch (keyCode) {
                case InputConstants.KEY_LEFT:
                    arrowKeyMove = Vector2D.LEFT;
                    break;
                case InputConstants.KEY_RIGHT:
                    arrowKeyMove = Vector2D.RIGHT;
                    break;
                case InputConstants.KEY_UP:
                    arrowKeyMove = Vector2D.UP;
                    break;
                case InputConstants.KEY_DOWN:
                    arrowKeyMove = Vector2D.DOWN;
                    break;
            }
            BoundingBox2D containingSelected = boundingBoxContainingAll(new ArrayList<>(selected));
            Vector2D toMove = arrowKeyMove.constrain(
                    Vector2D.ZERO.sub(containingSelected.getMin()),
                    getDisplayedSize().sub(containingSelected.getMax())
            );

            selected.forEach(c -> c.addPos(toMove));
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
                    Vector2D windowSize = getScreenSize();
                    Vector2D cPos = clicked.getDisplayedPos();
                    Vector2D cSize = clicked.getDisplayedSize();
                    openPane(menu,
                            new Vector2D(
                                    cPos.getX() + cSize.getX() + menu.getDisplayedSize().getX() + 1 < windowSize.getX() ?
                                            cPos.getX() + cSize.getX() + 1 : cPos.getX() - menu.getDisplayedSize().getX() - 1,
                                    clicked.getDisplayedPos().getY()
                            )
                    );
                }
            } else if (selected.size() > 0) {
                highlighted.addAll(selected);
                selected.clear();
                PopupMenu menu = new PopupMenu();
                menu.addComponent(new Button("Delete", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    for (Component c : highlighted)
                        menu.paneHolder.removeComponent(c);
                    menu.close();
                }));
                openPane(menu, mouse);
            } else {
                highlighted.clear();
                PopupMenu menu = new PopupMenu();
                PopupMenu newLabelMenu = new PopupMenu();
                newLabelMenu.addComponent(new Button("Add InfoLabel", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    InfoLabel infoLabel = new InfoLabel("Example Label");
                    infoLabel.setPos(mouse);
                    addComponent(infoLabel);
                    menu.close();
                }));
                newLabelMenu.addComponent(new Button("Add KeyBindingLabel", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    KeyBindingLabel keyBindingLabel = new KeyBindingLabel(mouse, new Vector2D(20, 20), "key.forward");
                    addComponent(keyBindingLabel);
                    menu.close();
                }));
                newLabelMenu.addComponent(new Button("Add MessageQueue", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    MessageQueue messageQueue = new MessageQueue("Example MessageQueue");
                    messageQueue.setPos(mouse);
                    messageQueue.setSize(new Vector2D(30, 22));
                    addComponent(messageQueue);
                    menu.close();
                }));
                newLabelMenu.addComponent(new Button("Add BarrierDisplay", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    BarrierDisplayComponent barrierDisplay = new BarrierDisplayComponent();
                    barrierDisplay.setPos(mouse);
                    barrierDisplay.setSize(new Vector2D(30, 30));
                    addComponent(barrierDisplay);
                    menu.close();
                }));
                newLabelMenu.addComponent(new Button("Add Plot (WIP)", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    Plot plot = new Last45Plot();
                    plot.setPos(mouse);
                    plot.setSize(new Vector2D(40, 40));
                    addComponent(plot);
                    menu.close();
                }));
                newLabelMenu.addComponent(new Button("Add Angle path (WIP)", b -> {
                    if (b != Mouse.Button.LEFT) return;
                    AnglePath path = new AnglePath(false);
                    path.setPos(mouse);
                    path.setSize(new Vector2D(40, 40));
                    addComponent(path);
                    menu.close();
                }));

                menu.addSubMenu(new Button("Add Label"), newLabelMenu);
                openPane(menu, mouse);
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
                    c.setRenderOffset(Vector2D.ZERO);
                    c.addPos(holdingSetPosOffset);
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

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (!openPanes.isEmpty())
            openPanes.get(openPanes.size() - 1).handleMouseScroll(mousePos, delta);
        return ArrayListUtil.orMap(
                ArrayListUtil.getAllOfType(MouseScrollListener.class, components, movableComponents),
                b -> b.handleMouseScroll(mousePos, delta)
        );
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
        if (openPanes.isEmpty() || openPanes.get(openPanes.size() - 1) instanceof PopupMenu) drawDefaultBackground();
        Vector2D hoverMousePos = openPanes.isEmpty() ? mouse : new Vector2D(-1, -1);

        movableComponents.forEach(c -> c.setSelected(selected.contains(c)));
        movableComponents.forEach(c -> c.setHighlighted(highlighted.contains(c)));

        for (Component component : movableComponents) {
            if (holding.contains(component)) {
                Vector2D offset = component.getRenderOffset();
                component.setRenderOffset(Vector2D.ZERO);
                component.render(hoverMousePos);
                component.setRenderOffset(offset);
            } else component.render(hoverMousePos);
        }

        for (Component b : components) b.render(hoverMousePos);

        if (!holding.isEmpty()) {
            BoundingBox2D containingHolding = boundingBoxContainingAll(new ArrayList<>(holding));

            Vector2D toMove = mouse.sub(lastClickedPos);
            toMove = toMove.constrain(
                    containingHolding.getMin().mult(-1),
                    getScreenSize().sub(containingHolding.getMax())
            );
            holdingSetPosOffset = toMove;
            for (Component component : holding) {
                component.setRenderOffset(toMove);
                component.render(hoverMousePos);
            }
        }

        if (lastClickedPos != null && lastClicked == null && !mouse.equals(lastClickedPos)) {
            Vector2D p = new Vector2D(Math.min(lastClickedPos.getX(), mouse.getX()), Math.min(lastClickedPos.getY(), mouse.getY()));
            Vector2D s = new Vector2D(Math.max(lastClickedPos.getX(), mouse.getX()), Math.max(lastClickedPos.getY(), mouse.getY())).sub(p);
            Renderer2D.drawHollowRect(p, s, 1, Color.RED);
        }

        if (!openPanes.isEmpty()) {
            Pane<?> last = openPanes.get(openPanes.size() - 1);
            if (!(last instanceof PopupMenu))
                drawDefaultBackground();
            for (int i = 0; i < openPanes.size() - 1; i++) {
                openPanes.get(i).render(Vector2D.OFFSCREEN);
            }
            last.render(mouse);
        }
    }

    public ArrayList<Component> overlap(Vector2D p1, Vector2D p2) {
        return movableComponents.stream().filter(
                c -> {
                    Vector2D c1 = c.getDisplayedPos();
                    Vector2D c2 = c.getDisplayedPos().add(c.getDisplayedSize());

                    if (c1.getX() > p2.getX() || c2.getX() < p1.getX()) return false;
                    //noinspection RedundantIfStatement
                    if (c1.getY() > p2.getY() || c2.getY() < p1.getY()) return false;
                    return true;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
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

    public Component findFirstContainPos(Vector2D p) {
        ArrayList<Component> containPos = findContainPos(p);
        if (containPos.isEmpty()) return null;
        return containPos.get(0);
    }

    @SuppressWarnings("unchecked")
    public <T extends PaneHolder> void openPane(Pane<T> p, Vector2D pos) {
        openPanes.add(p);
        p.setPaneHolder((T) this);
        p.setLoaded(true);
        p.setPos(pos);

        cleanupScreen();
    }

    public <T extends PaneHolder> void closePane(Pane<T> p) {
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

    public ArrayList<Component> findContainPos(Vector2D p) {
        return movableComponents.stream().filter(c -> c.contains(p)).collect(Collectors.toCollection(ArrayList::new));
    }

    private void cleanupScreen() {
        selected.clear();
        holding.clear();
        lastClicked = null;
        lastClickedPos = null;
        holdingSetPosOffset = null;
    }

    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        if (!openPanes.isEmpty())
            openPanes.get(openPanes.size() - 1).handleKeyInput(keyCode, scanCode, modifiers, isCharTyped);
        return ArrayListUtil.orMap(
                ArrayListUtil.getAllOfType(KeyInputListener.class, components, movableComponents),
                b -> b.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
        );
    }

    public BoundingBox2D boundingBoxContainingAll(ArrayList<Component> components) {
        if (components.isEmpty()) return null;

        Vector2D min = null, max = null;
        for (Component c : components) {
            Vector2D p = c.getDisplayedPos().sub(c.getRenderOffset());
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

    @SuppressWarnings("unchecked")
    public <T extends PaneHolder> void openPane(Pane<T> p) {
        openPanes.add(p);
        p.setPaneHolder((T) this);
        p.setLoaded(true);

        cleanupScreen();
    }

    public final void closeAllPanes() {
        for (int i = openPanes.size() - 1; i >= 0; i--) {
            closePane(openPanes.get(i));
        }
    }
}
