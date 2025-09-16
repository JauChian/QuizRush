package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db;
    final String TAG="MainActivity";
    FirebaseAuth auth;
    FirebaseUser user;
    String userType, uID;

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
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Check if user is logged in, if not, redirect to PlayerDashboard
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), PlayerDashboard.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Hi " + user.getUid(), Toast.LENGTH_SHORT).show();
            // Fetch user data from Firestore
            readUser(user.getUid());
        }
    }

    // Method to read user information based on userID
    public void readUser(String userID) {
        try {
            db.collection("Users").document(userID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                // Check if the document exists and retrieve user information
                                if (documentSnapshot.exists()) {
                                    uID = documentSnapshot.getId();
                                    userType = documentSnapshot.getString("userType");

                                    // Redirect based on user type
                                    if ("admin".equals(userType)) {
                                        Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                                        startActivity(intent);
                                        finish();
                                    } else if ("player".equals(userType)) {
                                        Intent intent = new Intent(getApplicationContext(), PlayerDashboard.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Invalid user type found
                                        Toast.makeText(MainActivity.this, "Error reading user type!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    // Document does not exist
                                    Log.d(TAG, "No such document");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing user data", e);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Log the failure to retrieve document
                            Log.w(TAG, "Error reading document", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error reading user data from Firestore", e);
        }
    }
}