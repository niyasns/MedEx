package com.example.android.medex;

import android.app.DownloadManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "SignUpActivity";
    private static boolean registeredUser= false;

    GoogleSignInClient mGoogleSignInClient;
    Button mGSignUpButton;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);

        mGSignUpButton = findViewById(R.id.g_signup);

        db = FirebaseFirestore.getInstance();

        mGSignUpButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        alertIfSignedUpAlready(account);
    }

    private void alertIfSignedUpAlready(GoogleSignInAccount account) {

        if(account != null)
        {
            Toast.makeText(this, "Already Signed up with Google", Toast.LENGTH_LONG).show();
            mGSignUpButton.setText(getResources().getString(R.string.login_google));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.g_signup:
                gSignUp();
                break;
                
            case R.id.f_signup:
                fSignUp();
                break;
        }
    }

    private void fSignUp() {
    }

    private void gSignUp() {
        mGSignUpButton.setBackgroundResource(R.drawable.button_text_color_onclick);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try{
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            UserLoggedIn(account);

        }catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code = " + e.getStatusCode());
            UserLoggedIn(null);
        }
    }

    private void UserLoggedIn(GoogleSignInAccount account) {

        if(account != null) {


            CollectionReference usersReference = db.collection("users");
            Query query = usersReference.whereEqualTo("token", account.getIdToken());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful())
                    {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(querySnapshot.isEmpty()) {
                            Log.d(TAG,"New user detected : " + querySnapshot.getDocuments());
                            registeredUser = false;
                        } else {
                            registeredUser = true;
                        }
                    } else {
                        Log.d(TAG, "User query failed", task.getException());
                    }
                }
            });

            if(!registeredUser)
            {
                Intent intent = new Intent(SignupActivity.this, SignupDetailActivity.class);
                intent.putExtra("account", account);
                startActivity(intent);
            }
            else if(registeredUser)
            {
                Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                intent.putExtra("token", account.getIdToken());
                startActivity(intent);
            }

        } else {

            Toast.makeText(this, "Google authentication failed", Toast.LENGTH_LONG).show();
            mGSignUpButton.setBackgroundResource(R.drawable.button_text_color);
        }

    }
}
