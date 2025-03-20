package org.shared;

import java.io.Serializable;

public class PacketMessage implements Serializable {
    private final byte[] content;
    private final MessageType type;

    public PacketMessage(byte[] content, MessageType type) {
        this.content = content;
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }
}