package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class NewLabelPane extends Pane {
    private Vector2D creationPos = Vector2D.ZERO;
    private final Button addLabelButton;
    private final Button addKeyButton;
    private final Button addMessageButton;

    public NewLabelPane(Vector2D pos, Vector2D size) {
        super(pos, size);
        this.backgroundColor = new Color(31, 31, 31, 50);
        this.addLabelButton = new Button("Add InfoLabel", Vector2D.OFFSCREEN, new Vector2D(getSize().getX() - 2, 11), mouseButton -> {
            InfoLabel infoLabel = new InfoLabel("Example Label", creationPos);
            //infoLabel.pos = creationPos.sub(infoLabel.getSize().div(2.0));
            this.parent.addComponent(infoLabel);
            this.close();
        });
        this.addKeyButton = new Button("Add KeyBindingLabel", Vector2D.OFFSCREEN, new Vector2D(getSize().getX() - 2, 11), mouseButton -> {
            KeyBindingLabel keyBindingLabel = new KeyBindingLabel(creationPos, new Vector2D(20, 20), "key.forward");
            //keyBindingLabel.pos = creationPos.sub(keyBindingLabel.getSize().div(2.0));
            this.parent.addComponent(keyBindingLabel);
            close();
        });
        this.addMessageButton = new Button("Add MessageQueue", Vector2D.OFFSCREEN, new Vector2D(getSize().getX() - 2, 11), mouseButton -> {
            MessageQueue messageQueue = new MessageQueue(creationPos, new Vector2D(30, 22), "Example MessageQueue");
            //messageQueue.pos = creationPos.sub(messageQueue.getSize().div(2.0));
            this.parent.addComponent(messageQueue);
            close();
        });
        this.components.add(addLabelButton);
        this.components.add(addKeyButton);
        this.components.add(addMessageButton);
    }

    @Override
    public void render(Vector2D mousePos) {
        this.addLabelButton.pos = new Vector2D(
                getDisplayPos().getX() + getSize().getX() / 2 - this.addLabelButton.getSize().getX() / 2,
                getDisplayPos().getY() + 11
        );
        this.addKeyButton.pos = new Vector2D(
                getDisplayPos().getX() + getSize().getX() / 2 - this.addKeyButton.getSize().getX() / 2,
                this.addLabelButton.getPos().getY() + this.addLabelButton.getSize().getY() + 1
        );
        this.addMessageButton.pos = new Vector2D(
                getDisplayPos().getX() + getSize().getX() / 2 - this.addMessageButton.getSize().getX() / 2,
                this.addKeyButton.getPos().getY() + this.addKeyButton.getSize().getY() + 1
        );
        super.render(mousePos);
    }

    public void setCreationPos(Vector2D creationPos) {
        this.creationPos = creationPos;
    }
}
