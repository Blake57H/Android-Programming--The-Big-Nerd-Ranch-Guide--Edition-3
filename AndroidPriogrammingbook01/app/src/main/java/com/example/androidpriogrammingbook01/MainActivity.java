package com.example.androidpriogrammingbook01;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private Button true_button;
    private Button false_button;
    private ImageButton mNextButton;
    private ImageButton mPreviousButton;
    private Button mShowAnswerButton;
    private TextView mQuestionTextView;
    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_africa, false),
            new Question(R.string.question_australia, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_oceans, true)
    };
    private int mCurrentIndex = 0;
    private int mCorrectAnswerCount = 0;
    private int mAnsweredCount = 0;
    private int mCheatCounter;

    // 0 = not answered, 1 = answered correctly, 2 = answer incorrectly
    private int[] mAnsweredState = new int[mQuestionBank.length];

    private static final String TAG = "MainActivity";

    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWER_STATE = "answer_state";
    private static final String KEY_cheat_counter = "cheat_counter";

    private static final int REQUEST_CODE_cheat_state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_main);

        mCheatCounter = 0;

        //mQuestionTextView = (TextView) findViewById(R.id.previous_button);
        mQuestionTextView = (TextView) findViewById(R.id.textView_question);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateQuestion(true, false);
            }
        });

        true_button = (Button) findViewById(R.id.button_true);
        true_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckAnswer(true, false);
            }
        });

        false_button = (Button) findViewById(R.id.button_false);
        false_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckAnswer(false, false);
            }
        });

        mNextButton = findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateQuestion(true, false);
            }
        });

        mPreviousButton = findViewById(R.id.previous_button);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateQuestion(false, false);
            }
        });

        mShowAnswerButton = findViewById(R.id.reveal_answer_button);
        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CheatActivity();
                startActivityForResult(CheatActivity.newIntent(MainActivity.this, mQuestionBank[mCurrentIndex].isQuestionAnswer()), REQUEST_CODE_cheat_state);
            }
        });

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mAnsweredState = savedInstanceState.getIntArray(KEY_ANSWER_STATE);
            mCheatCounter = savedInstanceState.getInt(KEY_cheat_counter, 0);
            for (int a = 0; a < mQuestionBank.length; a += 1) {
                if (mAnsweredState[a] == 1)
                    CheckAnswer(mQuestionBank[a].isQuestionAnswer(), true);
                else if (mAnsweredState[a] == 2)
                    CheckAnswer(!mQuestionBank[a].isQuestionAnswer(), true);
                else if (mAnsweredState[a] == 0)
                    SetYesNoButtonEnabled(true);
            }
        } else {
            for (int a = 0; a < mQuestionBank.length; a += 1) {
                mAnsweredState[a] = 0;
            }
        }
        UpdateQuestion(false, true);
        CheckCheatCount();
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
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putIntArray(KEY_ANSWER_STATE, mAnsweredState);
        savedInstanceState.putInt(KEY_cheat_counter, mCheatCounter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_cheat_state) {
            if (data == null)
                return;
            mCheatCounter += CheatActivity.isUserCheated(data) ? 1 : 0;
            CheckCheatCount();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        Log.d(TAG, "onCreateView() called");
        return super.onCreateView(parent, name, context, attrs);
    }

    private void UpdateQuestion(boolean next, boolean newOne) {
        //Log.d(TAG, "Update question text", new Exception());
        int toNext = (next ? 1 : mQuestionBank.length - 1);
        if (newOne) toNext = 0;
        mCurrentIndex = (mCurrentIndex + toNext) % mQuestionBank.length;
        mQuestionTextView.setText(mQuestionBank[mCurrentIndex].getTextResourceID());
        if (mAnsweredState[mCurrentIndex] != 0) {
            SetYesNoButtonEnabled(false);
        } else {
            SetYesNoButtonEnabled(true);
        }
    }

    private void CheckAnswer(boolean userAnswer, boolean isRestoringStatus) {
        int resultToast = 0, answer_state = 0;
        if (userAnswer == mQuestionBank[mCurrentIndex].isQuestionAnswer()) {
            resultToast = R.string.correct_answer_toast;
            mCorrectAnswerCount += 1;
            answer_state = 1;
        } else {
            resultToast = R.string.incorrect_answer_toast;
            answer_state = 2;
        }

        if (!isRestoringStatus) {
            Toast.makeText(MainActivity.this, resultToast, Toast.LENGTH_SHORT).show();
            mAnsweredState[mCurrentIndex] = answer_state;
        }
        mQuestionBank[mCurrentIndex].setQuestionAnswered(true);
        SetYesNoButtonEnabled(false);
        mAnsweredCount += 1;
        if (mAnsweredCount >= mQuestionBank.length) {
            mNextButton.setEnabled(false);
            mPreviousButton.setEnabled(false);
            float scoreF = ((float) mCorrectAnswerCount / (float) mAnsweredCount);
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            String score = df.format(scoreF);
            Toast.makeText(MainActivity.this,
                    getString(R.string.quiz_complete_toast) + " " + score + "% " +
                            getString(R.string.quiz_complete_toast_pt2) + " " + mCheatCounter + " " +
                            getString(R.string.quiz_complete_toast_pt3),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void CheckCheatCount() {
        if (mCheatCounter >= 3)
            mShowAnswerButton.setEnabled(false);
        else
            mShowAnswerButton.setEnabled(true);
    }

    private void SetYesNoButtonEnabled(boolean enabled) {
        true_button.setEnabled(enabled);
        false_button.setEnabled(enabled);
    }
}
