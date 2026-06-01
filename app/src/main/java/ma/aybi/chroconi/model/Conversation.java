package ma.aybi.chroconi.model;

import java.util.ArrayList;
import java.util.List;

public class Conversation {
    public static List<Conversation> allConversations = new ArrayList<>();
    private List<Message> messages;

    private String gitURI;
    private String name;
    private String lastMessage;
    private String time;

    public Conversation(String gitURI, String name, String lastMessage, String time) {
        this.gitURI = gitURI;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        allConversations.add(this);
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

    public String getGitURI() {
        return gitURI;
    }

    public void setGitURI(String gitURI) {
        this.gitURI = gitURI;
    }
}
