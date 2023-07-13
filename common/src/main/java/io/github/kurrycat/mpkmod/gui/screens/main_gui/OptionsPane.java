package io.github.kurrycat.mpkmod.gui.screens.main_gui;

import io.github.kurrycat.mpkmod.gui.components.Button;
import io.github.kurrycat.mpkmod.gui.components.*;
import io.github.kurrycat.mpkmod.util.Vector2D;
import io.github.kurrycat.mpkmod.util.WorldToFile;

import java.awt.*;

public class OptionsPane extends Pane<MainGuiScreen> {
    public OptionsPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(16, 16, 16, 70);
        addTitle("Options");
        initComponents();
    }

    private void initComponents() {
        OptionList optionList = new OptionList(
                new Vector2D(0, 0.05),
                new Vector2D(0.9, 0.8)
        );
        addChild(optionList, PERCENT.ALL, Anchor.CENTER);

        OptionItem pkcOption = new OptionItem(optionList);
        pkcOption.setHeight(20);
        TextRectangle radiusText = new TextRectangle(
                new Vector2D(0, 0),
                new Vector2D(50, 1),
                "Radius:",
                new Color(0, 0, 0, 0),
                Color.WHITE
        );
        pkcOption.addChild(radiusText, PERCENT.SIZE_Y);
        Div content = new Div(new Vector2D(0,0), new Vector2D(-2, -2));
        pkcOption.addChild(content, PERCENT.NONE, Anchor.CENTER);
        pkcOption.stretchXBetween(content, radiusText, null);
        NumberSlider pkcFileRadius = new NumberSlider(
                1, 20, 1, 5,
                new Vector2D(0, 0),
                new Vector2D(1 / 2D, 1),
                v -> {
                }
        );
        content.addChild(pkcFileRadius, PERCENT.ALL);
        content.addChild(
                new Button("Save as PKC File",
                        new Vector2D(1 / 2D, 0),
                        new Vector2D(1 / 2D, 1),
                        mouseButton -> WorldToFile.parseWorld((int) pkcFileRadius.getValue())
                ), PERCENT.ALL
        );

        optionList.addItem(pkcOption);


        /*TestList list = new TestList();
        list.setPos(new Vector2D(10, 0.1));
        list.setSize(new Vector2D(0.4, 0.8));
        for (int i = 0; i < 20; i++)
            list.addItem(new TestItem(list));
        addChild(list, PERCENT.POS_Y | PERCENT.SIZE, Anchor.TOP_RIGHT);*/
    }

    @Override
    public void render(Vector2D mousePos) {
        super.render(mousePos);
    }

    private static class OptionList extends ScrollableList<OptionItem> {
        public OptionList(Vector2D pos, Vector2D size) {
            super();
            setPos(pos);
            setSize(size);
        }
    }

    private static class OptionItem extends ScrollableListItem<OptionItem> {
        public OptionItem(ScrollableList<OptionItem> parent) {
            super(parent);

        }

        @Override
        public void render(int index, Vector2D pos, Vector2D size, Vector2D mouse) {
            renderDefaultBorder(pos, size);
            renderComponents(mouse);
        }
    }
}
