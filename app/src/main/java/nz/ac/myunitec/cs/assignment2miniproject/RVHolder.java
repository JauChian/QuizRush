package nz.ac.myunitec.cs.assignment2miniproject;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RVHolder extends RecyclerView.ViewHolder {
    TextView tv_name, tv_difficulty, startDate, endDate, likes;
    ImageView btn_like, btn_edit, btn_delete;

    public RVHolder(@NonNull View itemView) {
        super(itemView);

        tv_name = itemView.findViewById(R.id.tv_name);
        tv_difficulty = itemView.findViewById(R.id.tv_difficulty);
        startDate = itemView.findViewById(R.id.tv_start_date);
        endDate = itemView.findViewById(R.id.tv_end_date);
        likes = itemView.findViewById(R.id.tv_like_num);
        btn_like = itemView.findViewById(R.id.btn_like_or_not);
        btn_edit = itemView.findViewById(R.id.btn_edit);
        btn_delete = itemView.findViewById(R.id.btn_delete);

    }
}
