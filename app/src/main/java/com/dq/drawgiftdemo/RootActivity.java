package com.dq.drawgiftdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        findViewById(R.id.to_dialog_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(RootActivity.this,LiveActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.to_decorView_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication(), "你看懂上面的demo，这个就太简单了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}