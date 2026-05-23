package ma.aybi.chroconi.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static List<Message> allMessages = new ArrayList<>();
    private Long id;
    private Sender sender;
    private MessageContent content;
    private Date sentAt;
    public Message (Long id, Sender sender, MessageContent content, Date sentAt) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
        allMessages.add(this);
    }

    public byte[] serialize () {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(this);
        }catch (Exception e) {
            return null;
        }
        return baos.toByteArray();
    }

    public static Message deserialize (byte[] serializedMessage) {
        ByteArrayInputStream bais = new ByteArrayInputStream(serializedMessage);
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Message) ois.readObject();
        }catch (Exception e) {
            return null;
        }
    }

    public static Message createMessage(Sender sender, MessageContent content){
        Message msg = new Message(
                System.currentTimeMillis(),
                sender,
                content,
                new Date()
        );
        return msg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public MessageContent getContent() {
        return content;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public static List<Message> getAllMessages() {
        return allMessages;
    }

    public static void setAllMessages(List<Message> allMessages) {
        Message.allMessages = allMessages;
    }
}
