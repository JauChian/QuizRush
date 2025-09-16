package nz.ac.myunitec.cs.assignment2miniproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RVAdapter extends RecyclerView.Adapter<RVHolder> {
    private List<Quiz> quizList;
    private Context context;
    private String userType;
    private boolean isClickable;

    public RVAdapter(List<Quiz> quizList, Context context, String userType, boolean isClickable) {
        this.quizList = quizList;
        this.context = context;
        this.userType = userType;
        this.isClickable = isClickable;
    }

    @NonNull
    @Override
    public RVHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_quiz_tournament, parent, false);
        return new RVHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVHolder holder, int position) {
        Quiz quiz = quizList.get(position);

        // Set data for UI components
        holder.likes.setText(String.valueOf(quiz.getLikes()));
        holder.tv_name.setText("Name: "+ quiz.getName());
        holder.tv_difficulty.setText("Difficulty: "+ quiz.getDifficulty());

        // Format and set dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (quiz.getStartDate() != null) {
            holder.startDate.setText("Start Date: " + dateFormat.format(quiz.getStartDate()));
        }

        if (quiz.getEndDate() != null) {
            holder.endDate.setText("End Date: " + dateFormat.format(quiz.getEndDate()));
        }

        // Check if user is logged in
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        // Control visibility of delete and edit buttons based on user type
        if ("admin".equals(userType)) {
            holder.btn_delete.setVisibility(View.VISIBLE);
            holder.btn_edit.setVisibility(View.VISIBLE);
        } else {
            holder.btn_delete.setVisibility(View.GONE);
            holder.btn_edit.setVisibility(View.GONE);
        }

        // Like button click listener with Firebase update
        holder.btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // 判断当前的 contentDescription
                    if (holder.btn_like.getContentDescription().equals("not_like")) {
                        // 当前是未点赞状态 -> 切换为已点赞
                        holder.btn_like.setImageResource(R.drawable.icn_like);  // 设置为已点赞图标
                        holder.btn_like.setContentDescription("like");  // 更新 contentDescription

                        quiz.setLikes(quiz.getLikes() + 1);  // 增加 likes 数量
                    } else {
                        // 当前是已点赞状态 -> 切换为未点赞
                        holder.btn_like.setImageResource(R.drawable.icn_not_like);  // 设置为未点赞图标
                        holder.btn_like.setContentDescription("not_like");  // 更新 contentDescription

                        quiz.setLikes(quiz.getLikes() - 1);  // 减少 likes 数量
                    }

                    // 更新 Firebase 中的 likes 数量
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Quiz")
                            .document(quiz.getQuizID())
                            .update("likes", quiz.getLikes())  // 只更新 likes
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    holder.likes.setText(String.valueOf(quiz.getLikes()));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });

                }catch (Exception e) {
                    Log.e("RVAdapter", "Error updating like status", e);
                }
            }
        });

        // Delete button click listener
        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    // 假设 quiz.getQuizID() 返回要删除的文档 ID
                    db.collection("Quiz").document(quiz.getQuizID())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // 成功删除后的处理逻辑
                                    Log.d("Firebase", "Quiz deleted successfully!");
                                    Toast.makeText(context, "Quiz deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 处理删除失败的情况
                                    Log.e("Firebase", "Delete failed: " + e.getMessage());
                                    Toast.makeText(context, "Error deleting quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } catch (Exception e) {
                    Log.e("RVAdapter", "Error deleting quiz", e);
                }
            }
        });

        // Edit button click listener for navigating to NewQuiz activity
        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewQuiz.class);
                // 假设 quiz.getQuizID() 返回的是你要传递的 question ID
                intent.putExtra("quiz_id", quiz.getQuizID());
                context.startActivity(intent);
            }
        });

        // Control item click based on login status and click ability(past, upcoming or attended)
        if (!isLoggedIn || !isClickable) {
            holder.itemView.setOnClickListener(null);  // 禁用點擊
            holder.itemView.setAlpha(0.5f);  // 調低透明度表示不可用
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, QuizActivity.class);
                    intent.putExtra("quizID", quiz.getQuizID());
                    context.startActivity(intent);
                }
            });
            holder.itemView.setAlpha(1.0f);  // 正常透明度
        }
    }

    @Override
    public int getItemCount() {
        // 返回用户列表的长度
        return quizList.size();
    }

    // Method to update data and click ability state
    public void updateData(List<Quiz> newList, boolean isClickable) {
        this.quizList = newList;
        this.isClickable = isClickable;
        notifyDataSetChanged();
    }
}
