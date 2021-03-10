package com.example.android.signallab;


import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


public class VideoActivity extends AppCompatActivity {
    Camera mCamera;
    CameraPreview mPreview;
    ImageView convertedImageView;
    LinearLayout layoutForImage;
    FrameLayout preview;
    private boolean isRecording = false;
    private Button startRecording, stopRecording;
    public ArrayList<double []> mean_values = new ArrayList<>();
    TextView pulse;
    public Handler handler;
    TextView timerView;
    CountDownTimer MyCountDownTimer;

    private int frames = 1;
    private boolean rec = false;
    private long timeStart = 0;
    private long timeStop = 0;
    private long timeElapsed = 0;
    /**Initialize the activity. Defining layout for Video activity view, creating a Camera Preview and buttons for start and stop */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        convertedImageView = new ImageView(this);
        // Get the mobiles camera and set it to our camera object.
        mCamera = getCameraInstance();
        // Create a layout to put our image.
        layoutForImage = findViewById(R.id.ll);
        // Creates our own camera preview object to  be able to make changes to the previews.
        mPreview = new CameraPreview(this, mCamera, convertedImageView,layoutForImage);
        // Add our camerapreview to this activitys layout.
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        //This is done to not show the real preview frame, and only our ImageView.
        preview.setVisibility(View.INVISIBLE);
        pulse = findViewById(R.id.pulseView);
        timerView = findViewById(R.id.timer);
        startRecording = findViewById(R.id.startButton);
        startRecording.setOnClickListener(new View.OnClickListener() {
            /** Response to button click of start Recording button.
             *  Calls the start record method and time method.
             *  Creates handler to schedule new call of both start record, time and timing
             *  with a delay of 10 seconds be between start record and timing. */
            @Override
            public void onClick(View v) {
                if (!isRecording)
                {
                    startRecord();
                    time();
                    handler = new Handler();
                    final int delay = 10000; // 10 sec!

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            timing();
                            startRecord();
                            time();
                            handler.postDelayed(this, delay);
                        }
                    }, delay);
                }

            }
        });
        stopRecording = findViewById(R.id.stopButton);
        stopRecording.setOnClickListener(new View.OnClickListener() {
            /** Response to button click of stop Recording button.
             * Calls the stop recording method. */
            @Override
            public void onClick(View v) {
                if (isRecording)
                {
                    stopRecord();
                }
            }
        });


    }
    /**Count down timer from 10 to 0, lets the user know when next update of pulse will be. */
    private void time(){
        MyCountDownTimer=new CountDownTimer(10000,1000){
            public void onTick(long timeToGo){
                NumberFormat f = new DecimalFormat("00");
                long sec = (timeToGo/1000)%60;
                timerView.setText(f.format(sec));

            }
            public void onFinish(){
                timerView.setText("00");
            }
        }.start();
    }
    /**Starts storing mean values from frames in Camera Preview class. */
    private void startRecord(){
        mCamera.startPreview();
        isRecording=true;
        mPreview.recording();
        timeStart = System.currentTimeMillis();
    }

    /** Stops the storing of mean values and starts the calculation of pulse class. */
    private void timing(){
            isRecording = false;
            mPreview.notRecording();
            timeStop = System.currentTimeMillis();
            mean_values = mPreview.send();
            mCamera.stopPreview();
            timeElapsed = timeStop - timeStart;
            double TE = (double) timeElapsed;
            TE = TE / 1000;
            double samples = (double) mean_values.size();
            long startTime = System.currentTimeMillis();
            CalcThread ct = new CalcThread();
            ct.calc(TE, samples);
            long endTime = System.currentTimeMillis();
            long calcTime = endTime - startTime;
            Log.i("here", "Tid det tar att beräkna: " + calcTime + " ms");
            Log.i("here", "Antal Samples per omgång" + samples + " stycken");

    }
    /** Stops the storing of mean values and the count down timer */
    private void stopRecord(){
        isRecording=false;
        mPreview.notRecording();
        handler.removeCallbacksAndMessages(null);
        MyCountDownTimer.cancel();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // öppna FRAM kamera
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    /**Creates and calls upon the class that performs the calculations.
     * Shows resulting pulse after calculations to user. */
    class CalcThread {
        public void calc(double time, double sampels){

            SignalProcessing s = new SignalProcessing();
            int size = mean_values.size();

            final double[] red = new double[size];
            for(int i = 0; i<size;i++){
                red[i]= mean_values.get(i)[0];

            }

            final double[] green = new double[size];
            for(int i = 0; i<size;i++){
                green[i]= mean_values.get(i)[1];

            }
            final double[] blue = new double[size];
            for(int i = 0; i<size;i++){
                blue[i]= mean_values.get(i)[2];

            }

            double [] frekvenser =s.freq(red,green,blue,time,sampels);
            Log.i("here", "Red:  " + frekvenser[0] + " Green: " + frekvenser[1] + " Blue: " + frekvenser[2]);
            pulse.setText("Heart Rate: " + String.valueOf(Math.round(frekvenser[1])) + " BPM");
        }
    }

}

