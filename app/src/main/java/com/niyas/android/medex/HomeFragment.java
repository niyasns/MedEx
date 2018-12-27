package com.niyas.android.medex;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction ;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.special.ResideMenu.ResideMenu;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.invoke.ConstantCallSite;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class HomeFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private View parentView;
    private ResideMenu resideMenu;
    HomeActivity parentActivity;
    CardView quiz;
    CardView module;
    CardView Profile;
    CardView Videos;
    TextView heading;
    TextView quizText, profileText, moduleText, videosText;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    static HashMap<String, String> tipOfTheDay = new HashMap<>();
    TextView userWish, tipHead, tipBody, tipAuthor, userWishName;
    ProgressBar progressBar;
    ConstraintLayout tipLayout;
    private String TAG = "HomeFragment";
    static boolean isInitial = true;
    static Bitmap bitmapCache;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_home_test, container, false);
        setupViews();
        setupFirebase();
        setupWish();
        setupTipOfTheDay();
        return parentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupWish() {

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String wish = "";

        if(timeOfDay >= 0 && timeOfDay < 12) {
            wish = "Good Morning";
            userWish.setText(wish);
            loadImage(R.drawable.cover1);
        } else if(timeOfDay >= 12 && timeOfDay < 16) {
            wish = "Good Afternoon";
            userWish.setText(wish);
            loadImage(R.drawable.cover4);
        } else if(timeOfDay >= 16 && timeOfDay < 21) {
            wish = "Good Evening";
            userWish.setText(wish);
            loadImage(R.drawable.cover2);
        } else if(timeOfDay >= 21 && timeOfDay < 24) {
            wish = "Good Night";
            userWish.setText(wish);
            loadImage(R.drawable.cover3);
        }

        userWishName.setText(getUserName());
    }

    private String getUserName(){

        if(firebaseUser != null) {
            return firebaseUser.getDisplayName();
        } else {
            return "";
        }
    }

    private void setupTipOfTheDay() {

        if(tipOfTheDay.get("title") == null){
            new getTipOfTheDay().execute();
        } else {
            Log.d(TAG, "not isInitial");
            tipHead.setText(tipOfTheDay.get("title"));
            tipBody.setText(tipOfTheDay.get("quote"));
            String author = "-- " + tipOfTheDay.get("author") + " --";
            tipAuthor.setText(author);
        }
    }

    private void loadImage(int cover) {

        tipLayout.setBackgroundResource(cover);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class getTipOfTheDay extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String url = "http://quotes.rest/qod.json";
            String jsonStr = sh.makeServiceCall(url);
            Log.d(TAG, "Response from URL: " + jsonStr);

            if(jsonStr != null) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    JSONObject jsonContent = jsonObject.getJSONObject("contents");
                    JSONArray jsonQuotes = jsonContent.getJSONArray("quotes");

                    for(int i = 0; i < jsonQuotes.length(); i++) {
                        JSONObject c = jsonQuotes.getJSONObject(i);
                        String quote = c.getString("quote");
                        String author = c.getString("author");
                        String image = c.getString("background");
                        String title = c.getString("title");
                        String length = c.getString("length");

                        tipOfTheDay.put("quote", quote);
                        tipOfTheDay.put("author", author);
                        tipOfTheDay.put("image", image);
                        tipOfTheDay.put("title", title);
                        tipOfTheDay.put("length", length);

                    }

                } catch (JSONException e) {
                    Log.e(TAG, "JSONException " + e.getMessage());
                }
            } else {
                Log.e(TAG, "JSON string null");
            }
            Integer length = 0;
            if(tipOfTheDay.get("length") != null){
                length = Integer.parseInt(tipOfTheDay.get("length"));
            }
            if(tipOfTheDay.size() == 0 || length > 250) {
                tipOfTheDay.put("quote", "If we’re not stretching, we’re not growing. If we’re not growing, we’re probably " +
                        "not fulfilling our potential. The only person in this room that knows your potential is you.");
                tipOfTheDay.put("author", "Kevin Turner");
                tipOfTheDay.put("image", "https://theysaidso.com/img/bgs/man_on_the_mountain.jpg");
                tipOfTheDay.put("title", "Inspiring Quote of the day");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            isInitial = false;
            tipHead.setText(tipOfTheDay.get("title"));
            tipBody.setText(tipOfTheDay.get("quote"));
            String author = "-- " + tipOfTheDay.get("author") + " --";
            tipAuthor.setText(author);
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    private void setupViews() {

        Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );

        parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);
        heading = parentActivity.findViewById(R.id.heading);
        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        quiz = parentView.findViewById(R.id.quiz);
        module = parentView.findViewById(R.id.modules);
        Profile = parentView.findViewById(R.id.profile);
        Videos = parentView.findViewById(R.id.videos);
        quizText = parentView.findViewById(R.id.quizText);
        profileText = parentView.findViewById(R.id.profileText);
        videosText = parentView.findViewById(R.id.videosText);
        moduleText  =parentView.findViewById(R.id.modulesText);
        userWish = parentView.findViewById(R.id.user_wish);
        userWishName = parentView.findViewById(R.id.user_wish_name);
        tipHead = parentView.findViewById(R.id.tip_head);
        tipBody = parentView.findViewById(R.id.tip_body);
        tipAuthor = parentView.findViewById(R.id.tip_author);
        tipLayout = (ConstraintLayout) parentView.findViewById(R.id.tip_Layout);
        parentActivity.stopService(new Intent(parentActivity, BackgroundSoundService.class));

        heading.setText(R.string.home);
        quizText.setTypeface(raleway_bold);
        moduleText.setTypeface(raleway_bold);
        profileText.setTypeface(raleway_bold);
        videosText.setTypeface(raleway_bold);
        userWish.setTypeface(raleway_bold);
        userWishName.setTypeface(raleway_bold);
        tipHead.setTypeface(raleway_bold);
        tipBody.setTypeface(raleway_regular);
        tipAuthor.setTypeface(raleway_regular);

        quiz.setOnClickListener(this);
        module.setOnClickListener(this);
        Profile.setOnClickListener(this);
        Videos.setOnClickListener(this);

        resideMenu = parentActivity.getResideMenu();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quiz:
                changeFragment(new CountDownFragment());
                break;
            case R.id.modules:
                changeFragment(new ModuleFragment());
                break;
            case R.id.profile:
                changeFragment(new ProfileFragment());
                break;
            case R.id.videos:
                changeFragment(new VideosFragment());
                break;
        }
    }

    private void changeFragment(android.support.v4.app.Fragment targetFragment) {

        android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransitionStyle(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.replace(R.id.frame_window, targetFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
