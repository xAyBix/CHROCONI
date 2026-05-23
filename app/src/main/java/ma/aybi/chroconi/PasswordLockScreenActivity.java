package ma.aybi.chroconi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.security.Encryptor;

public class PasswordLockScreenActivity extends AppCompatActivity {
    Button unlockButton;
    EditText passwordInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_lock_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(
                android.graphics.Color.TRANSPARENT);
        unlockButton = findViewById(R.id.unlockButton);
        passwordInput = findViewById(R.id.passwordInput);
        unlockButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString();
            if (passwordCheck(password)) {
                Encryptor.setSalt(
                        PreferencesManager.getSalt(getApplicationContext())
                );
                Encryptor.generateKey(password);
                byte[] byteToken = Encryptor.decrypt(
                        PreferencesManager.getToken(getApplicationContext())
                );
                if (byteToken == null) {
                    passwordInput.setError("Wrong password");
                }else {
                    String textToken = Encryptor.byteToString(byteToken);
                    GithubConnection.testConnection(getApplicationContext(), textToken, success -> {
                        if (success) {
                            Intent intent =
                                    new Intent(
                                            this,
                                            InboxActivity.class);
                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                            startActivity(intent);

                            unlockButton.animate()
                                    .scaleX(0.96f)
                                    .scaleY(0.96f)
                                    .setDuration(80)
                                    .withEndAction(() -> {

                                        unlockButton.animate()
                                                .scaleX(1f)
                                                .scaleY(1f)
                                                .setDuration(80);

                                    });

                            overridePendingTransition(
                                    R.anim.slide_up,
                                    R.anim.slide_fade_out);
                            finish();
                        }else {
                            passwordInput.setError("Unable to connect");
                        }
                    });
                }


            }else {
                passwordInput.setError("Please enter a password");
            }



        });
    }
    private boolean passwordCheck (String password) {
        return !(password.isEmpty());
    }
}