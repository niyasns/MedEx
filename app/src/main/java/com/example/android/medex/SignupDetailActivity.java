package com.example.android.medex;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpDetailActivity";
    String personName;
    String personEmail;
    String personId;
    String personToken;
    String personMobile;
    String personDistrict;
    String personBloodGroup;
    Uri personPhoto;

    CircleImageView circleImageView;
    EditText userName;
    EditText userEmail;
    EditText userMobile;
    EditText userDistrict;
    EditText userBloodGroup;

    Button mSignUp;

    FirebaseFirestore db;
    Map<String, Object> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_detail);

        Intent intent = getIntent();
        GoogleSignInAccount account = (GoogleSignInAccount) intent.getSerializableExtra("account");
        extractData(account);

        mSignUp.setOnClickListener(this);

        userName = findViewById(R.id.username);
        userEmail = findViewById(R.id.email);
        userMobile = findViewById(R.id.mobileno);
        userDistrict = findViewById(R.id.district);
        userBloodGroup = findViewById(R.id.bloodgroup);

        userName.setText(personName);
        userEmail.setText(personEmail);
        circleImageView.setImageURI(personPhoto);

        db = FirebaseFirestore.getInstance();
    }

    private void addUserDataToFireStore() {

        mSignUp.setBackgroundResource(R.drawable.button_text_color_onclick);

        personMobile = userMobile.getText().toString();
        personBloodGroup = userBloodGroup.getText().toString();
        personDistrict = userDistrict.getText().toString();

        user = new HashMap<>();
        user.put("token", personToken);
        user.put("id", personId);
        user.put("name", personName);
        user.put("mobile", personMobile);
        user.put("email", personEmail);
        user.put("district", personDistrict);
        user.put("blood", personBloodGroup);
        user.put("pic", personPhoto.toString());

        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Intent intent = new Intent(SignupDetailActivity.this, HomeActivity.class);
                        intent.putExtra("docId", documentReference.getId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        mSignUp.setBackgroundResource(R.drawable.button_text_color);
                        Toast.makeText(SignupDetailActivity.this, "Sign up Failed, Try Again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void extractData(GoogleSignInAccount account) {

        personName = account.getDisplayName();
        personEmail = account.getEmail();
        personId = account.getId();
        personToken = account.getIdToken();
        personPhoto = account.getPhotoUrl();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_up:
                addUserDataToFireStore();
                break;
        }
    }
}
