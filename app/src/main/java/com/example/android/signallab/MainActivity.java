package com.example.android.signallab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {
    private Button vidButton;
    private Button infoButton;
    /** Initialize the activity. Defining layout for the Main activity view and creating the buttons needed */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vidButton = findViewById(R.id.mVideoButton);
        vidButton.setOnClickListener(new OnClickListener() {
            /**Response to button click of vidButton takes user to video activity view */
            @Override
            public void onClick(View v) {
                Intent startRecordActivityIntent = new Intent(MainActivity.this,
                        VideoActivity.class);
                startActivity(startRecordActivityIntent);
            }
        });
        infoButton = findViewById(R.id.mInfoButton);
        infoButton.setOnClickListener(new OnClickListener() {
            /**Response to button click of infobutton takes user to info activity view */
            @Override
            public void onClick(View v) {
                Intent infoActivity = new Intent(MainActivity.this,
                        infoActivity.class);
                startActivity(infoActivity);
            }
        });

    }
}
