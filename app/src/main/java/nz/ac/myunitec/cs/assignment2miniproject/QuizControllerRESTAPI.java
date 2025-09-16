package nz.ac.myunitec.cs.assignment2miniproject;

import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuizControllerRESTAPI implements Callback<Questions> {

    final String BASE_URL ="https://opentdb.com/";
    public Questions questions;
    List<Question> questionList;
    String category;
    String difficulty;


    public QuizControllerRESTAPI(String category, String difficulty) {
        this.category = category;
        this.difficulty = difficulty;
    }

    public void start() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuestionsRESTAPI questionsRESTAPI = retrofit.create(QuestionsRESTAPI.class);
        String apiUrl = BASE_URL + "api.php?amount=10&category=" + category + "&difficulty=" + difficulty+"&type=multiple";
        Call<Questions> call = questionsRESTAPI.getQuestions(apiUrl);
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<Questions> call, Response<Questions> response) {
        if(response.isSuccessful()){
            //Log.d("Response",response.toString());
            questions = response.body();
            Log.d("QUESTION_COUNT"," QUESTION Count: "+ questions.getResults().size());
            questionList = questions.getResults();
            if(questionList!=null)
                for (Question q: questionList){
                    Log.d("QUESTION_INFO"," QUESTION :"+ q.toString());
                }
            else
                Log.d("QUESTION_INFO"," QUESTION's List empty");
            Log.d("QUESTION_COUNT"," Question Count: "+ questions.getResults().size());
        }
    }

    @Override
    public void onFailure(Call<Questions> call, Throwable t) {
        t.printStackTrace();
        Log.d("QUESTION_INFO","Error getting users");

    }

    public List<Question> getQuestionList() {
        return questionList;  // 返回获取到的列表
    }
}
