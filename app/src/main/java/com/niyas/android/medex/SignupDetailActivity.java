package com.niyas.android.medex;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class SignupDetailActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "SignUpDetailActivity";
    private boolean mobileOk = true;
    private boolean emailOk = true;
    private boolean groupOk = false;
    private boolean districtOk = false;

    List<String> districts = Arrays.asList("District", "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod", "Kollam", "Kottayam", "Kozhikode",
            "Malappuram", "Palakkad", "Pathanamthitta", "Thrissur", "Wayanad", "Trivandrum");

    List<String> groups = Arrays.asList("Blood Group", "A+ve", "A-ve", "B+ve", "B-ve", "O+ve", "O-ve", "AB-ve", "AB+ve");

    String personName;
    String personEmail;
    String personId;
    String personMobile;
    String personDistrict;
    String personBloodGroup;
    Uri personPhoto;

    ImageView circleImageView;
    EditText userName;
    EditText userEmail;
    EditText userMobile;
    Spinner userDistrict;
    Spinner userBloodGroup;

    String district;
    String group;
    String source;

    Button mSignUp;

    FirebaseFirestore db;
    Map<String, Object> user;

    FirebaseAuth mAuth;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.signup_detail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            source = (String) bundle.get("source");
            Log.d(TAG, " " + source);
        }
        final Typeface raleway_regular = Typeface.createFromAsset(this.getAssets(),"fonts/Raleway-Regular.ttf" );

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        userName = findViewById(R.id.username);
        userEmail = findViewById(R.id.email);
        userMobile = findViewById(R.id.mobileno);
        userDistrict = findViewById(R.id.placeLayout);
        userBloodGroup = findViewById(R.id.groupLayout);
        circleImageView = findViewById(R.id.circleImageView);
        mSignUp = findViewById(R.id.sign_up);
        progressBar = findViewById(R.id.progressbar);

        userDistrict.setOnItemSelectedListener(this);
        userBloodGroup.setOnItemSelectedListener(this);

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, districts){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                ((TextView) v).setTypeface(raleway_regular);
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if(position == 0)
                {
                    //Future update
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };

        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userDistrict.setAdapter(districtAdapter);

        ArrayAdapter<String> groupAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, groups){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v =  super.getView(position, convertView, parent);
                ((TextView) v).setTypeface(raleway_regular);
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if(position == 0)
                {
                    //Future update
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
        };

        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userBloodGroup.setAdapter(groupAdapter);

        progressBar.setVisibility(View.INVISIBLE);

        userName.setTypeface(raleway_regular);
        userEmail.setTypeface(raleway_regular);
        userMobile.setTypeface(raleway_regular);
        mSignUp.setTypeface(raleway_regular);

        mSignUp.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        try {
            if(currentUser != null) {
                if(currentUser.getUid() == null) {
                    Intent intent = new Intent(SignupDetailActivity.this, SignupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Log.d(TAG, currentUser.getUid());
                    personId = currentUser.getUid();
                }
                if(currentUser.getDisplayName() != null) {
                    userName.setText(currentUser.getDisplayName());
                    personName = currentUser.getDisplayName();
                }

                if (currentUser.getEmail() != null) {
                    userEmail.setText(currentUser.getEmail());
                    userEmail.setFocusable(false);
                }
                personPhoto = currentUser.getPhotoUrl();
                if(personPhoto == null) {
                    Picasso.get().load("https://picsum.photos/100/100/?random").into(circleImageView);
                } else {
                    Picasso.get().load(currentUser.getPhotoUrl()).into(circleImageView);
                }
            } else {
                Intent intent = new Intent(SignupDetailActivity.this, SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, TAG, e.getMessage());
            Crashlytics.logException(e);
            Intent intent = new Intent(SignupDetailActivity.this, SignupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserDataToFireStore() {

        progressBar.setVisibility(View.VISIBLE);

        personEmail = userEmail.getText().toString().trim();
        personMobile = userMobile.getText().toString().trim();
        personBloodGroup = group;
        personDistrict = district;

        if(personMobile.length() != 10)
        {
            Log.d(TAG, "Mobile not ok" + mobileOk);
            mobileOk = false;
        } else {
            mobileOk = true;
        }

        if(!validEmail(personEmail))
        {
            emailOk = false;
        } else {
            emailOk = true;
        }


        if(!personDistrict.equals("District")){
            districtOk = true;
        } else {
            districtOk = false;
        }

        if(!personBloodGroup.equals("Blood Group")) {
            groupOk = true;
        } else {
            groupOk = false;
        }

        if(mobileOk && emailOk && districtOk && groupOk)
        {
            try{
                user = new HashMap<>();
                personId = mAuth.getCurrentUser().getUid();
                if(personId == null) {
                    Intent intent = new Intent(SignupDetailActivity.this, SignupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                user.put("id", personId);
                user.put("name", personName);
                user.put("mobile", personMobile);
                user.put("email", personEmail);
                user.put("district", personDistrict);
                user.put("blood", personBloodGroup);
                if(personPhoto == null){
                    user.put("pic", "https://picsum.photos/100/100/?random");
                } else {
                    user.put("pic", personPhoto.toString());
                }
                user.put("source",source);
            } catch (Exception e) {
                Crashlytics.log(Log.ERROR, TAG, e.getMessage());
            }

            db.collection("users").
                    whereEqualTo("id", user.get("id"))
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if(querySnapshot.isEmpty()) {
                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            db.collection("users").document(documentReference.getId())
                                                    .update("userId", documentReference.getId())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            Intent intent = new Intent(SignupDetailActivity.this, HomeActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error adding document id", e);
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            mSignUp.setBackgroundResource(R.drawable.button_text_color);
                                                            Toast.makeText(SignupDetailActivity.this, "Sign up Failed, Try Again", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                            progressBar.setVisibility(View.INVISIBLE);
                                            mSignUp.setBackgroundResource(R.drawable.button_text_color);
                                            Toast.makeText(SignupDetailActivity.this, "Sign up Failed, Try Again", Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SignupDetailActivity.this, "User data existed", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(SignupDetailActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
        else if (!mobileOk)
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Enter 10 digit mobile number", Toast.LENGTH_SHORT).show();
        }
        else if (!emailOk)
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
        }
        else if (!districtOk)
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, personDistrict + " is not accepted", Toast.LENGTH_SHORT).show();
        }
        else if (!groupOk)
        {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, personBloodGroup + " is not accepted", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_up:
                addUserDataToFireStore();
                break;
        }
    }

    private boolean validEmail(String email) {
        Log.d(TAG, email);
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId())
        {
            case R.id.placeLayout:
                district = parent.getItemAtPosition(position).toString().trim();
                break;
            case R.id.groupLayout:
                group = parent.getItemAtPosition(position).toString().trim();
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
