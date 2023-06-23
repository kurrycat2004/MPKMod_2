package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Plot extends ResizableComponent {
    private static final double AXIS_MARGIN = 12;

    public Color edgeColor = new Color(100, 100, 100, 50);
    public Color selectedColor = new Color(255, 170, 0, 100);

    public Color backgroundColor = new Color(100, 100, 100, 40);
    protected int startX = -2;
    protected int endX = 3;

    protected int dataSize;

    protected double minY = 0;
    protected double maxY = 90;
    protected double stepY = 10;

    protected List<Vector2D> dataPoints = null;
    protected List<Vector2D> renderPoints = null;

    protected Supplier<List<List<Double>>> dataSupplier = null;

    public Plot() {
        this.dataSize = endX - startX + 1;
        this.setMinSize(new Vector2D(40, 40));
    }

    public Plot setDataSupplier(Supplier<List<List<Double>>> dataSupplier) {
        this.dataSupplier = dataSupplier;
        return this;
    }

    @Override
    public void updatePosAndSize() {
        super.updatePosAndSize();
        updateRenderPoints();
    }

    public void updateRenderPoints() {
        if (dataPoints == null) return;
        Vector2D min = getDisplayedPos().add(AXIS_MARGIN, 2);
        Vector2D max = getDisplayedPos().add(getDisplayedSize()).sub(2, AXIS_MARGIN);
        this.renderPoints = dataPoints.stream().map(p ->
                new Vector2D(
                        MathUtil.map(p.getX(), startX, endX, min.getX(), max.getX()),
                        MathUtil.map(p.getY(), minY, maxY, max.getY(), min.getY())
                )
        ).collect(Collectors.toList());
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, selected ? selectedColor : backgroundColor, edgeColor);

        if (dataSupplier != null) {
            List<List<Double>> data = dataSupplier.get();
            if (data != null) setData(data);
        }

        if (renderPoints != null && renderPoints.size() > 1) {
            Renderer2D.drawLines(
                    renderPoints,
                    Color.RED
            );
        }
        drawXAxis();
        drawYAxis();
    }

    public void setData(List<List<Double>> dataPoints) {
        this.dataPoints = new ArrayList<>();
        for (int i = 0; i < dataPoints.size(); i++) {
            double offset = 1D / dataPoints.get(i).size();
            for (int d = 0; d < dataPoints.get(i).size(); d++) {
                this.dataPoints.add(
                        new Vector2D(
                                i + startX + offset * d,
                                dataPoints.get(i).get(d))
                );
            }
        }
        updateRenderPoints();
    }

    private void drawXAxis() {
        double yPos = getDisplayedPos().getY() + getDisplayedSize().getY() - AXIS_MARGIN;
        Renderer2D.drawLine(
                new Vector2D(
                        getDisplayedPos().getX() + AXIS_MARGIN,
                        yPos),
                new Vector2D(
                        getDisplayedPos().getX() + getDisplayedSize().getX() - 2,
                        yPos
                ),
                Color.WHITE
        );
        if (endX > startX)
            for (int i = startX; i <= endX; i++) {
                double xPos = MathUtil.map(i,
                        startX, endX,
                        getDisplayedPos().getX() + AXIS_MARGIN,
                        getDisplayedPos().getX() + getDisplayedSize().getX() - 2);
                Renderer2D.drawLine(
                        new Vector2D(xPos, yPos - 2),
                        new Vector2D(xPos, yPos + 2),
                        Color.WHITE
                );
                FontRenderer.drawCenteredString(
                        String.valueOf(i),
                        new Vector2D(xPos, yPos + AXIS_MARGIN / 2 + 1),
                        Color.WHITE,
                        false
                );
            }
    }

    private void drawYAxis() {
        double xPos = getDisplayedPos().getX() + AXIS_MARGIN;
        Renderer2D.drawLine(
                new Vector2D(
                        xPos,
                        getDisplayedPos().getY() + 2),
                new Vector2D(
                        xPos,
                        getDisplayedPos().getY() + getDisplayedSize().getY() - AXIS_MARGIN
                ),
                Color.WHITE
        );
        if (maxY > minY)
            for (double i = minY; i <= maxY; i += stepY) {
                double yPos = MathUtil.map(i,
                        minY, maxY,
                        getDisplayedPos().getY() + getDisplayedSize().getY() - AXIS_MARGIN,
                        getDisplayedPos().getY() + 2);
                Renderer2D.drawLine(
                        new Vector2D(xPos - 2, yPos),
                        new Vector2D(xPos + 2, yPos),
                        Color.WHITE
                );
                FontRenderer.drawCenteredString(
                        String.valueOf(i),
                        new Vector2D(xPos - AXIS_MARGIN / 2 - 1, yPos),
                        Color.WHITE,
                        false
                );
            }
    }
}
