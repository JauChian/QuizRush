package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    FirebaseFirestore db;
    EditText txtEmail, txtPass, txtPass2;
    TextView btnReg, btnLogin;
    Spinner roleSpinner ;
    private FirebaseAuth mAuth;
    final String TAG="REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        // Initialize Firebase Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind UI elements
        roleSpinner = findViewById(R.id.register_role_spinner);
        txtEmail = findViewById(R.id.register_email);
        txtPass = findViewById(R.id.register_password);
        txtPass2 = findViewById(R.id.register_confirm_password);
        btnReg = findViewById(R.id.register_button);
        btnLogin = findViewById(R.id.login_redirect);

        // Set default role selection to "player"
        roleSpinner.setSelection(0);

        // Redirect to Login screen when login button is clicked
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
            }
        });

        // Register button click listener for handling user registration
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, confirm_password;
                try{
                    email = txtEmail.getText().toString().trim().toLowerCase();
                    password = txtPass.getText().toString().trim();
                    confirm_password = txtPass2.getText().toString().trim();

                    if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm_password)) {
                        Toast.makeText(Register.this,
                                "You must provide both email and password",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 檢查 password 和 confirm_password 是否一致
                    if (!password.equals(confirm_password)) {
                        Toast.makeText(Register.this,
                                "Passwords do not match",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "Registration Successful.",
                                                Toast.LENGTH_SHORT).show();
                                        //Add user to firestore
                                        addUser(view);

                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }catch (Exception e) {
                    Log.e(TAG, "Error during registration process", e);
                    Toast.makeText(Register.this, "An error occurred during registration.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Add user data to Firestore
    public void addUser(View view) {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null) {
                String userID = currentUser.getUid();
                // Check if a role is selected in the spinner
                if (roleSpinner == null || roleSpinner.getSelectedItem() == null) {
                    Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Prepare user data for Firestore
                String selectedRole = roleSpinner.getSelectedItem().toString();
                Map<String, Object> user = new HashMap<>();
                user.put("participatedGames", new ArrayList<String>());

                // Determine user role and add it to user data
                if (selectedRole.equals("player")) {
                    user.put("userType", "player");
                } else if (selectedRole.equals("admin")) {
                    user.put("userType", "admin");
                }

                // Use UID as Firestore document ID for the user
                db.collection("Users").document(userID)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User added with UID: " + userID);

                                // Create empty Info sub-collection for the user
                                Map<String, Object> emptyInfo = new HashMap<>(); // 创建一个空的 Map
                                db.collection("Users").document(userID).collection("Info")
                                        .add(emptyInfo)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d(TAG, "Empty Info document created successfully with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error creating empty Info document", e);
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding user", e);
                            }
                        });
            } else {
                Log.w(TAG, "No user is logged in.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding user", e);
            Toast.makeText(this, "An error occurred while adding the user.", Toast.LENGTH_SHORT).show();
        }
    }
}