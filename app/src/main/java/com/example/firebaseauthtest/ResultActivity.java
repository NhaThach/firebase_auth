package com.example.firebaseauthtest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {

    private TextView    tvResult, tvUser;
    private Button      btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        tvResult = findViewById(R.id.tv_result);
        tvUser  = findViewById(R.id.tv_user);
        btnBack = findViewById(R.id.btn_back);

        if (getIntent() != null) {
            if (getIntent().hasExtra("RESULT")) {
                tvResult.setText(getIntent().getStringExtra("RESULT"));
            }
            if (getIntent().hasExtra("USER")) {
                tvUser.setText("Email: "+getIntent().getStringExtra("USER"));
            }
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, SignUpActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}