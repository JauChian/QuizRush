package nz.ac.myunitec.cs.assignment2miniproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface QuestionsRESTAPI {

    @GET
    Call<Questions> getQuestions(@Url String url);
}