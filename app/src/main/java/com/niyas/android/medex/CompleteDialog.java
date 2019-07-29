package com.niyas.android.medex;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.annotation.Nullable;

public class CompleteDialog extends Dialog implements View.OnClickListener {

    /* Dailog for quiz completion */
    private Activity mActivity;
    private Button close;
    private LinearLayout prizeLayout;
    private TextView textWinner;
    private TextView totalPrize;
    private TextView totalWinners;
    private TextView reward;
    private TextView winnerEqual;
    private TextView winnerSlash;
    private TextView winnerStatus;
    private TextView loading;
    ProgressBar progressBar;
    private String TAG = "CompleteDialog";
    private List<String> winnerArray;
    private Long winnerCount;
    private String quizId;
    private Long prizeMoney;
    private Long winnerReward;


    public CompleteDialog(@NonNull Activity activity, String quizId, Long prizeMoney) {
        super(activity);
        this.mActivity = activity;
        this.quizId = quizId;
        this.prizeMoney = prizeMoney;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivity.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.complete_dialog);


        Typeface raleway_bold = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(mActivity.getAssets(),"fonts/Raleway-Regular.ttf" );

        prizeLayout = (LinearLayout) findViewById(R.id.prize_layout);
        textWinner = findViewById(R.id.text_winner);
        totalPrize = findViewById(R.id.total_prize);
        totalWinners = findViewById(R.id.total_winners);
        reward = findViewById(R.id.reward);
        winnerEqual = findViewById(R.id.winner_equal);
        winnerSlash = findViewById(R.id.winner_slash);
        winnerStatus = findViewById(R.id.winner_status);
        close = findViewById(R.id.btn_close_oncomplete);
        progressBar = findViewById(R.id.progressbarDialog);
        loading = findViewById(R.id.text_loading);
        close.setOnClickListener(this);
        close.setEnabled(false);

        loading.setText("Loading...");
        loading.setTypeface(raleway_bold);

        textWinner.setTypeface(raleway_bold);
        totalPrize.setTypeface(raleway_bold);
        totalWinners.setTypeface(raleway_bold);
        reward.setTypeface(raleway_bold);
        winnerEqual.setTypeface(raleway_bold);
        winnerSlash.setTypeface(raleway_bold);
        winnerStatus.setTypeface(raleway_regular);
        close.setTypeface(raleway_bold);

        prizeLayout.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(progressDrawable);

        TextPaint paint = textWinner.getPaint();
        float width = paint.measureText("WINNER");

        Shader textShader = new LinearGradient(0, 0, width, textWinner.getTextSize(),
                new int[]{
                        Color.parseColor("#FF4B2B"),
                        Color.parseColor("#FF416C"),
                }, null, Shader.TileMode.CLAMP);
        textWinner.getPaint().setShader(textShader);
        textWinner.setTextColor(Color.parseColor("#FF4B2B"));


        CountDownTimer countDownTimer = new CountDownTimer(15000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "seconds remaining: " + millisUntilFinished / 1000);
                long secRemain = millisUntilFinished /1000;
                int progress = (int) ((secRemain * 100) / 15);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                firebaseResults();
            }
        };

        countDownTimer.start();
    }

    @Override
    public void onClick(View v) {
        /* Dismiss dailog and replace fragment */
        dismiss();
        //mActivity.stopService(new Intent(mActivity, BackgroundSoundService.class));
        ((AppCompatActivity)mActivity).getSupportFragmentManager().popBackStackImmediate();
        android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity)mActivity).getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction  fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_window, new HomeFragment());
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    public void firebaseResults() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference resultRef = db.collection("results");

        resultRef.whereEqualTo("quizId", quizId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()) {
                            try{
                                QuerySnapshot snapshot = task.getResult();
                                DocumentSnapshot DocumentSnapshot = snapshot.getDocuments().get(0);
                                Crashlytics.log(Log.DEBUG, TAG + ": task results", task.getResult().toString());
                                winnerArray = (List<String>) DocumentSnapshot.get("winners");
                                if(winnerArray != null){
                                    loading.setVisibility(View.GONE);
                                    prizeLayout.setVisibility(View.VISIBLE);
                                    winnerCount = (long) winnerArray.size();
                                    try{
                                        winnerReward = prizeMoney / winnerCount;
                                        totalWinners.setText(winnerCount.toString());
                                        totalPrize.setText(prizeMoney.toString());
                                        reward.setText(winnerReward.toString());
                                    } catch (Exception e) {
                                        Toast.makeText(mActivity, "Error in data received", Toast.LENGTH_SHORT).show();
                                        Crashlytics.log(Log.ERROR, TAG + ": settings results", e.getMessage());
                                    }
                                    close.setEnabled(true);
                                } else {
                                    Toast.makeText(getContext(), "Failed to access winners data",Toast.LENGTH_SHORT).show();
                                    Crashlytics.log(Log.ERROR, TAG + ": settings results", "winnerArray is null");
                                    close.setEnabled(true);
                                }
                            } catch (Exception e) {
                                close.setEnabled(true);
                                Crashlytics.log(Log.ERROR, TAG + ": task result error", e.getMessage());
                            }
                        } else {
                            close.setEnabled(true);
                            Crashlytics.log(Log.ERROR, TAG + ": result loading", "Error getting documents : " + task.getException());
                        }
                    }
                });
    }
}
