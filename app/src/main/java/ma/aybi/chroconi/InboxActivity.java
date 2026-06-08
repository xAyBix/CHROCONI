package ma.aybi.chroconi;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ma.aybi.chroconi.adapter.ConversationAdapter;
import ma.aybi.chroconi.adapter.InvitationAdapter;
import ma.aybi.chroconi.adapter.PriorityAdapter;
import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.BooleanCallBack;
import ma.aybi.chroconi.github.ContentCallBack;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.model.Conversation;
import ma.aybi.chroconi.model.Invitation;
import ma.aybi.chroconi.model.Message;
import ma.aybi.chroconi.model.MessageContent;
import ma.aybi.chroconi.model.MessageType;
import ma.aybi.chroconi.model.Sender;
import ma.aybi.chroconi.security.Encryptor;
import ma.aybi.chroconi.security.KeyUtils;
import ma.aybi.chroconi.security.MessageCrypto;

public class InboxActivity extends AppCompatActivity {
    RecyclerView chatRecycler, invitationsRecycler;
    View toastLayout;
    TextView toastText;
    LinearLayout btnNew;
    LinearLayout btnNotifications;
    LinearLayout btnPriorities;

    View vNotificationsDot;

    EditText etHostname;
    EditText etInvitedname;

    ConversationAdapter conversationAdapter;
    InvitationAdapter invitationAdapter;
    Dialog notificationsDialog;

    private static final long POLL_INTERVAL = 10_000;
    private static final String CHANNEL_ID = "chroconi_messages";
    private static final int NOTIFICATION_ID = 1001;
    private Handler pollHandler = new Handler();
    private Runnable pollRunnable;
    private boolean isPolling = false;
    private Set<String> notifiedFiles = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inbox);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(
                android.graphics.Color.TRANSPARENT);

        vNotificationsDot = findViewById(R.id.vNotificationsDot);
        vNotificationsDot.setVisibility(View.INVISIBLE);
        InvitationAdapter.vNotificationsDot = vNotificationsDot;

        chatRecycler = findViewById(R.id.chatRecycler);
        btnNew = findViewById(R.id.btnNew);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnPriorities = findViewById(R.id.btnPriorities);

        btnNew.setOnClickListener(v -> {
            showNewChatDialog();
        });
        btnNotifications.setOnClickListener(v -> {
            showNotificationsDialog();
        });
        btnPriorities.setOnClickListener(v -> {
            showPrioritiesDialog();
        });

        chatRecycler.setHasFixedSize(true);
        chatRecycler.setItemAnimator(null);
        conversationAdapter = new ConversationAdapter(this);

        chatRecycler.setLayoutManager(
                new LinearLayoutManager(this));


        chatRecycler.setAdapter(conversationAdapter);

        Conversation.loadAll(this);
        conversationAdapter.notifyDataSetChanged();

        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1002);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
        Conversation.loadAll(this);
        conversationAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPolling();
    }

    private void startPolling() {
        if (isPolling) return;
        isPolling = true;
        fetchInvitations();
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPolling) return;
                fetchInvitations();
                checkPriorityConversation();
                pollHandler.postDelayed(this, POLL_INTERVAL);
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
            pollRunnable = null;
        }
    }

    private void fetchInvitations() {
        GithubConnection.getCollabInvitations(this, success -> {
            updateNotificationDot();
            if (notificationsDialog != null && notificationsDialog.isShowing()
                    && invitationAdapter != null) {
                invitationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateNotificationDot() {
        if (!Invitation.invitations.isEmpty()) {
            vNotificationsDot.setVisibility(View.VISIBLE);
        } else {
            vNotificationsDot.setVisibility(View.INVISIBLE);
        }
    }

    private void showNotificationsDialog() {

        notificationsDialog = new Dialog(this);

        notificationsDialog.setContentView(
                R.layout.dialog_notifications);

        notificationsDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        notificationsDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        notificationsDialog.getWindow().getAttributes().windowAnimations =
                R.style.DialogAnimation;


        invitationsRecycler = notificationsDialog.findViewById(R.id.invitationsRecycler);

        invitationsRecycler.setHasFixedSize(true);
        invitationsRecycler.setItemAnimator(null);
        invitationAdapter = new InvitationAdapter(this);
        invitationAdapter.setOnInvitationAcceptedListener((repoFullName, inviter) -> {
            java.security.KeyPair keys = KeyUtils.generateKeyPair();
            String publicKeyStr = (keys != null) ? KeyUtils.publicKeyToString(keys.getPublic()) : null;
            String privateKeyEncStr = null;
            if (keys != null) {
                byte[] encryptedPriv = Encryptor.encrypt(keys.getPrivate().getEncoded());
                if (encryptedPriv != null) {
                    privateKeyEncStr = Encryptor.byteToString(encryptedPriv);
                }
            }

            new Conversation(repoFullName + ".git", inviter, null, null, publicKeyStr, privateKeyEncStr);
            Conversation.saveAll(getApplicationContext());
            conversationAdapter.notifyDataSetChanged();

            if (publicKeyStr != null) {
                String[] parts = repoFullName.split("/");
                String owner = parts[0];
                String repoName = parts[1];
                String currentUser = PreferencesManager.getLogin(getApplicationContext());
                if (currentUser != null) {
                    GithubConnection.pushFileToRepo(getApplicationContext(), owner, repoName,
                            currentUser + "_public", publicKeyStr, pushed -> {});
                }
            }
        });

        invitationsRecycler.setLayoutManager(
                new LinearLayoutManager(this));
        invitationsRecycler.setAdapter(invitationAdapter);

        notificationsDialog.show();

    }

    private void showNewChatDialog() {

        Dialog dialog = new Dialog(this);

        dialog.setContentView(
                R.layout.dialog_new_chat);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.getWindow().getAttributes().windowAnimations =
                R.style.DialogAnimation;

        dialog.show();

        // INPUTS
        etHostname = dialog.findViewById(R.id.etHostname);
        etInvitedname = dialog.findViewById(R.id.etInvitedname);

        Button btnCreate = dialog.findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {

            String hostname = etHostname.getText().toString().trim();
            String invitedname = etInvitedname.getText().toString().trim();

            if (hostname.isEmpty()) {
                etHostname.setError("Please enter your github username");
                return;
            }else if (hostname.length()>25) {
                etHostname.setError("Username length must be less than 26 characters");
                return;
            }

            if (invitedname.isEmpty()) {
                etInvitedname.setError("Please enter your friend's github username");
                return;
            }else if (invitedname.length()>25) {
                etInvitedname.setError("Username length must be less than 26 characters");
                return;
            }

            createConversation(hostname, invitedname, success -> {
                if (success) {
                    dialog.dismiss();
                }
            });

        });
    }
    private void showPrioritiesDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_priorities);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        RecyclerView prioritiesRecycler = dialog.findViewById(R.id.prioritiesRecycler);
        PriorityAdapter priorityAdapter = new PriorityAdapter(this);
        priorityAdapter.setOnPriorityChanged(() -> {
            conversationAdapter.notifyDataSetChanged();
        });
        prioritiesRecycler.setHasFixedSize(true);
        prioritiesRecycler.setItemAnimator(null);
        prioritiesRecycler.setLayoutManager(new LinearLayoutManager(this));
        prioritiesRecycler.setAdapter(priorityAdapter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new messages");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Conversation findConvByName(String name) {
        for (Conversation c : Conversation.allConversations) {
            if (c.getName() != null && c.getName().equals(name)) return c;
        }
        return null;
    }

    private void checkPriorityConversation() {
        String priorityName = PreferencesManager.getPriorityConversation(this);
        if (priorityName == null || priorityName.isEmpty()) return;

        final Conversation conv = findConvByName(priorityName);
        if (conv == null) return;

        String gitURI = conv.getGitURI();
        if (gitURI == null) return;
        String repoPath = gitURI.replace(".git", "");
        String[] parts = repoPath.split("/");
        if (parts.length < 2) return;
        String owner = parts[0];
        String repoName = parts[1];

        GithubConnection.getRepoContents(this, owner, repoName, (success, response) -> {
            if (!success || response == null) return;
            try {
                org.json.JSONArray arr = new org.json.JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject item = arr.getJSONObject(i);
                    String fileName = item.optString("name", null);
                    if (fileName != null && !fileName.endsWith("_public") && !notifiedFiles.contains(fileName)) {
                        notifiedFiles.add(fileName);
                        processNewMessageFile(owner, repoName, fileName, conv);
                    }
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void processNewMessageFile(String owner, String repoName, String fileName, Conversation conv) {
        GithubConnection.getFileContent(this, owner, repoName, fileName, new ContentCallBack() {
            @Override
            public void onResult(boolean success, String response) {
                if (!success || response == null) return;
                try {
                    String currentUser = PreferencesManager.getLogin(getApplicationContext());
                    if (currentUser != null && fileName.startsWith(currentUser + "_TEXTUAL_")) {
                        return;
                    }

                    org.json.JSONObject json = new org.json.JSONObject(response);
                    String base64Content = json.optString("content", "").replace("\n", "").replace("\r", "");

                    byte[] encryptedBytes = Base64.decode(base64Content, Base64.NO_WRAP);
                    byte[] decryptedBytes = null;
                    String myPrivateKeyStr = conv.getPrivateKeyEncrypted();
                    if (myPrivateKeyStr != null) {
                        try {
                            java.security.PrivateKey privateKey = KeyUtils.decryptStoredPrivateKey(myPrivateKeyStr);
                            if (privateKey != null) {
                                decryptedBytes = MessageCrypto.decrypt(encryptedBytes, privateKey);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (decryptedBytes == null) {
                        decryptedBytes = Encryptor.decrypt(encryptedBytes);
                    }
                    if (decryptedBytes == null) return;

                    Message msg = Message.deserialize(decryptedBytes);
                    conv.addMessage(msg);
                    if (msg.getContent() != null) {
                        conv.setLastMessage(new String(msg.getContent().getContent()));
                    }
                    conv.setTime(new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(msg.getSentAt()));
                    Conversation.saveAll(getApplicationContext());

                    if (ChatActivity.activeConversation != null) {
                        showNewMessageNotification(conv.getName(), msg.getContent(), msg.getSender());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showNewMessageNotification(String convName, MessageContent content, Sender sender) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("conversationName", convName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String displayText = (content.getType() == MessageType.TEXTUAL)
                ? new String(content.getContent()) : "[Attachment]";
        String displaySender = (sender == Sender.HOSTER)
                ? PreferencesManager.getLogin(this) : convName;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(displaySender)
                .setContentText(displayText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createConversation (String hostname, String invitedname, BooleanCallBack callBack) {
        LayoutInflater inflater = getLayoutInflater();

        Toast toast = new Toast(this);

        GithubConnection.createRepository(getApplicationContext(), hostname, invitedname, (success, resp) -> {
            if (success) {
                GithubConnection.setCollab(getApplicationContext(), resp, hostname, invitedname, (s, r) -> {
                    toastLayout = inflater.inflate(
                            R.layout.success_toast,
                            findViewById(android.R.id.content),
                            false);
                    toastText = toastLayout.findViewById(R.id.tvToastSuccess);

                    toastText.setText("Repository created.");

                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(toastLayout);
                    toast.show();
                    callBack.onResult(true);

                    java.security.KeyPair keys = KeyUtils.generateKeyPair();
                    String publicKeyStr = (keys != null) ? KeyUtils.publicKeyToString(keys.getPublic()) : null;
                    String privateKeyEncStr = null;
                    if (keys != null) {
                        byte[] encryptedPriv = Encryptor.encrypt(keys.getPrivate().getEncoded());
                        if (encryptedPriv != null) {
                            privateKeyEncStr = Encryptor.byteToString(encryptedPriv);
                        }
                    }

                    String gitURI = hostname + "/" + r + ".git";
                    new Conversation(gitURI, invitedname, null, null, publicKeyStr, privateKeyEncStr);
                    Conversation.saveAll(getApplicationContext());
                    conversationAdapter.notifyDataSetChanged();

                    if (publicKeyStr != null) {
                        GithubConnection.pushFileToRepo(getApplicationContext(), hostname, r,
                                hostname + "_public", publicKeyStr, pushed -> {});
                    }

                    if (s) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                toastText.setText("Invitation sent.");
                                toast.show();
                            }
                        }, 2000);
                    }else {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                toastLayout = inflater.inflate(
                                        R.layout.error_toast,
                                        findViewById(android.R.id.content),
                                        false);
                                toast.setView(toastLayout);
                                toastText = toastLayout.findViewById(R.id.tvToastError);
                                toastText.setText(r);
                                toast.show();
                            }
                        }, 2000);

                    }
                });
            }else {
                etHostname.setError(resp);
                callBack.onResult(false);
            }
        });

    }


}
