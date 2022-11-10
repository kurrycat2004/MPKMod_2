package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ColorUtil;
import io.github.kurrycat.mpkmod.util.MathUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MessageQueue extends ResizableComponent {
    @JsonProperty("displayName")
    private final String name;
    @JsonProperty
    private final String messageReceiverID = "offset";
    @JsonProperty
    public Color backgroundColor = new Color(100, 100, 100, 40);
    @JsonProperty
    public Color messageBackgroundColor = new Color(31, 31, 31, 47);
    @JsonProperty
    public Color messageColor = new Color(255, 255, 255, 255);
    @JsonProperty
    public Color edgeColor = new Color(100, 100, 100, 50);
    public Color selectedColor = new Color(255, 170, 0, 100);
    private ArrayList<Message> messages = new ArrayList<>();


    @JsonCreator
    public MessageQueue(@JsonProperty("pos") Vector2D pos, @JsonProperty("size") Vector2D size, @JsonProperty("name") String name) {
        super(pos, size);
        this.name = name;
        this.setMinSize(new Vector2D(40, 22));
    }

    public static MessageQueue getReceiverFor(String receiverID, ArrayList<MessageQueue> queuesToSearch) {
        return queuesToSearch.stream().filter(messageQueue -> messageQueue.messageReceiverID.equals(receiverID)).findFirst().orElse(null);
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayedPos(), getDisplayedSize(), 1, selected ? selectedColor : backgroundColor, edgeColor);
        //Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);
        if (highlighted) Renderer2D.drawDottedRect(getDisplayedPos(), getDisplayedSize(), 1, 1, 1, Color.BLACK);

        messages = messages.stream().filter(Message::isAlive).collect(Collectors.toCollection(ArrayList::new));

        double lineHeight = 10;
        for (int i = 0; i < messages.size() && (i + 1) * lineHeight <= getDisplayedSize().getY() - 2; i++) {
            messages.get(i).render(
                    new Vector2D(getDisplayedPos().getX() + 1, getDisplayedPos().getY() + getDisplayedSize().getY() - (i + 1) * lineHeight - 1),
                    new Vector2D(getDisplayedSize().getX() - 2, lineHeight)
            );
        }
        renderHoverEdges(mouse);
    }

    public void postMessage(String content) {
        messages.add(0, new Message(this, content));
    }

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    @Override
    public PopupMenu getPopupMenu() {
        PopupMenu menu = new PopupMenu();
        menu.addComponent(
                new Button("Delete", Vector2D.OFFSCREEN, new Vector2D(30, 11), mouseButton -> {
                    if(Mouse.Button.LEFT.equals(mouseButton)) {
                        menu.paneHolder.removeComponent(this);
                        menu.paneHolder.closePane(menu);
                    }
                })
        );
        return menu;
    }

    public static class Message {
        public String content;
        public Instant created;
        public MessageQueue parent;

        /**
         * max age in ms
         */
        public int maxAge = 10_000;
        /**
         * how many ms before maxAge it should start fading out
         */
        public int fadeOutTime = 1_000;

        public Message(MessageQueue parent, String content) {
            this.parent = parent;
            this.content = content;
            this.created = Instant.now();
        }

        public void render(Vector2D pos, Vector2D size) {
            long age = getAge();
            double fadeOutAlpha = age < maxAge - fadeOutTime ? 255 : MathUtil.map(maxAge - Math.min(age, maxAge), fadeOutTime, 0, 255, 0);

            Renderer2D.drawRect(pos, size, ColorUtil.withAlpha(parent.messageBackgroundColor, Math.min(fadeOutAlpha, parent.messageBackgroundColor.getAlpha())));
            //FontRenderer.drawCenteredString(this.content, pos.add(size.div(2)), new Color(255, 255, 255, 4), false);
            FontRenderer.drawCenteredString(this.content, pos.add(size.div(2)).add(0, 1), ColorUtil.withAlpha(parent.messageColor, fadeOutAlpha), false);
            //System.out.println(ColorUtil.fadeColor(parent.messageColor, fadeOutAlpha).getAlpha());
        }

        public boolean isAlive() {
            return getAge() <= maxAge;
        }

        public long getAge() {
            return ChronoUnit.MILLIS.between(created, Instant.now());
        }
    }
}
