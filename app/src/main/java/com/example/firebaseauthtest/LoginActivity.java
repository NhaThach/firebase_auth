package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edEmail, edPassword;
    private Button btnLogin, btnBack;

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
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        edEmail     = findViewById(R.id.ed_email);
        edPassword  = findViewById(R.id.ed_password);
        btnLogin    = findViewById(R.id.btn_login);
        btnBack     = findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        firebaseAuth        = FirebaseAuth.getInstance();
        db                  = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Users");
    }

    private void reqLoginWithFirebase() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        if (user != null) {
                            final String currentUserId = user.getUid();
                            Toast.makeText(LoginActivity.this, "User: "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            Log.d(">>>", "User Id: "+user.getUid());
                            Log.d(">>>", "User Email: "+user.getEmail());
                            Log.d(">>>", "User Phone: "+user.getPhoneNumber());
                            Log.d(">>>", "User Name: "+user.getDisplayName());
                            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        task.getResult();
                                        Log.d(">>>", "User Auth Token: "+task.getResult().getToken());
                                    }
                                }
                            });

                            // Not necessary
//                            collectionReference.whereEqualTo("userId", currentUserId)
//                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                                            if (error != null) {
//
//                                            }
//                                            if (value != null && !value.isEmpty()) {
//                                                // Getting all QueryDocSnapshots
//                                                for (QueryDocumentSnapshot snapshot : value) {
//                                                    String emailUser = snapshot.getString("email");
//                                                    // Move to Result Activity after registering user successfully
//                                                    Intent intentResult = new Intent(LoginActivity.this, ResultActivity.class);
//                                                    intentResult.putExtra("RESULT", "Login Successfully");
//                                                    intentResult.putExtra("USER", emailUser);
//                                                    startActivity(intentResult);
//                                                }
//                                            }
//                                        }
//                                    });
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Email or Password is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_login:
                    reqLoginWithFirebase();
                    break;
                case R.id.btn_back:
                    Intent intentBack = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intentBack);
                    finishAffinity();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}