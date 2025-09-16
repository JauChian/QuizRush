package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {
    TextView btnAdd;
    RecyclerView r_view;
    ImageView btnLogout;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        btnLogout = findViewById(R.id.img_login_out);
        btnAdd = findViewById(R.id.add_new_quiz_button);
        r_view = findViewById(R.id.r_view);

        // Set up RecyclerView
        r_view.setHasFixedSize(true);
        r_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // Load quiz categories from Firestore
        readQuiz();

        // Initialize FirebaseAuth and get current user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Set click listener for adding a new quiz
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminDashboard.this, NewQuiz.class);
                startActivity(intent);
            }
        });

        // Set click listener for logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null) {
                    // Sign out the user
                    auth.signOut();
                    Toast.makeText(AdminDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    // Redirect to login page after logout
                    Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reload quizzes when returning to the dashboard
        readQuiz();
    }

    // Method to read quizzes from Firestore
    private void readQuiz() {
        try {
            db.collection("Quiz")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            try {
                                if (task.isSuccessful()) {
                                    // List to store Quiz objects
                                    List<Quiz> quizList = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Convert Firestore document to Quiz object
                                        Quiz quiz = document.toObject(Quiz.class);
                                        quizList.add(quiz);
                                        Log.d("Quiz", "Quiz Name: " + quiz.getName() + ", Category: " + quiz.getCategory());
                                    }
                                    // Set the RecyclerView adapter with quiz data
                                    RVAdapter adapter = new RVAdapter(quizList, AdminDashboard.this, "admin", true);
                                    r_view.setAdapter(adapter);
                                } else {
                                    Log.w("Quiz", "Error getting documents.", task.getException());
                                }
                            } catch (Exception e) {
                                Log.e("AdminDashboard", "Error processing quiz data", e);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("NewQuiz", "Error reading quizzes", e);
        }
    }

}