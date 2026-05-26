package ma.aybi.chroconi;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ma.aybi.chroconi.adapter.ConversationAdapter;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.model.Conversation;

public class InboxActivity extends AppCompatActivity {
    RecyclerView chatRecycler;
    LinearLayout btnNew;

    ConversationAdapter adapter;
    List<Conversation> conversationList;
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

//        GithubConnection.createRepository(getApplicationContext(), "xAyBix", "yassine-mar", (success, repo) -> {
//            if (success) {
//                GithubConnection.setCollab(getApplicationContext(), repo,"xAyBix", "yassine-mar", (s, u) -> {
//
//                });
//            }else {
//                Log.d(">>FAILURE", "null");
//            }
//        });

        chatRecycler = findViewById(R.id.chatRecycler);
        btnNew =
                findViewById(R.id.btnNew);

        btnNew.setOnClickListener(v -> {

            showNewChatDialog();

        });

        chatRecycler.setHasFixedSize(true);
        chatRecycler.setItemAnimator(null);

        conversationList = new ArrayList<>();

        // Dummy Premium Data
        conversationList.add(
                new Conversation(
                        "Fatima Zahrae",
                        "See you tonight ✨",
                        "11:42"));

        conversationList.add(
                new Conversation(
                        "Hamza",
                        "Hhhh",
                        "09:21"));

        conversationList.add(
                new Conversation(
                        "Mohammed Amine",
                        "Wach",
                        "Yesterday"));

        adapter = new ConversationAdapter(
                this,
                conversationList);

        chatRecycler.setLayoutManager(
                new LinearLayoutManager(this));


        chatRecycler.setAdapter(adapter);
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
        EditText etHostname = dialog.findViewById(R.id.etHostname);
        EditText etInvitedname = dialog.findViewById(R.id.etInvitedname);

        // BUTTON
        Button btnCreate = dialog.findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {

            String hostname = etHostname.getText().toString().trim();
            String invitedname = etInvitedname.getText().toString().trim();

            // VALIDATION
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

            // 👉 NOW YOU HAVE THE VALUES
            createConversation(hostname, invitedname);

            dialog.dismiss();
        });
    }
    private void createConversation (String hostname, String invitedname) {
        GithubConnection.createRepository(getApplicationContext(), hostname, invitedname, (success, repo) -> {
            if (success) {
                GithubConnection.setCollab(getApplicationContext(), repo, hostname, invitedname, (s, u) -> {

                });
            }else {
                Log.d(">>FAILURE", "null");
            }
        });
    }
}
