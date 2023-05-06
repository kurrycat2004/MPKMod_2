package io.github.kurrycat.mpkmod.gui.components;

public interface MessageReceiver {
    void postMessage(String receiverID, String content, boolean highlighted);
}
