package ma.aybi.chroconi.model;

import java.io.Serializable;

public class MessageContent implements Serializable {
    private static final long serialVersionUID = 1L;
    private MessageType type;
    private byte[] content;
    public MessageContent (String content) {
        this.type = MessageType.TEXTUAL;
        this.content = content.getBytes();
    }

    public MessageContent (MessageType type, byte[] content) {
        this.type = type;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
