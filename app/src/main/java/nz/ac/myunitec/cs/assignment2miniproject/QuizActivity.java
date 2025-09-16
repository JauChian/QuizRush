package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    final String TAG="QuizActivity";
    FirebaseAuth auth;
    FirebaseUser user;
    String userType, uID;
    ImageView btnBack, btnEsc;
    List<Question> questions;
    int currentQuestionIndex = 0, score=0;
    TextView title, tv_question, btnNext;
    RadioGroup radioGroup;
    RadioButton answer1, answer2, answer3, answer4;
    String correctAnswer,quizID;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize Firebase Firestore and list of questions
        db = FirebaseFirestore.getInstance();
        questions = new ArrayList<>();

        // Bind UI elements
        btnBack = findViewById(R.id.back);
        btnEsc = findViewById(R.id.esc);
        btnNext = findViewById(R.id.btn_next);
        tv_question = findViewById(R.id.question);
        title = findViewById(R.id.question_title);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);
        radioGroup = findViewById(R.id.answersGroup);

        // Receive quizID from intent
        Intent intent = getIntent();
        if (intent != null) {
            quizID = intent.getStringExtra("quizID");
        }

        if (quizID != null) {
            loadQuestions();
        } else {
            Log.e("QuizActivity", "quizID is null!");
        }

        // Initialize FirebaseAuth and get current user
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Check if user is authenticated, otherwise redirect to Login
        if (user == null) {
            Intent intent1 = new Intent(getApplicationContext(), LogIn.class);
            startActivity(intent1);
            finish();
        } else {
            readUser(user.getUid());
        }

        // Set listeners for buttons
        btnEsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ("admin".equals(userType)) {
                    Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                    startActivity(intent);
                    finish();
                } else if ("player".equals(userType)) {
                    Intent intent = new Intent(getApplicationContext(), PlayerDashboard.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to the previous screen
                onBackPressed();
            }
        });

        // Next button logic to move to the next question
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentQuestionIndex < questions.size() - 1) {
                    int selectedId = radioGroup.getCheckedRadioButtonId();  // 获取用户选中的按钮ID

                    if (selectedId == -1) {
                        // 没有选中任何选项时
                        Toast.makeText(QuizActivity.this, "Please select an answer!", Toast.LENGTH_SHORT).show();
                    } else {
                        // 根据ID获取被选中的RadioButton
                        RadioButton selectedRadioButton = findViewById(selectedId);
                        String selectedAnswer = selectedRadioButton.getText().toString();

                        // 检查是否回答正确
                        if (selectedAnswer.equals(correctAnswer)) {
                            score++;
                            Toast.makeText(QuizActivity.this, "Correct!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(QuizActivity.this, "Incorrect! The correct answer is: " + correctAnswer, Toast.LENGTH_SHORT).show();
                        }
                    }
                    currentQuestionIndex++;
                    title.setText("Question "+String.valueOf(currentQuestionIndex + 1));
                    displayQuestion(currentQuestionIndex);
                } else {
                    // 测验完成的逻辑
                    Toast.makeText(QuizActivity.this, "Quiz Completed! Your score is "+String.valueOf(score)+" out of 10!", Toast.LENGTH_SHORT).show();
                    if (user != null) {
                        recordQuizParticipation(user.getUid(), quizID);
                    }
                    if ("admin".equals(userType)) {
                        Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                        startActivity(intent);
                        finish();
                    } else if ("player".equals(userType)) {
                        Intent intent = new Intent(getApplicationContext(), PlayerDashboard.class);
                        startActivity(intent);
                        finish();
                    }
                }

            }
        });
    }

    // Display a question at a specific index
    private void displayQuestion(int index) {
        try{
            Question question = questions.get(index);
            tv_question.setText(question.getQuestion());

            // 获取正确答案和错误答案
            correctAnswer = question.getCorrect_answer();
            List<String> incorrectAnswers = question.getIncorrect_answers();

            // 合并并打乱答案
            List<String> shuffledAnswers = new ArrayList<>(incorrectAnswers);
            shuffledAnswers.add(correctAnswer);
            Collections.shuffle(shuffledAnswers);

            // 设置打乱的答案
            answer1.setText(shuffledAnswers.get(0));
            answer2.setText(shuffledAnswers.get(1));
            answer3.setText(shuffledAnswers.get(2));
            answer4.setText(shuffledAnswers.get(3));
        }catch (Exception e) {
            Log.e(TAG, "Error displaying question", e);
        }
    }

    // Load questions from Firestore
    private void loadQuestions() {
        try{
            db.collection("Quiz")
                    .document(quizID)  // 用 quizID 获取相应的 Quiz 文档
                    .collection("Questions")  // 获取 Questions 子集合
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Question question = document.toObject(Question.class); // 将 Firestore 数据转换为 Question 对象
                                    questions.add(question);  // 将每个问题添加到列表
                                }
                                Log.d("Questions", "Loaded " + questions.size() + " questions.");
                                if (!questions.isEmpty()) {
                                    displayQuestion(0);  // 显示第一个问题
                                } else {
                                    Toast.makeText(QuizActivity.this, "No questions found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w("Questions", "Error getting questions.", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error loading questions from Firestore", e);
        }
    }
    // Read user details from Firestore
    public void readUser(String userID) {
        try{
            // 查找 "Users" 集合中的指定 userID 文檔
            db.collection("Users").document(userID).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                uID = documentSnapshot.getId();
                                userType = documentSnapshot.getString("userType");

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error reading document", e);
                        }
                    });
        }catch (Exception e) {
            Log.e(TAG, "Error reading user from Firestore", e);
        }
    }


    private void recordQuizParticipation(String userID, String quizID) {
        try{
            db = FirebaseFirestore.getInstance();
            // 將 quizID 添加到 Users 集合中的 participatedGames 列表中
            db.collection("Users").document(userID)
                    .update("participatedGames", FieldValue.arrayUnion(quizID))  // 使用 arrayUnion() 添加 quizID 到 participatedGames 列表
                    .addOnSuccessListener(aVoid -> {
                        // 成功將 quizID 添加到 participatedGames 列表
                        Log.d("QuizActivity", "Added quizID to participatedGames list");
                    })
                    .addOnFailureListener(e -> {
                        // 更新失敗，記錄錯誤
                        Log.e("QuizActivity", "Error updating participatedGames list", e);
                    });

        }catch (Exception e) {
            Log.e(TAG, "Error update participated games to firestore", e);
        }
    }

}