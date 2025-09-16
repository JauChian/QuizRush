package nz.ac.myunitec.cs.assignment2miniproject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NewQuiz extends AppCompatActivity {
    TextView title, btnAdd,result, btnLoad,cat,difficulty;
    EditText name,et_start_date, et_end_date;
    Spinner categorySpinner, difficultySpinner;
    FirebaseFirestore db;
    ImageView btnBack;
    private QuizControllerRESTAPI api;
    Questions questions;
    String selectedCategory = "9";  // Default category
    String selectedDifficulty = "easy";  // Default difficulty
    Date startDate,endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        name = findViewById(R.id.quiz_name);
        categorySpinner = findViewById(R.id.category_spinner);
        difficultySpinner = findViewById(R.id.difficulty_spinner);
        et_start_date = findViewById(R.id.start_date);
        et_end_date = findViewById(R.id.end_date);
        btnAdd = findViewById(R.id.add_new_quiz_button);
        btnBack =findViewById(R.id.back);
        title = findViewById(R.id.title);
        result = findViewById(R.id.result);
        btnLoad = findViewById(R.id.load_new_question_button);
        cat = findViewById(R.id.tv_cat);
        difficulty= findViewById(R.id.tv_difficulty);


        // Get intent data for quiz ID
        Intent intent = getIntent();
        String quizID = intent.getStringExtra("quiz_id");

        // Check if quizID was received and set UI accordingly
        if (quizID != null){
            Toast.makeText(this, "quiz ID received"+ quizID, Toast.LENGTH_SHORT).show();
            title.setText("Edit Quiz");
            btnAdd.setText("Save Changes");
            btnLoad.setVisibility(View.GONE);
            categorySpinner.setVisibility(View.GONE);
            difficultySpinner.setVisibility(View.GONE);
            cat.setVisibility(View.GONE);
            difficulty.setVisibility(View.GONE);
            categorySpinner.setEnabled(false);
            difficultySpinner.setEnabled(false);
            readQuiz(quizID);
        }else{
            Toast.makeText(this, "No quiz ID received", Toast.LENGTH_SHORT).show();
        }

        // Set up date picker dialogs for start dates
        et_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(et_start_date);
            }
        });

        // Set up date picker dialogs for end dates
        et_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(et_end_date);
            }
        });

        // Add quiz or save changes based on quizID existence
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(quizID!=null){
                    Toast.makeText(NewQuiz.this, "Save Changes Clicked", Toast.LENGTH_SHORT).show();
                    updateQuiz(quizID);

                }else{
                    Toast.makeText(NewQuiz.this, "Add New Quiz Clicked", Toast.LENGTH_SHORT).show();
                    addQuiz();
                }
                Intent intent = new Intent(getApplicationContext(), AdminDashboard.class);
                startActivity(intent);
            }
        });

        // Navigate back on clicking back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to the previous screen
                onBackPressed();
            }
        });

        // Load questions from API
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadQuestions();
            }
        });
    }
    // Display date picker dialog
    private void showDatePickerDialog(final EditText editText) {
        try{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // 创建日期选择器
            DatePickerDialog datePickerDialog = new DatePickerDialog(NewQuiz.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // 创建一个 Date 对象来保存选择的日期
                            Calendar selectedDate = Calendar.getInstance();
                            selectedDate.set(year, monthOfYear, dayOfMonth);
                            Date date = selectedDate.getTime();

                            // Save selected date
                            if (editText == et_start_date) {
                                startDate = date;
                                et_start_date.setError(null);
                            } else if (editText == et_end_date) {
                                if (startDate != null && date.before(startDate)) {
                                    // 如果 endDate 早于 startDate，显示错误并清空 endDate
                                    Toast.makeText(NewQuiz.this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
                                    et_end_date.setText("");
                                } else {
                                    endDate = date;
                                    // 清除可能存在的错误消息
                                    et_end_date.setError(null);
                                }
                            }

                            // Format and display selected date in EditText
                            String formattedDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                            editText.setText(formattedDate);
                        }
                    }, year, month, day);

            // 设置最小日期为今天
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // 设置今天为最早可选日期

            // 显示日期选择器
            datePickerDialog.show();

        }catch (Exception e) {
            Log.e("NewQuiz", "Error displaying date picker", e);
        }
    }

    // Fetch quiz details from Firestore
    private void readQuiz(String quizID) {
        try {
            // Retrieve the document from the "Quiz" collection for the given quizID
            db.collection("Quiz").document(quizID)
                    .get()  // 使用 get() 方法获取单个文档
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Quiz quiz = document.toObject(Quiz.class);
                                    name.setText(quiz.getName());

                                    // 日期格式化
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    startDate = quiz.getStartDate();
                                    endDate = quiz.getEndDate();
                                    et_start_date.setText(dateFormat.format(startDate));
                                    et_end_date.setText(dateFormat.format(endDate));

                                } else {
                                    Log.d("Quiz Data", "No such document");
                                }
                            } else {
                                Log.w("Quiz Data", "Error getting document", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("QuizActivity", "Error reading quiz data", e);
        }
    }
    // Add a new quiz to Firestore
    private void addQuiz() {
        try{
            // 获取用户输入的数据
            String quizName = name.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String difficulty = difficultySpinner.getSelectedItem().toString();

            // 验证输入数据是否为空
            if (quizName.isEmpty() || startDate == null || endDate == null) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            questions = api.questions;

            if (questions == null || questions.getResults().isEmpty()) {
                Toast.makeText(this, "Please load questions before adding the quiz", Toast.LENGTH_SHORT).show();
                return;
            }

            // 准备将问题列表添加到 quiz 对象中
            Map<String, Object> quizData = new HashMap<>();
            quizData.put("name", quizName);
            quizData.put("category", category);
            quizData.put("difficulty", difficulty);
            quizData.put("startDate", startDate); // 将 Date 对象直接保存到 Firestore
            quizData.put("endDate", endDate);     // 将 Date 对象直接保存到 Firestore
            quizData.put("likes", 0);  // 初始化 likes 为 0
            quizData.put("quizID","");

            // 将 Quiz 数据保存到 Firebase Firestore
            db.collection("Quiz")
                    .add(quizData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(NewQuiz.this, "Quiz added successfully", Toast.LENGTH_SHORT).show();
                            String documentID = documentReference.getId();  // 获取生成的 documentID
                            db.collection("Quiz")
                                    .document(documentID)
                                    .update("quizID", documentID);  // 更新

                            for (Question question : questions.getResults()) {
                                documentReference.collection("Questions").add(question)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Log.d("Firestore", "Question added with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Firestore", "Error adding question", e);
                                            }
                                        });
                            }
                            finish();  // 关闭当前活动并返回
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewQuiz.this, "Error adding quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e) {
            Log.e("NewQuiz", "Error adding new quiz", e);
        }
    }
    // Update an existing quiz in Firestore
    private void updateQuiz(String quizID) {
        try{
            db = FirebaseFirestore.getInstance();
            String quizName = name.getText().toString().trim();

            if (quizName.isEmpty() || startDate == null || endDate == null) {
                Toast.makeText(this, "Please fill in all fields before saving.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用 Map 更新多个字段
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", quizName);
            updates.put("startDate", startDate);
            updates.put("endDate", endDate);
            Log.d("UpdateQuiz", "Quiz Name: " + quizName);
            Log.d("UpdateQuiz", "Start Date: " + startDate);
            Log.d("UpdateQuiz", "End Date: " + endDate);

            db.collection("Quiz")
                    .document(quizID)
                    .update(updates)  // 使用 Map 一次性更新多个字段
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Quiz Update", "Quiz updated successfully for ID: " + quizID);
                            Toast.makeText(NewQuiz.this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Quiz Update", "Error updating quiz: " + e.getMessage());
                            Toast.makeText(NewQuiz.this, "Error updating quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e) {
            Log.e("Quiz Update", "Error updating quiz", e);
        }
    }

    // Load questions from the API
    private void loadQuestions(){
        api = new QuizControllerRESTAPI(selectedCategory, selectedDifficulty);
        api.start();
    }

    private String getCategoryIDFromName(String categoryName) {
        switch (categoryName) {
            case "9. General Knowledge":
                return "9";
            case "10. Entertainment: Books":
                return "10";
            case "11. Entertainment: Film":
                return "11";
            case "12. Entertainment: Music":
                return "12";
            case "13. Entertainment: Musicals and Theatres":
                return "13";
            case "14. Entertainment: Television":
                return "14";
            default:
                return "9";  // 默认类别
        }
    }

    // 将难度转换为 API 所需的格式
    private String getDifficultyFromName(String difficulty) {
        switch (difficulty) {
            case "Easy":
                return "easy";
            case "Medium":
                return "medium";
            case "Hard":
                return "hard";
            default:
                return "easy";
        }
    }
}