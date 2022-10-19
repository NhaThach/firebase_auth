package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edEmail, edPassword;
    private Button btnLogin, btnBack;
    private ImageView ivGoogleLogin;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase connection
    private FirebaseFirestore db;

    private CollectionReference collectionReference;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void initView() {
        edEmail         = findViewById(R.id.ed_email);
        edPassword      = findViewById(R.id.ed_password);
        btnLogin        = findViewById(R.id.btn_login);
        btnBack         = findViewById(R.id.btn_back);
        ivGoogleLogin   = findViewById(R.id.iv_google);

        btnLogin.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ivGoogleLogin.setOnClickListener(this);

        initFirebase();
    }

    private void initFirebase() {
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

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
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

    private void reqLoginWithGoogleAccount() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 100) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                task.getResult(ApiException.class);
                task.getResult().getIdToken();

                Log.d(">>>", "Get ID Token: " + task.getResult().getIdToken());

                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }

            }
        } catch (ApiException e) {
            e.printStackTrace();
            Toast.makeText(this, "Login Fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null) {
                        Toast.makeText(LoginActivity.this, "User: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        Log.d(">>>", "User Id: " + user.getUid());
                        Log.d(">>>", "User Email: " + user.getEmail());
                        Log.d(">>>", "User Phone: " + user.getPhoneNumber());
                        Log.d(">>>", "User Name: " + user.getDisplayName());
                        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                            @Override
                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                if (task.isSuccessful()) {
                                    task.getResult();
                                    Log.d(">>>", "User Auth Token: " + task.getResult().getToken());
                                }
                            }
                        });
                    }

                }).addOnFailureListener(this, e -> Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_login:
                    reqLoginWithFirebase();
                    break;
                case R.id.iv_google:
                    reqLoginWithGoogleAccount();
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