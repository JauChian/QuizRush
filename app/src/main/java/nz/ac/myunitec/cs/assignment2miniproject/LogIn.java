package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogIn extends AppCompatActivity {
    EditText txtEmail, txtPass;
    TextView tvReg, btnLogin;
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        try {
            // Check if user is already signed in, if yes, go to MainActivity
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error checking user authentication state.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        txtEmail = findViewById(R.id.email);
        txtPass = findViewById(R.id.password);
        btnLogin = findViewById(R.id.login_button);
        tvReg = findViewById(R.id.register_redirect);
        mAuth = FirebaseAuth.getInstance();

        // Redirect to Register screen when "register" text is clicked
        tvReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

        // Handle login button click to authenticate user
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Retrieve email and password from input fields
                    String email = txtEmail.getText().toString();
                    String password = txtPass.getText().toString();

                    // Check if email or password fields are empty
                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                        Toast.makeText(LogIn.this,
                                "You must provide both email and password",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Authenticate user with Firebase
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            // Sign-in success, navigate to MainActivity
                                            Toast.makeText(LogIn.this, "Authentication Successful.",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Sign-in failed, show error message
                                            Toast.makeText(LogIn.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(LogIn.this, "Error during authentication.", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(LogIn.this, "Error processing login.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
        });

    }



}