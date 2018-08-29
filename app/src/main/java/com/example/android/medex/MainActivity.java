package com.example.android.medex;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;
    private TextView[] mDots;
    private SliderAdapter sliderAdapter;
    private Button vSignup;

    private ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        vSignup = (Button) findViewById(R.id.view_pager_signup);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );
        vSignup.setTypeface(raleway_regular);

        mSlideViewPager = (ViewPager) findViewById(R.id.view_pager);
        mDotLayout = (LinearLayout) findViewById(R.id.dots_layout);
        sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        isStoragePermissionGranted();

        vSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                vSignup.setBackgroundResource(R.drawable.rounded_button_home_onclick);
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    updateUI(currentUser);
                }else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    public void addDotsIndicator(int position) {

        mDots = new TextView[3];
        mDotLayout.removeAllViews();

        for(int i = 0; i < mDots.length; i++) {

            mDots[i] = new TextView(this);
            if(Build.VERSION.SDK_INT > 24)
            {
                mDots[i].setText(Html.fromHtml("&#8226", Html.FROM_HTML_MODE_COMPACT));
            } else {
                mDots[i].setText(Html.fromHtml("&#8226"));
            }
            mDots[i].setTextSize(35);
            mDots[i].setPadding(4,1,4,1);
            mDots[i].setTextColor(ContextCompat.getColor(this, R.color.colorTransparentWhite));

            mDotLayout.addView(mDots[i]);
        }

        if(mDots.length > 0)
        {
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            addDotsIndicator(position);

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void updateUI(FirebaseUser user) {

        if(user != null)
        {
            CollectionReference usersReference = db.collection("users");
            Query query = usersReference.whereEqualTo("id", user.getUid());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot queryResult = task.getResult();
                        if (!queryResult.isEmpty()) {
                            Log.d(TAG, "Registered user details " + queryResult.getDocuments());
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "New User found");
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(MainActivity.this, SignupDetailActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "New user check get failed with ", task.getException());
                    }
                }
            });

        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_LONG).show();
        }

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

}
