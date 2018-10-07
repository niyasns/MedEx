package com.niyas.android.medex;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import io.fabric.sdk.android.Fabric;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private LinearLayout mDotLayout;
    private Button loginPageButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    /**
     * variable to limit firebase settings to execute once.
     */
    private static boolean isfirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        initFirebase();
        initCalligraphy();
        initView();
        loginPageButton.setOnClickListener(this);
    }
    /*
    Setting up components for the activity.
     */
    private void initView() {
        /*
        Setting "LOGIN" button with raleway font.
         */
        loginPageButton = findViewById(R.id.view_pager_signup);
        Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );
        loginPageButton.setTypeface(raleway_regular);
        /*
        Setting progress bar and making it invisible.
         */
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        /*
        Setting view pager
         */
        ViewPager mSlideViewPager = findViewById(R.id.view_pager);
        mDotLayout = findViewById(R.id.dots_layout);
        SliderAdapter sliderAdapter = new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        addDotsIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);
    }
    /*
     Initializing Calligraphy font library for raleway font.
    */
    private void initCalligraphy() {

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Raleway-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    /**
     * Initializing Firebase
     */
    private void initFirebase() {

        db = FirebaseFirestore.getInstance();
        if(isfirst) {
            Crashlytics.log(Log.DEBUG, TAG, "Initial app loading: FirebaseSettings initialized");
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
            isfirst = false;
        } else {
            Crashlytics.log(Log.DEBUG, TAG, "FirebaseSettings initialization skipped");
        }
        mAuth = FirebaseAuth.getInstance();
    }
    /*
    Checking external storage permissions for downloading files.
     */
    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                loginAfterPermission();
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted as per sdk < 23");
            loginAfterPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loginAfterPermission();
                } else {
                    loginPageButton.setBackgroundResource(R.drawable.rounded_button);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Please allow read and write permissions", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    /** Login function after checking storage permissions **/
    public void loginAfterPermission() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_pager_signup:
                progressBar.setVisibility(View.VISIBLE);
                loginPageButton.setBackgroundResource(R.drawable.rounded_button_home_onclick);
                isStoragePermissionGranted();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /*
    Calligarphy font library stuff
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    /**
     * Adding dots indicator below view pager.
     * @param position : current slide in view pager.
     */
    public void addDotsIndicator(int position) {

        TextView[] mDots = new TextView[3];
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

        mDots[position].setTextColor(getResources().getColor(R.color.colorWhite));
    }

    /**
     * Listener for view pager changes.
     */
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

    /**
     * Updating view based on user signed in or not.
     * If an user is already signed in, clicking login button directs to
     * the HomeActivity without login again. If there is no user currently
     * signed in login button directs to Login page.
     * @param user :firebase currently logged in.
     */
    private void updateUI(FirebaseUser user) {

        if(user != null)
        {
            CollectionReference usersReference = db.collection("users");
            Query query = usersReference.whereEqualTo("email", user.getEmail());
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
                            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Log.d(TAG, "New user check get failed with ", task.getException());
                    }
                }
            });

        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show();
        }
    }
}
