package ma.aybi.chroconi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatActivity extends AppCompatActivity {

    LinearLayout inputContainer;
    TextView tvUsername;
    ImageView btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        inputContainer =
                findViewById(R.id.inputContainer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (view, windowInsets) -> {

                    Insets ime =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.ime());

                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat.Type.systemBars());

                    // MOVE INPUT ABOVE KEYBOARD
                    inputContainer.animate()
                            .translationY(-ime.bottom)
                            .setDuration(180)
                            .start();

                    // KEEP RECYCLER SAFE
                    view.setPadding(
                            0,
                            systemBars.top,
                            0,
                            systemBars.bottom);

                    return windowInsets;
        });


        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false);
        String username =
                getIntent().getStringExtra("username");

        tvUsername = findViewById(R.id.tvUsername);
        btnBack = findViewById(R.id.btnBack);

        tvUsername.setText(username);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InboxActivity.class);
                i.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
        });
    }
}