package com.example.androidpriogrammingbook01;

public class Question {
    public int getTextResourceID() {
        return mTextResourceID;
    }
    public void setTextResourceID(int textResourceID) {
        mTextResourceID = textResourceID;
    }
    private int mTextResourceID;

    public boolean isQuestionAnswer() {
        return mQuestionAnswer;
    }
    public void setQuestionAnswer(boolean questionAnswer) {
        mQuestionAnswer = questionAnswer;
    }
    private boolean mQuestionAnswer;


    public boolean isQuestionAnswered() {
        return mQuestionAnswered;
    }
    public void setQuestionAnswered(boolean questionAnswered) {
        mQuestionAnswered = questionAnswered;
    }
    private boolean mQuestionAnswered = false;

    public Question(int textResourceID, boolean answeredCorrect){
        mQuestionAnswer = answeredCorrect;
        mTextResourceID = textResourceID;
    }
}
