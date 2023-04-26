package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class NewLabelPane extends Pane<MainGuiScreen> {
    private Vector2D creationPos = Vector2D.ZERO;
    private final Button addLabelButton;
    private final Button addKeyButton;
    private final Button addMessageButton;
    private final Button addBarrierDisplayButton;

    public NewLabelPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(31, 31, 31, 50);
        this.addLabelButton = new Button("Add InfoLabel", Vector2D.OFFSCREEN, new Vector2D(getDisplayedSize().getX() - 2, 11), mouseButton -> {
            InfoLabel infoLabel = new InfoLabel("Example Label", creationPos);
            //infoLabel.pos = creationPos.sub(infoLabel.getDisplayedSize().div(2.0));
            this.paneHolder.addComponent(infoLabel);
            this.close();
        });
        this.addKeyButton = new Button("Add KeyBindingLabel", Vector2D.OFFSCREEN, new Vector2D(getDisplayedSize().getX() - 2, 11), mouseButton -> {
            KeyBindingLabel keyBindingLabel = new KeyBindingLabel(creationPos, new Vector2D(20, 20), "key.forward");
            //keyBindingLabel.pos = creationPos.sub(keyBindingLabel.getDisplayedSize().div(2.0));
            this.paneHolder.addComponent(keyBindingLabel);
            close();
        });
        this.addMessageButton = new Button("Add MessageQueue", Vector2D.OFFSCREEN, new Vector2D(getDisplayedSize().getX() - 2, 11), mouseButton -> {
            MessageQueue messageQueue = new MessageQueue(creationPos, new Vector2D(30, 22), "Example MessageQueue");
            //messageQueue.pos = creationPos.sub(messageQueue.getDisplayedSize().div(2.0));
            this.paneHolder.addComponent(messageQueue);
            close();
        });
        this.addBarrierDisplayButton = new Button("Add BarrierDisplay", Vector2D.OFFSCREEN, new Vector2D(getDisplayedSize().getX() - 2, 11), mouseButton -> {
            BarrierDisplayComponent barrierDisplay = new BarrierDisplayComponent(new Vector2D(-35, -35), new Vector2D(30, 30));
            this.paneHolder.addComponent(barrierDisplay);
            close();
        });
        this.components.add(addLabelButton);
        this.components.add(addKeyButton);
        this.components.add(addMessageButton);
        this.components.add(addBarrierDisplayButton);
    }

    @Override
    public void render(Vector2D mousePos) {
        this.addLabelButton.pos = new Vector2D(
                getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.addLabelButton.getDisplayedSize().getX() / 2,
                getDisplayedPos().getY() + 11
        );
        this.addKeyButton.pos = new Vector2D(
                getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.addKeyButton.getDisplayedSize().getX() / 2,
                this.addLabelButton.getPos().getY() + this.addLabelButton.getDisplayedSize().getY() + 1
        );
        this.addMessageButton.pos = new Vector2D(
                getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.addMessageButton.getDisplayedSize().getX() / 2,
                this.addKeyButton.getPos().getY() + this.addKeyButton.getDisplayedSize().getY() + 1
        );
        this.addBarrierDisplayButton.pos = new Vector2D(
                getDisplayedPos().getX() + getDisplayedSize().getX() / 2 - this.addBarrierDisplayButton.getDisplayedSize().getX() / 2,
                this.addMessageButton.getPos().getY() + this.addMessageButton.getDisplayedSize().getY() + 1
        );
        super.render(mousePos);
    }

    public void setCreationPos(Vector2D creationPos) {
        this.creationPos = creationPos;
    }
}
