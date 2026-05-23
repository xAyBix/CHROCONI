package ma.aybi.chroconi;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

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
import ma.aybi.chroconi.model.Conversation;

public class InboxActivity extends AppCompatActivity {
    RecyclerView chatRecycler;

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

        chatRecycler = findViewById(R.id.chatRecycler);
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
}
