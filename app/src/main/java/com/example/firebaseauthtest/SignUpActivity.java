package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText    edEmail, edPassword;
    private Button      btnSignUp, btnLogin;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase connection
    private FirebaseFirestore db;

    private CollectionReference collectionReference;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void initView() {
        edEmail             = findViewById(R.id.ed_email);
        edPassword          = findViewById(R.id.ed_password);
        btnSignUp           = findViewById(R.id.btn_sign_up);
        btnLogin            = findViewById(R.id.btn_login);

        btnSignUp.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        firebaseAuth        = FirebaseAuth.getInstance();
        db                  = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");

        // Authentication
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    // User Already Logged In
                } else {
                    // No user yet
                }
            }
        };
    }

    private void reqSignUpWithFirebase() {
        String email    = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        addNewUserToFirebaseDb(email);
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Email or Password is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void addNewUserToFirebaseDb(String email) {
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            final String currentUserId = currentUser.getUid();

            // Create a userMap so we can create a user in the Users Collection in Firestore
            Map<String, String> userObj = new HashMap<>();
            userObj.put("userId", currentUserId);
            userObj.put("email", email);

            // Adding user to Firestore
            collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    documentReference.get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (Objects.requireNonNull(task.getResult().exists())) {
                                String emailUser = task.getResult().getString("email");

                                // Move to Result Activity after registering user successfully
                                Intent intentResult = new Intent(SignUpActivity.this, ResultActivity.class);
                                intentResult.putExtra("RESULT", "Sign Up Successfully");
                                intentResult.putExtra("USER", emailUser);
                                startActivity(intentResult);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_sign_up:
                    reqSignUpWithFirebase();
                    break;
                case R.id.btn_login:
                    Intent intentLogin = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intentLogin);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}