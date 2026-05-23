package ma.aybi.chroconi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ma.aybi.chroconi.config.PreferencesManager;
import ma.aybi.chroconi.security.Encryptor;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().setStatusBarColor(
                android.graphics.Color.TRANSPARENT);
        new Handler().postDelayed(() -> {
            if(PreferencesManager.isFirstTime(this)) {
                intent = new Intent(this, SetupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out);

                finish();
            }else {
                intent = new Intent(this, PasswordLockScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out);

                finish();
            }

        }, 2200);
    }
}