package ma.aybi.chroconi;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ma.aybi.chroconi.adapter.MessageAdapter;
import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.ContentCallBack;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.model.Conversation;
import ma.aybi.chroconi.model.Message;
import ma.aybi.chroconi.model.MessageContent;
import ma.aybi.chroconi.model.Sender;
import ma.aybi.chroconi.security.Encryptor;
import ma.aybi.chroconi.security.KeyUtils;
import ma.aybi.chroconi.security.MessageCrypto;

public class ChatActivity extends AppCompatActivity {

    LinearLayout inputContainer;
    TextView tvUsername;
    ImageView btnBack;
    EditText etMessage;
    ImageButton btnSend;
    RecyclerView messageRecycler;

    Conversation conversation;
    String currentUser;
    String repoOwner;
    String repoName;
    Sender myRole;
    MessageAdapter messageAdapter;
    List<Message> messages;
    Set<String> downloadedFiles = new HashSet<>();

    PublicKey otherPublicKey;
    PrivateKey myPrivateKey;

    public static String activeConversation = null;

    private static final long POLL_INTERVAL = 5_000;
    private static final long PRIORITY_POLL_INTERVAL = 2_000;
    private Handler pollHandler = new Handler();
    private Runnable pollRunnable;
    private boolean isPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        inputContainer = findViewById(R.id.inputContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (view, windowInsets) -> {
                    Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
                    Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                    inputContainer.animate()
                            .translationY(-ime.bottom)
                            .setDuration(180)
                            .start();
                    view.setPadding(0, systemBars.top, 0, systemBars.bottom);
                    return windowInsets;
        });

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        String username = getIntent().getStringExtra("username");

        tvUsername = findViewById(R.id.tvUsername);
        btnBack = findViewById(R.id.btnBack);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        messageRecycler = findViewById(R.id.messageRecycler);

        tvUsername.setText(username);

        currentUser = PreferencesManager.getLogin(this);

        for (Conversation c : Conversation.allConversations) {
            if (c.getName().equals(username)) {
                conversation = c;
                break;
            }
        }

        if (conversation == null) {
            Toast.makeText(this, "Conversation not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        parseGitURI(conversation.getGitURI());
        loadMyPrivateKey();
        fetchOtherPublicKey();

        activeConversation = conversation.getName();

        messages = conversation.getMessages();
        messageAdapter = new MessageAdapter(this, messages, myRole);
        messageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageRecycler.setAdapter(messageAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
        activeConversation = conversation != null ? conversation.getName() : null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPolling();
        activeConversation = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activeConversation = null;
    }

    private void loadMyPrivateKey() {
        String encPriv = conversation.getPrivateKeyEncrypted();
        if (encPriv != null) {
            myPrivateKey = KeyUtils.decryptStoredPrivateKey(encPriv);
        }
    }

    private void fetchOtherPublicKey() {
        String otherUser = conversation.getName();
        if (otherUser == null || otherUser.isEmpty()) return;
        GithubConnection.getFileContent(this, repoOwner, repoName,
                otherUser + "_public", new ContentCallBack() {
            @Override
            public void onResult(boolean success, String response) {
                if (!success || response == null) return;
                try {
                    org.json.JSONObject fileJson = new org.json.JSONObject(response);
                    String encoded = fileJson.getString("content").replace("\n", "").replace("\r", "");
                    byte[] decoded = Base64.decode(encoded, Base64.NO_WRAP);
                    otherPublicKey = KeyUtils.stringToPublicKey(new String(decoded));
                } catch (Exception e) {
                }
            }
        });
    }

    private long getPollInterval() {
        String priority = PreferencesManager.getPriorityConversation(this);
        if (priority != null && priority.equals(conversation.getName())) {
            return PRIORITY_POLL_INTERVAL;
        }
        return POLL_INTERVAL;
    }

    private void startPolling() {
        if (isPolling) return;
        isPolling = true;
        final long interval = getPollInterval();
        fetchMessages();
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPolling) return;
                fetchMessages();
                pollHandler.postDelayed(this, getPollInterval());
            }
        };
        pollHandler.postDelayed(pollRunnable, interval);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
            pollRunnable = null;
        }
    }

    private void fetchMessages() {
        GithubConnection.getRepoContents(this, repoOwner, repoName, new ContentCallBack() {
            @Override
            public void onResult(boolean success, String response) {
                if (!success || response == null) return;
                try {
                    org.json.JSONArray files = new org.json.JSONArray(response);
                    for (int i = 0; i < files.length(); i++) {
                        final String fileName = files.getJSONObject(i).getString("name");
                        if (!fileName.contains("_TEXTUAL_")) continue;
                        if (downloadedFiles.contains(fileName)) continue;
                        downloadedFiles.add(fileName);

                        GithubConnection.getFileContent(ChatActivity.this, repoOwner, repoName,
                                fileName, new ContentCallBack() {
                            @Override
                            public void onResult(boolean ok, String fileResponse) {
                                if (!ok || fileResponse == null) return;
                                try {
                                    org.json.JSONObject fileJson = new org.json.JSONObject(fileResponse);
                                    String encoded = fileJson.getString("content").replace("\n", "").replace("\r", "");
                                    byte[] encrypted = Base64.decode(encoded, Base64.NO_WRAP);
                                    byte[] decrypted = null;
                                    if (myPrivateKey != null) {
                                        decrypted = MessageCrypto.decrypt(encrypted, myPrivateKey);
                                    }
                                    if (decrypted == null) {
                                        decrypted = Encryptor.decrypt(encrypted);
                                    }
                                    if (decrypted == null) return;
                                    Message msg = Message.deserialize(decrypted);
                                    if (msg == null) return;
                                    if (isAlreadyLoaded(msg)) return;
                                    runOnUiThread(() -> {
                                        messages.add(msg);
                                        java.util.Collections.sort(messages,
                                                (a, b) -> a.getSentAt().compareTo(b.getSentAt()));
                                        messageAdapter.notifyDataSetChanged();
                                        messageRecycler.smoothScrollToPosition(messages.size() - 1);
                                        String text = new String(msg.getContent().getContent());
                                        conversation.setLastMessage(text);
                                        conversation.setTime(new SimpleDateFormat("HH:mm", Locale.US)
                                                .format(msg.getSentAt()));
                                        Conversation.saveAll(getApplicationContext());
                                    });
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private boolean isAlreadyLoaded(Message msg) {
        for (Message m : messages) {
            if (m.getId().equals(msg.getId())) return true;
        }
        return false;
    }

    private void parseGitURI(String gitURI) {
        String withoutExt = gitURI.replace(".git", "");
        String[] parts = withoutExt.split("/");
        repoOwner = parts[0];
        repoName = parts[1];

        myRole = currentUser != null && currentUser.equals(repoOwner)
                ? Sender.HOSTER : Sender.CLIENT;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");

        Message msg = Message.createMessage(myRole, new MessageContent(text));
        messages.add(msg);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        messageRecycler.smoothScrollToPosition(messages.size() - 1);
        conversation.setLastMessage(text);
        conversation.setTime(new SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
        Conversation.saveAll(getApplicationContext());

        String username = currentUser != null ? currentUser : "unknown";
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String fileName = username + "_TEXTUAL_" + timestamp;

        byte[] serialized = msg.serialize();
        if (serialized == null) return;
        byte[] encrypted = null;
        if (otherPublicKey != null) {
            encrypted = MessageCrypto.encrypt(serialized, otherPublicKey);
        }
        if (encrypted == null) {
            encrypted = Encryptor.encrypt(serialized);
        }
        if (encrypted == null) return;
        String fileContent = Base64.encodeToString(encrypted, Base64.NO_WRAP);

        GithubConnection.pushFileToRepo(this, repoOwner, repoName, fileName,
                fileContent, success -> {
            if (!success) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Message queued", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
