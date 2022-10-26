package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edEmail, edPassword;
    private Button btnLogin, btnBack, btnSwitchMethod, btnLogout;
    private ImageView ivGoogleLogin, ivSendCode;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firebase connection
    private FirebaseFirestore db;

    private CollectionReference collectionReference;

    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;

    private boolean isPhoneNum;
    private String verificationID;

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
        ivSendCode      = findViewById(R.id.iv_send_code);
        btnSwitchMethod = findViewById(R.id.btn_switch_method);
        btnLogout       = findViewById(R.id.btn_logout);

        btnLogin.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ivGoogleLogin.setOnClickListener(this);
        ivSendCode.setOnClickListener(this);
        btnSwitchMethod.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

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
                            Toast.makeText(LoginActivity.this, "Login Success!\nUser: "+user.getEmail(), Toast.LENGTH_SHORT).show();
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
                            collectionReference.whereEqualTo("userId", user.getUid())
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                            if (error != null) {

                                            }
                                            if (value != null && !value.isEmpty()) {
                                                // Getting all QueryDocSnapshots
                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    String emailUser = snapshot.getString("email");
                                                    // Move to Result Activity after registering user successfully
                                                    Intent intentResult = new Intent(LoginActivity.this, ResultActivity.class);
                                                    intentResult.putExtra("RESULT", "Login Successfully");
                                                    intentResult.putExtra("USER", emailUser);
                                                    startActivity(intentResult);
                                                }
                                            }
                                        }
                                    });
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
                        Toast.makeText(LoginActivity.this, "Login Success!\nUser: " + user.getEmail(), Toast.LENGTH_SHORT).show();
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

    private void sendAuthCode() {
        if (!TextUtils.isEmpty(edEmail.getText().toString().trim())) {
            PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber("+855"+edEmail.getText().toString().trim())
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                        }

                        @Override
                        public void onVerificationFailed(@NonNull FirebaseException e) {
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            super.onCodeSent(s, forceResendingToken);
                            Toast.makeText(LoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();
                            verificationID = s;
                        }

                    }).build();

            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
        }
    }

    private void verifyAuthCodeAndSignIn(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);

        // inside this method we are checking if
        // the code entered is correct or not.
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {

                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null) {
                        Toast.makeText(LoginActivity.this, "Login Success!\nUser: " + user.getPhoneNumber(), Toast.LENGTH_SHORT).show();
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

    private void changeBtnSendCodeColor() {
        ivSendCode.setColorFilter(ContextCompat.getColor(this, R.color.dark_green));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ivSendCode.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.light_green));
            }
        }, 60000);
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_login:
                    if (isPhoneNum) {
                        verifyAuthCodeAndSignIn(edPassword.getText().toString().trim());
                    } else  {
                        reqLoginWithFirebase();
                    }
                    break;
                case R.id.iv_google:
                    reqLoginWithGoogleAccount();
                    break;
                case R.id.btn_back:
                    Intent intentBack = new Intent(LoginActivity.this, SignUpActivity.class);
                    startActivity(intentBack);
                    finishAffinity();
                    break;
                case R.id.btn_switch_method:
                    if (!isPhoneNum) {
                        isPhoneNum = true;
                        edEmail.setHint("Phone Number");
                        edEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                        edPassword.setHint("Verification Code");
                        edPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        edPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                        btnSwitchMethod.setText("Switch To Email");
                        ivSendCode.setVisibility(View.VISIBLE);
                    } else {
                        isPhoneNum = false;
                        edEmail.setHint("Email");
                        edEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        edPassword.setHint("Password");
                        edPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        edPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        btnSwitchMethod.setText("Switch To Phone Number");
                        ivSendCode.setVisibility(View.GONE);
                    }
                    edEmail.setText("");
                    edPassword.setText("");
                    break;
                case R.id.iv_send_code:
                    changeBtnSendCodeColor();
                    sendAuthCode();
                    break;
                case R.id.btn_logout:
                    firebaseAuth.signOut();
                    gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(LoginActivity.this, "Logged Out!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}