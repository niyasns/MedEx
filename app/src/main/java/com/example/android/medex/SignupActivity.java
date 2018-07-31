package com.example.android.medex;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private final static int RC_SIGN_IN = 9001;
    private static final String TAG = "SignUpActivity";
    private static boolean registeredUser= false;

    GoogleSignInClient mGoogleSignInClient;

    ConnectivityManager conMgr;
    NetworkInfo activeNetwork;
    Button mGSignUpButton;
    Button mFSignUpButton;

    TextView mSubTitleOne;
    TextView mSubTitleTwo;

    ProgressBar progressBar;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_activity);

        mGSignUpButton = findViewById(R.id.g_signup);
        mFSignUpButton = findViewById(R.id.f_signup);

        mSubTitleOne = findViewById(R.id.subTitleOne);
        mSubTitleTwo = findViewById(R.id.subTitleTwo);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        mSubTitleOne.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Raleway-Bold.ttf"));
        mSubTitleTwo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf"));

        mFSignUpButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf"));
        mGSignUpButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf"));

        mGSignUpButton.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        alertIfSignedUpAlready(currentUser);
    }

    private void alertIfSignedUpAlready(FirebaseUser currentUser) {

        if(currentUser != null)
        {
            mGSignUpButton.setText(getResources().getString(R.string.login_google));
            Toast.makeText(this, currentUser.getDisplayName() + " already signed up with google", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.g_signup:
                conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetwork = conMgr.getActiveNetworkInfo();
                if(activeNetwork != null && activeNetwork.isConnected())
                {
                    gSignUp();
                }
                else
                {
                    Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
                }
                break;
                
            case R.id.f_signup:
                conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetwork = conMgr.getActiveNetworkInfo();
                if(activeNetwork != null && activeNetwork.isConnected())
                {
                    fSignUp();
                }
                else
                {
                    Toast.makeText(this, "Please connect to internet", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void fSignUp() {
    }

    private void gSignUp() {
        progressBar.setVisibility(View.VISIBLE);
        mGSignUpButton.setBackgroundResource(R.drawable.rounded_button_home_onclick);
        mGSignUpButton.setTextColor(R.drawable.button_text_color_home_onclick);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = task.getResult();
                firebaseAuthWithGoogle(account);
            }catch (Exception e) {
                Log.w(TAG, "Google Sign in failed: signInResult:failed code = " + e);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }

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
                            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "New User found");
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(SignupActivity.this, SignupDetailActivity.class);
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
            mGSignUpButton.setBackgroundResource(R.drawable.rounded_button_home);
            mGSignUpButton.setTextColor(getResources().getColor(R.color.colorTransparentWhite));
        }

    }

}
