package com.example.androidpriogrammingbook01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_the_answer = "com.example.androidpriogrammingbook01.the_answer";
    private static final String EXTRA_cheated = "com.example.androidpriogrammingbook01.cheated";
    private static final String KEY_cheated = "cheated";
    private static final String KEY_answerTextView = "answerTextViewText";
    private static final String TAG = "CheatActivity";


    private boolean mTheAnswer;
    private boolean mCheated = false;

    private Button mRevealAnswerButton;
    private TextView mShowAnswerTextView;

    public static Intent newIntent(Context packageContext, boolean the_answer) {
        Intent intent = new Intent(packageContext, CheatActivity.class);
        intent.putExtra(EXTRA_the_answer, the_answer);
        return intent;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);

        mTheAnswer = getIntent().getBooleanExtra(EXTRA_the_answer, false);

        mShowAnswerTextView = findViewById(R.id.reveal_answer_textView);
        TextView APITextView = findViewById(R.id.api_level_textView);

        mRevealAnswerButton = findViewById(R.id.reveal_answer_button);
        mRevealAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowAnswerTextView.setText(getString(R.string.show_answer_textView_text) + " " + (mTheAnswer ? getString(R.string.true_button) : getString(R.string.false_button)));
                userCheated();

                int cx = mRevealAnswerButton.getWidth() / 2;
                int cy = mRevealAnswerButton.getHeight() / 2;
                float radius = mRevealAnswerButton.getWidth();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animator animator = ViewAnimationUtils.createCircularReveal(mRevealAnswerButton, cx, cy, radius, 0);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRevealAnswerButton.setVisibility(View.INVISIBLE);
                        }
                    });
                    animator.start();
                } else {
                    mRevealAnswerButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (savedInstanceState != null) {
            mCheated = savedInstanceState.getBoolean(KEY_cheated);
            mShowAnswerTextView.setText(savedInstanceState.getCharSequence(KEY_answerTextView));
        }

        APITextView.setText("API LEVEL " + Build.VERSION.SDK_INT + " (" + Build.VERSION.RELEASE + ")");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putBoolean(KEY_cheated, mCheated);
        savedInstanceState.putCharSequence(KEY_answerTextView, mShowAnswerTextView.getText());
    }


    private void userCheated() {
        Intent data = new Intent();
        mCheated = true;
        data.putExtra(EXTRA_cheated, mCheated);
        setResult(RESULT_OK, data);
    }

    public static boolean isUserCheated(Intent result) {
        return result.getBooleanExtra("com.example.androidpriogrammingbook01.cheated", false);
    }
}
