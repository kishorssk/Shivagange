package com.srirangadigital.tungalahari;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class DescribeSongActivity extends AppCompatActivity implements View.OnClickListener {

    public static String mAlbumId, mAlbumTitle, mSongId, mSongTitle, mSongSinger, mSongWriter;

    public static ImageView btnPlay, btnDownload;
    public static TextView seekPosition, totalDuration;

    public static RelativeLayout loadingPanel, downloadingPanel;

    private WebView lyricsView;

    public static SeekBar seekBar;

    private Intent playerService;

    public File downloadDir;
    public static String filePath, filePathAbsolute;

    public static boolean localFileAvailable = false;
    public static boolean isConnectedToInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_describe_song);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        mAlbumId = getIntent().getStringExtra("albumId");
        mAlbumTitle = getIntent().getStringExtra("albumTitle");
        mSongId = getIntent().getStringExtra("songId");
        mSongTitle = getIntent().getStringExtra("songTitle");
        mSongSinger = getIntent().getStringExtra("songSinger");
        mSongWriter = getIntent().getStringExtra("songWriter");

        mSongId = mSongId.replace("song_", "").replace("_", "/");

        try {
            int imageId = getResources().getIdentifier(mAlbumId, "drawable", this.getPackageName());
            Glide.with(this).load(imageId).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }


        TextView singerTextView = (TextView) findViewById(R.id.singer);
        TextView writerTextView = (TextView) findViewById(R.id.writer);
        TextView titleTextView = (TextView) findViewById(R.id.title);

        loadingPanel = (RelativeLayout) findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.GONE);

        downloadingPanel = (RelativeLayout) findViewById(R.id.downloadingPanel);
        downloadingPanel.setVisibility(View.GONE);

        singerTextView.setText(mSongSinger);
        writerTextView.setText(mSongWriter);
        titleTextView.setText(mSongTitle);

//        Media player function

        btnPlay = (ImageView) findViewById(R.id.btnPlay);
        btnPlay.setBackgroundResource(R.drawable.play);

        btnDownload = (ImageView) findViewById(R.id.btnDownload);
        btnDownload.setBackgroundResource(R.drawable.download);

        btnDownload.setOnClickListener(this);

        seekPosition = (TextView) findViewById(R.id.seek_position);
        totalDuration = (TextView) findViewById(R.id.total_duration);

        lyricsView = (WebView) findViewById(R.id.lyrics_webview);

        try {
            if (Arrays.asList(getResources().getAssets().list("albums/" + mSongId)).contains("lyrics.html"))
                lyricsView.loadUrl("file:///android_asset/albums/" + mSongId + "/lyrics.html");
            else
                lyricsView.loadUrl("file:///android_asset/generic/noLyrics.html");
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar = (SeekBar) findViewById(R.id.seek_bar);

        downloadDir = Utility.getAlbumStorageDir(this, "Tungalahari");
        filePath = downloadDir.getPath().concat("/" + mSongId + "/index.mp3");
        filePathAbsolute = downloadDir.getAbsolutePath().concat("/" + mSongId + "/index.mp3");

        File file = new File(filePath);
        if (file.exists()) {

            localFileAvailable = true;
            btnDownload.setBackgroundResource(R.drawable.downloaded);
        }
        else{

            localFileAvailable = false;
        }

        new LongOperation().execute();

        playerService = new Intent(DescribeSongActivity.this, MusicPlayerService.class);
//        if (!(isMyServiceRunning(MusicPlayerService.class)))
        startService(playerService);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//    }

    @Override
    protected void onDestroy() {

        try {
            if (!MusicPlayerService.mp.isPlaying()) {

                MusicPlayerService.mp.stop();

                removeNotification();

                stopService(playerService);
            } else {
                btnPlay.setBackgroundResource(R.drawable.pause);
            }
        } catch (Exception e) {
            Log.e("Exception", "" + e.getMessage() + e.getStackTrace() + e.getCause());
        }

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {

        File file = new File(filePath);
        if (file.exists()) {

            localFileAvailable = true;
            btnDownload.setBackgroundResource(R.drawable.downloaded);
        }
        else{

            localFileAvailable = false;
        }

        try {

            if (!("".equals(MusicPlayerService.mmSongId)) && !(MusicPlayerService.mmSongId.equals(mSongId))) {

                MusicPlayerService.mp.stop();
                MusicPlayerService.mp.reset();
                MusicPlayerService.mp.release();

                stopService(playerService);

                startService(playerService);
            }

            if (isMyServiceRunning(MusicPlayerService.class)) {

                btnPlay.setBackgroundResource(R.drawable.play);
            }

            if (MusicPlayerService.mp.isPlaying()) {

                btnPlay.setBackgroundResource(R.drawable.pause);
            }

        } catch (Exception e) {
            Log.e("Exception", "" + e.getMessage() + e.getStackTrace() + e.getCause());
        }

        super.onResume();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnDownload:

                startDownload();

                break;
        }
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(mSongTitle);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAndRequestPermissions() {

        return true;
    }

//    Download code

    public void startDownload() {

        if (checkAndRequestPermissions()) {
            if (Utility.isExternalStorageWritable()) {

                if (!localFileAvailable) {

                    if(isConnectedToInternet) {

                        btnDownload.setVisibility(View.GONE);
                        downloadingPanel.setVisibility(View.VISIBLE);

                        String url = getString(R.string.audio_source_server) + mSongId + "/index.mp3";
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                        request.setDescription("Downloading...");
                        request.setTitle(mSongTitle);

                        // in order for this if to run, you must use the android 3.2 to compile your app
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                        request.setDestinationUri(Uri.parse("file://" + filePathAbsolute));
                        request.setMimeType("audio/mp3");

                        // get download service and enqueue file
                        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                    else{

                        Toast.makeText(this, "Downloading songs requires an active Internet connection", Toast.LENGTH_LONG).show();
                    }

                } else {

                    Toast.makeText(this, "Song already downloaded", Toast.LENGTH_LONG).show();
                }
            } else {

                Toast.makeText(this, "Can not download! Storage not found", Toast.LENGTH_LONG).show();
            }
        } else {

            Toast.makeText(this, "Permission denied. Go to settings and enable permissions", Toast.LENGTH_LONG).show();
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            btnDownload.setBackgroundResource(R.drawable.downloaded);
            btnDownload.setVisibility(View.VISIBLE);
            downloadingPanel.setVisibility(View.INVISIBLE);

            MusicPlayerService.localFileAvailableHere = true;

            unregisterReceiver(onComplete);
        }
    };

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try{
                isConnectedToInternet = new Common().isConnected();
            }catch (IOException e) {e.printStackTrace();}
            catch (InterruptedException e) {e.printStackTrace();}

            return "Connected";
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            btnPlay.setVisibility(View.GONE);
            loadingPanel.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            btnPlay.setVisibility(View.VISIBLE);
            loadingPanel.setVisibility(View.GONE);
        }
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(101);
    }
}
