package ma.aybi.chroconi.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    private static List<Conversation> allConversations = new ArrayList<>();
    private List<Message> messages;

    private String name;
    private String lastMessage;
    private String time;

    public Conversation(String name, String lastMessage, String time) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
    }

    public static List<Conversation> getAllConversations () {
        return allConversations;
    }

    public static void addConversation (Conversation conversation) {
        allConversations.add(conversation);
    }

    public List<Message> getMessages () {
        return messages;
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
