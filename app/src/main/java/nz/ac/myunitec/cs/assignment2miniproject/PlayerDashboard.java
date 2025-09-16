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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PlayerDashboard extends AppCompatActivity implements View.OnClickListener {
    ImageView btnLogin;
    RecyclerView r_view;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore db;
    TextView tvPast, tvOngoing, tvUpcoming,selectedTextView,tvParticipated;
    Date currentDate;
    List<Quiz> quizList,pastQuizList, ongoingQuizList, upcomingQuizList, participatedQuizList;
    RVAdapter adapter;
    String userType, uID;
    List<String> participatedGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI components
        btnLogin = findViewById(R.id.img_login_out);
        tvPast = findViewById(R.id.tv_past);
        tvOngoing = findViewById(R.id.tv_ongoing);
        tvUpcoming = findViewById(R.id.tv_upcoming);
        tvParticipated = findViewById(R.id.tv_participated);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user =auth.getCurrentUser();

        // Check if user is logged in
        if (user != null) {
            Toast.makeText(this, "Hi " + user.getUid(), Toast.LENGTH_SHORT).show();
            readUser(user.getUid());
            btnLogin.setImageResource(R.drawable.icn_logout);
        }else{
            btnLogin.setImageResource(R.drawable.icn_login);
        }

        // Initialize lists for quiz data
        quizList = new ArrayList<>();
        pastQuizList= new ArrayList<>();
        ongoingQuizList = new ArrayList<>();
        upcomingQuizList = new ArrayList<>();
        participatedGames = new ArrayList<>();
        participatedQuizList = new ArrayList<>();

        // Initialize current date
        currentDate = new Date();

        // Set up RecyclerView with an adapter
        r_view = findViewById(R.id.r_view);
        r_view.setHasFixedSize(true);
        r_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new RVAdapter(ongoingQuizList, PlayerDashboard.this,"player",true);
        r_view.setAdapter(adapter);

        // Load quiz data
        readQuiz();

        // Set click listeners for buttons
        tvPast.setOnClickListener(this);
        tvOngoing.setOnClickListener(this);
        tvUpcoming.setOnClickListener(this);
        tvParticipated.setOnClickListener(this);

        // Select the ongoing tab by default
        selectedTextView = tvOngoing;
        selectedTextView.setSelected(true);
        onClick(tvOngoing);

        // Login or logout button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (user == null) {
                        // Go to login screen if not logged in
                        Intent intent = new Intent(getApplicationContext(), LogIn.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign out and go to main screen
                        auth.signOut();
                        Toast.makeText(PlayerDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PlayerDashboard.this, MainActivity.class);
                        startActivity(intent);
                    }
                }catch (Exception e) {
                    Log.e("PlayerDashboard", "Error during login/logout", e);
                }
            }
        });
    }

    // Filter quiz into lists based on their date status (past, ongoing, upcoming, participated)
    private void filteredList() {
        try {
            upcomingQuizList.clear();
            ongoingQuizList.clear();
            pastQuizList.clear();
            participatedQuizList.clear();

            for (Quiz q : quizList) {
                if (participatedGames != null && participatedGames.contains(q.getQuizID())) {
                    participatedQuizList.add(q);
                } else {
                    if (q.endDate.before(currentDate)) {
                        pastQuizList.add(q);
                    } else if (q.startDate.after(currentDate)) {
                        upcomingQuizList.add(q);
                    } else {
                        ongoingQuizList.add(q);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PlayerDashboard", "Error filtering quizzes", e);
        }
    }

    // Handle tab selection for different quiz status
    @Override
    public void onClick(View view) {
        try {
            if (selectedTextView != null) {
                selectedTextView.setSelected(false);
            }
            selectedTextView = (TextView) view;
            selectedTextView.setSelected(true);
            if (view.getId() == R.id.tv_past) {
                Toast.makeText(PlayerDashboard.this, "Past selected", Toast.LENGTH_SHORT).show();
                adapter.updateData(pastQuizList, false);
            } else if (view.getId() == R.id.tv_ongoing) {
                Toast.makeText(PlayerDashboard.this, "Ongoing selected", Toast.LENGTH_SHORT).show();
                adapter.updateData(ongoingQuizList, true);
            } else if (view.getId() == R.id.tv_upcoming) {
                Toast.makeText(PlayerDashboard.this, "Upcoming selected", Toast.LENGTH_SHORT).show();
                adapter.updateData(upcomingQuizList, false);
            } else if (view.getId() == R.id.tv_participated) {
                adapter.updateData(participatedQuizList, false);
            }
        } catch (Exception e) {
            Log.e("PlayerDashboard", "Error handling tab selection", e);
        }
    }

    // Fetch all quiz data from Firestore and populate quizList
    private void readQuiz() {
        try {
            db.collection("Quiz")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                quizList.clear();  // 確保每次重新加載時清空 quizList
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Quiz quiz = document.toObject(Quiz.class);
                                    quizList.add(quiz);  // 將 quiz 添加到 quizList 中
                                    Log.d("Quiz", "Quiz Name: " + quiz.getName() + ", Category: " + quiz.getCategory());
                                }
                                filteredList();
                                adapter.updateData(ongoingQuizList,true);  // 初始設置為 ongoingQuizList
                            } else {
                                Log.w("Quiz", "Error getting documents.", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("NewQuiz", "Error reading quizzes", e);
        }
    }

    // Fetch user data based on userID and check for participated games
    public void readUser(String userID) {
        try {
            db.collection("Users").document(userID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                if (documentSnapshot.exists()) {
                                    uID = documentSnapshot.getId();
                                    userType = documentSnapshot.getString("userType");
                                    participatedGames = (List<String>) documentSnapshot.get("participatedGames");
                                } else {
                                    Log.d("Participated Games", "No such document");
                                }
                            } catch (Exception e) {
                                Log.e("PlayerDashboard", "Error processing user data", e);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("Participated Games", "Error reading document", e);
                        }
                    });
        } catch (Exception e) {
            Log.e("PlayerDashboard", "Error reading user data", e);
        }
    }
}