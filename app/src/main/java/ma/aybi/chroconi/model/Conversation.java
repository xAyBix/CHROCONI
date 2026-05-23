package ma.aybi.chroconi.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private List<Message> allMessages = new ArrayList<>();

    private String name;
    private String lastMessage;
    private String time;

    public Conversation(String name, String lastMessage, String time) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
    }

    public List<Message> getAllMessages () {
        return allMessages;
    }

    public void seekMessages () {
        // fill messages list
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getTime() {
        return time;
    }
}
