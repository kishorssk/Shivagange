package com.srirangadigital.shivagange;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class About extends AppCompatActivity {

    MediaPlayer hhSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        hhSpeech = MediaPlayer.create(this, R.raw.hhsriswamiji);

    }

    public void playIT(View v){

        hhSpeech.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hhSpeech.release();
    }
}
