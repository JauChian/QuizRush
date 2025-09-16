package nz.ac.myunitec.cs.assignment2miniproject;


import java.util.Date;
import java.util.List;
import java.util.Set;

public class Quiz {
    String quizID;
    String name;
    String category;
    String difficulty;
    Date startDate, endDate;
    int likes;
    List<Question> questions;

    public Quiz() {
    }

    public Quiz(String quizID, String name, String category, String difficulty, Date startDate, Date endDate, int likes, List<Question> questions) {
        this.quizID = quizID;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.likes = likes;
        this.questions = questions;
    }

    public String getQuizID() {
        return quizID;
    }

    public void setQuizID(String quizID) {
        this.quizID = quizID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    //use it for test if we get it from api or not
    @Override
    public String toString() {
        return "Quiz{" +
                "id=" + quizID +
                ", difficulty='" + difficulty + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", likes='" + likes + '\'' +
                '}';
    }
}
