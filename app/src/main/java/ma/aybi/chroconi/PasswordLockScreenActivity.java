package ma.aybi.chroconi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.github.GithubConnection;
import ma.aybi.chroconi.security.Encryptor;

public class PasswordLockScreenActivity extends AppCompatActivity {
    TextView tvInfo, tvReset;
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
        tvInfo = findViewById(R.id.tvInfo);
        tvReset = findViewById(R.id.tvReset);
        unlockButton = findViewById(R.id.unlockButton);
        passwordInput = findViewById(R.id.passwordInput);

        tvInfo.setVisibility(View.GONE);
        tvReset.setVisibility(View.GONE);

        unlockButton.setOnClickListener(v -> {
            v.setActivated(false);
            String password = passwordInput.getText().toString();
            if (passwordCheck(password)) {
                Encryptor.setSalt(
                        PreferencesManager.getSalt(getApplicationContext())
                );
                Encryptor.generateKey(password);
                byte[] byteToken = Encryptor.decrypt(
                        PreferencesManager.getToken(getApplicationContext())
                );
                int attempts = PreferencesManager.getConnAttempts(getApplicationContext());
                if (byteToken == null &&  attempts > 4) {
                    reset();
                }else if (byteToken == null) {
                    passwordInput.setError("Wrong password");
                    PreferencesManager.increamentConnAttempts(getApplicationContext());
                    unlockButton.setText("UNLOCK (" + (5-PreferencesManager.getConnAttempts(getApplicationContext())) + ")" );
                }else{
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
                            PreferencesManager.resetConnAttempts(getApplicationContext());

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
                            tvInfo.setVisibility(View.VISIBLE);
                            tvReset.setVisibility(View.VISIBLE);
                        }
                    });
                }


            }else {
                passwordInput.setError("Please enter a password");
            }
            v.setActivated(true);

        });

        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

    }
    private boolean passwordCheck (String password) {
        return !(password.isEmpty());
    }

    private void reset () {
        PreferencesManager.destroy(getApplicationContext());

        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);

        mainIntent.setPackage(getApplicationContext().getPackageName());
        getApplicationContext().startActivity(mainIntent);
        System.exit(0);
    }
}