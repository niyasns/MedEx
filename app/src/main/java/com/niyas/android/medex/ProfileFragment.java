package com.niyas.android.medex;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.special.ResideMenu.ResideMenu;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProfileFragment extends android.support.v4.app.Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "ProfileFragment";
    private View parentView;
    private ResideMenu resideMenu;
    private TextView heading;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    Button update;
    ImageView circleImageView;
    EditText userName;
    EditText userMobile;
    EditText userEmail;
    Spinner userDistrict;
    Spinner userBloodGroup;

    private String district;
    private String group;
    private String userId;
    private String docId;

    ArrayAdapter<String> districtAdapter;
    ArrayAdapter<String> groupAdapter;
    /* List of districts */
    List<String> districts = Arrays.asList("District", "Alappuzha", "Ernakulam", "Idukki", "Kannur", "Kasaragod", "Kollam", "Kottayam", "Kozhikode",
            "Malappuram", "Palakkad", "Pathanamthitta", "Thrissur", "Wayanad", "Trivandrum");
    /* List of blood groups */
    List<String> groups = Arrays.asList("Blood Group", "A+ve", "A-ve", "B+ve", "B-ve", "O+ve", "O-ve", "AB-ve", "AB+ve");

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_profile, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setupViews();
        setupFirebase();
        readUserDetails();
        return parentView;
    }
    /* Reading user details from firebase */
    private void readUserDetails() {
        userId = mAuth.getUid();
        db.collection("users")
                .whereEqualTo("id", userId)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "User details read complete" + document.getData());
                                progressBar.setVisibility(View.INVISIBLE);
                                userName.setText(document.getString("name"));
                                userMobile.setText(document.getString("mobile"));
                                userEmail.setText(document.getString("email"));
                                district = document.getString("district");
                                group = document.getString("blood");
                                docId = document.getString("userId");
                                int dIndex = districtAdapter.getPosition(district);
                                int bIndex = groupAdapter.getPosition(group);
                                userDistrict.setSelection(dIndex);
                                userBloodGroup.setSelection(bIndex);
                                Picasso.get().load(document.getString("pic")).into(circleImageView);
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void setupFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupViews() {
        Typeface raleway_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        final Typeface raleway_regular = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );
        HomeActivity parentActivity = (HomeActivity) getActivity();
        Button button = parentActivity.findViewById(R.id.menu_button);

        heading = parentActivity.findViewById(R.id.heading);
        heading.setTypeface(raleway_bold);
        heading.setText(R.string.profile);

        progressBar = parentActivity.findViewById(R.id.progressbarHome);
        progressBar.setVisibility(View.VISIBLE);

        update = parentView.findViewById(R.id.edit_user_frag);
        update.setOnClickListener(this);

        userName = parentView.findViewById(R.id.usernameFrag);
        userMobile = parentView.findViewById(R.id.mobilenoFrag);
        userDistrict = parentView.findViewById(R.id.placeLayoutFrag);
        userEmail = parentView.findViewById(R.id.emailFrag);
        userBloodGroup = parentView.findViewById(R.id.groupLayoutFrag);
        circleImageView = parentView.findViewById(R.id.circleImageViewFrag);


        userDistrict.setOnItemSelectedListener(this);
        userBloodGroup.setOnItemSelectedListener(this);

        userEmail.setTypeface(raleway_regular);
        userName.setTypeface(raleway_regular);
        userMobile.setTypeface(raleway_regular);

        districtAdapter = new ArrayAdapter<String>(parentActivity, R.layout.spinner_item, districts){
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

        groupAdapter = new ArrayAdapter<String>(parentActivity, R.layout.spinner_item, groups){
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
        Map<String, Object> userData = new HashMap<>();
        progressBar.setVisibility(View.VISIBLE);
        userData.put("name", userName.getText().toString().trim());
        userData.put("mobile", userMobile.getText().toString().trim());
        userData.put("district", district.trim());
        userData.put("blood", group.trim());

        Log.d(TAG, " " + docId);

        db.collection("users")
                .document(docId)
                .update(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(parentView.getContext(), "User details updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(parentView.getContext(), "User details update failed. Try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId())
        {
            case R.id.placeLayoutFrag:
                district = parent.getItemAtPosition(position).toString().trim();
                Log.d(TAG,"District changed" + district);
                break;
            case R.id.groupLayoutFrag:
                group = parent.getItemAtPosition(position).toString().trim();
                Log.d(TAG, "Group changed" + group);
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
