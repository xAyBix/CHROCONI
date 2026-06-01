package ma.aybi.chroconi;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ma.aybi.chroconi.adapter.ConversationAdapter;
import ma.aybi.chroconi.adapter.InvitationAdapter;
import ma.aybi.chroconi.github.BooleanCallBack;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.model.Conversation;
import ma.aybi.chroconi.model.Invitation;

public class InboxActivity extends AppCompatActivity {
    RecyclerView chatRecycler, invitationsRecycler;
    View toastLayout;
    TextView toastText;
    LinearLayout btnNew;
    LinearLayout btnNotifications;

    View vNotificationsDot;



    EditText etHostname;
    EditText etInvitedname;

    ConversationAdapter conversationAdapter;
    InvitationAdapter invitationAdapter;


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
        runOnUiThread(()->{
            GithubConnection.getCollabInvitations(this, success -> {
                        if (!Invitation.invitations.isEmpty()) {
                            vNotificationsDot.invalidate();
                            vNotificationsDot.setVisibility(View.VISIBLE);
                        }
            });
        });




        btnNew.setOnClickListener(v -> {
            showNewChatDialog();
        });
        btnNotifications.setOnClickListener(v -> {
            showNotificationsDialog();
        });



        chatRecycler.setHasFixedSize(true);
        chatRecycler.setItemAnimator(null);


        // Dummy Premium Data


        conversationAdapter = new ConversationAdapter(this);

        chatRecycler.setLayoutManager(
                new LinearLayoutManager(this));


        chatRecycler.setAdapter(conversationAdapter);
    }

    private void showNotificationsDialog() {

        Dialog dialog = new Dialog(this);

        dialog.setContentView(
                R.layout.dialog_notifications);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.getWindow().getAttributes().windowAnimations =
                R.style.DialogAnimation;


        invitationsRecycler = dialog.findViewById(R.id.invitationsRecycler);

        invitationsRecycler.setHasFixedSize(true);
        invitationsRecycler.setItemAnimator(null);
        invitationAdapter = new InvitationAdapter(this);

        invitationsRecycler.setLayoutManager(
                new LinearLayoutManager(this));
        invitationsRecycler.setAdapter(invitationAdapter);

        dialog.show();

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
                    if (s) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                toastText.setText("Invitation sent.");
                                toast.show();
                                new Conversation(hostname + "/" + r + ".git", invitedname, null, null);
                                conversationAdapter.notifyDataSetChanged();
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
