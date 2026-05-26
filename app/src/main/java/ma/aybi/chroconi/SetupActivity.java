package ma.aybi.chroconi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

public class SetupActivity extends AppCompatActivity {
    EditText etPassword, etConfirmPassword, etToken;
    Button btnSetup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etToken = findViewById(R.id.etInvitedname);
        btnSetup = findViewById(R.id.btnSetup);

        setPasswordInputsListeners();

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setActivated(false);
                String pText = etPassword.getText().toString();
                String cText = etConfirmPassword.getText().toString();
                String tText = etToken.getText().toString();
                if(pText.length()<8) {
                    etPassword.setError("Password must be at least 8 characters");
                }else if (!isValidPassword(pText)) {
                    etPassword.setError("Requires: uppercase, lowercase, and number");
                }else {
                    etPassword.setError(null);
                }

                if (!cText.equals(pText)) {
                    etConfirmPassword.setError("Passwords don't match");
                }else {
                    etConfirmPassword.setError(null);
                }

                if(tText.isEmpty()) {
                    etToken.setError("Token is required");
                }else{
                    tryToConnect(tText);

                }
                v.setActivated(true);

            }
        });


    }

    private boolean isValidPassword(String password) {
        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            // Early exit if all conditions are met
            if (hasLower && hasUpper && hasDigit) {
                return true;
            }
        }
        return false;
    }
    private void setPasswordInputsListeners () {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length()<8) {
                    etPassword.setError("Password must be at least 8 characters");
                }else if (!isValidPassword(s.toString())) {
                    etPassword.setError("Requires: uppercase, lowercase, and number");
                }else {
                    etPassword.setError(null);
                }
            }
        });

        etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(etPassword.getText().toString())) {
                    etConfirmPassword.setError("Passwords don't match");
                }else {
                    etConfirmPassword.setError(null);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
    private void tryToConnect (String token) {
        GithubConnection.testConnection(getApplicationContext(), token, success -> {
           if (success) {
               // Generating salt
               Encryptor.setSalt(
                       Encryptor.generateSalt()
               );
               Encryptor.generateKey(etPassword.getText().toString());

               byte[] encryptedToken = Encryptor.encrypt(
                       Encryptor.stringToByte(token)
               );


               // Store the token and salt
               PreferencesManager.setSalt(getApplicationContext(), Encryptor.getSalt());
               PreferencesManager.setToken(getApplicationContext(), encryptedToken);

               PreferencesManager.setFirstTime(getApplicationContext(), false);


               Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
               intent.setFlags(
                       Intent.FLAG_ACTIVITY_NEW_TASK
                               | Intent.FLAG_ACTIVITY_CLEAR_TASK);

               startActivity(intent);
               overridePendingTransition(
                       R.anim.slide_up,
                       R.anim.slide_fade_out);
               finish();
           }else {
               etToken.setError("Unable to connect");
           }
        });
    }
}