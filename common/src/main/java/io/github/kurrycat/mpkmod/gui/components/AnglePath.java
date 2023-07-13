package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.*;
import io.github.kurrycat.mpkmod.gui.Theme;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseScrollListener;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.*;

public class AnglePath extends ResizableComponent implements MouseScrollListener {
    protected final Deque<DataPoint> dataPoints;
    public Color selectedColor = new Color(255, 170, 0, 100);

    @JsonProperty("mode")
    public Mode mode = Mode.X_Y;
    @JsonProperty("timeWindow")
    public int timeWindow = 10;
    protected Deque<Vector2D> renderPoints;
    protected long lastTick = -1;
    private boolean fullScreen;

    private long displayTime = 0;
    private long startTime = 0;

    @JsonCreator
    public AnglePath(@JsonProperty("fullscreen") boolean fullScreen) {
        this.fullScreen = fullScreen;
        dataPoints = new LinkedList<>();
        renderPoints = new LinkedList<>();
    }

    @JsonGetter("fullscreen")
    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen != this.fullScreen) {
            this.fullScreen = fullScreen;
            reloadRenderPoints();
        }

        this.fullScreen = fullScreen;
    }

    @Override
    public void updatePosAndSize() {
        super.updatePosAndSize();
        reloadRenderPoints();
    }

    protected void reloadRenderPoints() {
        renderPoints = new LinkedList<>();
        for (Iterator<DataPoint> it = dataPoints.descendingIterator(); it.hasNext(); ) {
            DataPoint dataPoint = it.next();
            for (Vector2D v : dataToRenderPoint(dataPoint))
                renderPoints.addFirst(v);
        }
    }

    protected Collection<Vector2D> dataToRenderPoint(DataPoint dataPoint) {
        Vector2D screenSize = Renderer2D.getScreenSize();
        double ssX = screenSize.getX(), ssY = screenSize.getY();
        Vector2D pos = fullScreen ? Vector2D.ZERO : getDisplayedPos();
        double minX = pos.getX(), minY = pos.getY();
        Vector2D size = fullScreen ? Renderer2D.getScaledSize() : getDisplayedSize();
        double maxX = minX + size.getX(), maxY = minY + size.getY();

        LinkedList<Vector2D> list = new LinkedList<>();
        int len = dataPoint.x.length;
        int time = (int) (dataPoint.tickTime - displayTime);

        for (int i = 0; i < len; i++) {
            double x, y;
            switch (mode) {
                case X_Y:
                    x = MathUtil.map(dataPoint.x[i], 0, ssX, minX, maxX)
                            + size.getX() / 2D;
                    y = MathUtil.map(-dataPoint.y[i], 0, ssY, minY, maxY)
                            + size.getY() / 2D;
                    break;
                case X_TIME:
                case Y_TIME:
                    x = MathUtil.map(
                            (1D / len) * i + time,
                            0, timeWindow,
                            minX, maxX);

                    double val = mode == Mode.X_TIME ? dataPoint.x[i] : dataPoint.y[i];
                    y = MathUtil.map(-val, 0, ssY, minY, maxY)
                            + size.getY() / 2D;
                    break;
                default:
                    x = y = 0;
                    break;
            }

            list.addLast(new Vector2D(x, y));
        }
        return list;
    }

    @Override
    public void render(Vector2D mouse) {
        updateDataPoints();
        boolean isInMainGui = Objects.equals(Minecraft.getCurrentGuiScreen(), "main_gui");
        if (!fullScreen || isInMainGui) {
            Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1,
                    selected ? selectedColor : Theme.lightBackground, Theme.lightEdge);
            long latestTime = dataPoints.size() == 0 ? 0 : dataPoints.getFirst().tickTime;
            if (latestTime - displayTime >= timeWindow) {
                double size = (double) timeWindow / (latestTime - startTime) * (getDisplayedSize().getX() - 2);
                double pos = MathUtil.map(displayTime, startTime, latestTime - timeWindow,
                        getDisplayedPos().getX() + 1,
                        getDisplayedPos().getX() + getDisplayedSize().getX() - size - 1);
                Renderer2D.drawRect(
                        new Vector2D(
                                pos, getDisplayedPos().getY() + getDisplayedSize().getY() - 1
                        ),
                        new Vector2D(size, 1),
                        Color.RED
                );
            }
        }

        if (renderPoints != null && renderPoints.size() > 1) {
            boolean shouldScissor = !fullScreen && mode != Mode.X_Y &&
                    !(isInMainGui && Keyboard.getPressedButtons().contains(InputConstants.KEY_LSHIFT));
            if (shouldScissor)
                Renderer2D.enableScissor(getDisplayedPos().getX() + 1, getDisplayedPos().getY() + 1,
                        getDisplayedSize().getX() - 2, getDisplayedSize().getY() - 2);

            Renderer2D.drawLines(
                    renderPoints,
                    Color.RED
            );

            if (shouldScissor)
                Renderer2D.disableScissor();
        }
    }

    protected void updateDataPoints() {
        if (lastTick == API.tickTime) return;

        Player p = Player.getLatest();
        Player bp = Player.getBeforeLatest();

        if (p == null || bp == null) return;

        if (bp.timingInput.isStopTick() && bp.deltaMouseX.length == 0 && (!p.timingInput.isStopTick() || p.deltaMouseX.length != 0)) {
            dataPoints.clear();
            renderPoints.clear();
            displayTime = API.tickTime - 1;
            startTime = displayTime;
            dataPoints.addFirst(new DataPoint(API.tickTime - 1));
            for (Vector2D v : dataToRenderPoint(dataPoints.getFirst()))
                renderPoints.addFirst(v);
        }

        int movementCount = p.deltaMouseX.length;
        if (movementCount == 0) return;

        int x = dataPoints.getFirst().x[dataPoints.getFirst().x.length - 1];
        int y = dataPoints.getFirst().y[dataPoints.getFirst().y.length - 1];

        DataPoint dataPoint = new DataPoint(
                new int[movementCount],
                new int[movementCount],
                API.tickTime);

        for (int i = 0; i < movementCount; i++) {
            dataPoint.x[i] = x += p.deltaMouseX[i];
            dataPoint.y[i] = y += p.deltaMouseY[i];
        }

        if (displayTime < API.tickTime - timeWindow) {
            displayTime = API.tickTime - timeWindow;
            if (mode != Mode.X_Y)
                reloadRenderPoints();
        }

        dataPoints.addFirst(dataPoint);
        for (Vector2D v : dataToRenderPoint(dataPoint))
            renderPoints.addFirst(v);

        lastTick = API.tickTime;
    }


    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new TextCheckButton(Vector2D.OFFSCREEN.copy(), "Fullscreen", fullScreen, this::setFullScreen)
        );
        Button b = new Button("Mode: " + mode.name());
        b.setButtonCallback(mouseButton -> {
            if (mouseButton != Mouse.Button.LEFT && mouseButton != Mouse.Button.RIGHT) return;
            mode = Mode.values()[(mode.ordinal() - (mouseButton.value * 2 - 1) + Mode.values().length) % Mode.values().length];
            reloadRenderPoints();
            b.setText("Mode: " + mode.name());
        });
        menu.addComponent(b);
        menu.addComponent(
                new NumberSlider(1, Player.maxSavedTicks, 1, timeWindow,
                        Vector2D.OFFSCREEN.copy(), new Vector2D(1, 12),
                        newValue -> {
                            timeWindow = (int) newValue;
                            reloadRenderPoints();
                        })
        );
        menu.addComponent(
                new Button("Delete", mouseButton -> {
                    if (Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.close();
                    }
                })
        );
        return menu;
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (!contains(mousePos)) return false;
        displayTime = (long) MathUtil.constrain(
                displayTime + Math.signum(delta),
                startTime,
                Math.max(startTime, dataPoints.getFirst().tickTime - timeWindow));
        reloadRenderPoints();
        return true;
    }

    public enum Mode {
        X_Y,
        X_TIME,
        Y_TIME;
    }

    protected static class DataPoint {
        public int[] x, y;
        public long tickTime;

        public DataPoint(long tickTime) {
            this(new int[]{0}, new int[]{0}, tickTime);
        }

        public DataPoint(int[] x, int[] y, long tickTime) {
            this.x = x;
            this.y = y;
            this.tickTime = tickTime;
        }
    }
}
