package ma.aybi.chroconi.model;

import android.content.Context;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kotlin.text.Charsets;
import ma.aybi.chroconi.security.Encryptor;
import ma.aybi.chroconi.util.FileStorageSystem;

public class Conversation {
    public static List<Conversation> allConversations = new ArrayList<>();

    private List<Message> messages;

    private String gitURI;
    private String name;
    private String lastMessage;
    private String time;
    private String publicKey;
    private String privateKeyEncrypted;

    public Conversation(String gitURI, String name, String lastMessage, String time) {
        this(gitURI, name, lastMessage, time, null, null);
    }

    public Conversation(String gitURI, String name, String lastMessage, String time,
                        String publicKey, String privateKeyEncrypted) {
        this.gitURI = gitURI;
        this.name = name;
        this.lastMessage = lastMessage;
        this.time = time;
        this.publicKey = publicKey;
        this.privateKeyEncrypted = privateKeyEncrypted;
        allConversations.add(this);
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

    public List<Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void addMessage(Message msg) {
        getMessages().add(msg);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKeyEncrypted() {
        return privateKeyEncrypted;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("gitURI", gitURI != null ? gitURI : "");
            json.put("name", name != null ? name : "");
            json.put("lastMessage", lastMessage != null ? lastMessage : "");
            json.put("time", time != null ? time : "");
            json.put("publicKey", publicKey != null ? publicKey : "");
            json.put("privateKeyEncrypted", privateKeyEncrypted != null ? privateKeyEncrypted : "");
            if (messages != null && !messages.isEmpty()) {
                JSONArray msgArray = new JSONArray();
                for (Message msg : messages) {
                    byte[] serialized = msg.serialize();
                    if (serialized != null) {
                        msgArray.put(Base64.encodeToString(serialized, Base64.NO_WRAP));
                    }
                }
                json.put("messages", msgArray);
            }
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public static Conversation fromJson(JSONObject json) {
        try {
            String gitURI = json.optString("gitURI", "");
            String name = json.optString("name", "");
            String lastMessage = json.optString("lastMessage", null);
            String time = json.optString("time", null);
            String publicKey = json.optString("publicKey", null);
            String privateKeyEncrypted = json.optString("privateKeyEncrypted", null);
            if (lastMessage != null && lastMessage.isEmpty()) lastMessage = null;
            if (time != null && time.isEmpty()) time = null;
            if (publicKey != null && publicKey.isEmpty()) publicKey = null;
            if (privateKeyEncrypted != null && privateKeyEncrypted.isEmpty()) privateKeyEncrypted = null;
            Conversation c = new Conversation(gitURI, name, lastMessage, time, publicKey, privateKeyEncrypted);
            JSONArray msgArray = json.optJSONArray("messages");
            if (msgArray != null) {
                for (int i = 0; i < msgArray.length(); i++) {
                    byte[] decoded = Base64.decode(msgArray.getString(i), Base64.NO_WRAP);
                    Message msg = Message.deserialize(decoded);
                    if (msg != null) c.addMessage(msg);
                }
            }
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveAll(Context context) {
        try {
            JSONArray array = new JSONArray();
            for (Conversation c : allConversations) {
                JSONObject json = c.toJson();
                if (json != null) array.put(json);
            }
            String jsonStr = array.toString();
            byte[] encrypted = Encryptor.encrypt(jsonStr.getBytes(Charsets.ISO_8859_1));
            if (encrypted != null) {
                FileStorageSystem.saveEncrypted(context, encrypted);
            }
        } catch (Exception e) {
        }
    }

    public static void loadAll(Context context) {
        try {
            byte[] encrypted = FileStorageSystem.loadEncrypted(context);
            if (encrypted == null) return;
            byte[] decrypted = Encryptor.decrypt(encrypted);
            if (decrypted == null) return;
            String jsonStr = new String(decrypted, Charsets.ISO_8859_1);
            JSONArray array = new JSONArray(jsonStr);
            allConversations.clear();
            for (int i = 0; i < array.length(); i++) {
                fromJson(array.getJSONObject(i));
            }
        } catch (Exception e) {
        }
    }
}
