package com.ty.socketpractice4a;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnExample1;
    private Button mBtnExample2;
    private Button mBtnExample3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mBtnExample1 = findViewById(R.id.main_btn_ex1);
        mBtnExample1.setOnClickListener(this);

        mBtnExample2 = findViewById(R.id.main_btn_ex2);
        mBtnExample2.setOnClickListener(this);

        mBtnExample3 = findViewById(R.id.main_btn_ex3);
        mBtnExample3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.main_btn_ex1) {
            startActivity(new Intent(this, Example1Activity.class));
        } else if (v.getId() == R.id.main_btn_ex2) {
            startActivity(new Intent(this, Example2Activity.class));
        } else if (v.getId() == R.id.main_btn_ex3) {
            startActivity(new Intent(this, Example3Activity.class));
        }
    }
}