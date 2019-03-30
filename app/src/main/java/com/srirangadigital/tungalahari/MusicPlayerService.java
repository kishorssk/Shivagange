package com.srirangadigital.tungalahari;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by root on 1/6/17.
 */

public class MusicPlayerService extends Service implements OnClickListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener  {

    private WeakReference<ImageView> btnPlay;
    public static WeakReference<TextView> seekPosition, totalDuration;
    public static WeakReference<RelativeLayout> loadingPanel;
    public static WeakReference<SeekBar> seekBar;

    public static WeakReference<String> mAlbumId, mAlbumTitle, mSongId, mSongTitle, mSongSinger, mSongWriter, filePathAbsolute;
    public static WeakReference<Boolean> localFileAvailable, isConnectedToInternet;
    public static String mmSongId, mmAlbumId = "";
    public static boolean localFileAvailableHere = false;

    static Handler progressBarHandler = new Handler();

    public static MediaPlayer mp = null;
    private boolean isPause, mediaPrepared = false;

    public boolean audioFocusObtained = false;

    //AudioFocus
    private AudioManager audioManager;

    @Override
    public void onCreate() {

        super.onCreate();

        initMediaPlayer();
        initVariables();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null){
            initUI();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnPlay:

                if(mp == null) {

                    initMediaPlayer();
                    initListeners();
                }

                if (mp.isPlaying()) {

                    mp.pause();
                    isPause = true;
                    progressBarHandler.removeCallbacks(mUpdateTimeTask);
                    btnPlay.get().setBackgroundResource(R.drawable.play);
                    removeNotification();
                    return;
                }

                if(isPause) {

                    initNotification("Playing (Tungalahari)...", this);
                    mp.start();
                    isPause = false;
                    updateProgressBar();
                    btnPlay.get().setBackgroundResource(R.drawable.pause);
                    return;
                }

                if (!mp.isPlaying()) {

                    prepareMediaPlayer();
                }

                break;
        }
    }

    public void updateProgressBar(){
        try{
            progressBarHandler.postDelayed(mUpdateTimeTask, 100);
        }catch(Exception e){

        }
    }

    static Runnable mUpdateTimeTask = new Runnable() {

        public void run(){
            long currentDuration = 0;

            try {
                currentDuration = mp.getCurrentPosition();

                seekPosition.get().setText(Utility.milliSecondsToTimer(currentDuration)); // Displaying time completed playing
                totalDuration.get().setText(Utility.milliSecondsToTimer(mp.getDuration()));

                seekBar.get().setMax(mp.getDuration());
                seekBar.get().setProgress((int) currentDuration);	/* Running this thread after 100 milliseconds */
                progressBarHandler.postDelayed(this, 100);

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {

        loadingPanel.get().setVisibility(View.GONE);
        btnPlay.get().setVisibility(View.VISIBLE);

        try {

            initNotification("Playing (Tungalahari)...", this);
            mp.start();
            updateProgressBar();
            btnPlay.get().setBackgroundResource(R.drawable.pause);

        } catch (Exception e) {
            Log.i("EXCEPTION", "" + e.getMessage());
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START){

            btnPlay.get().setVisibility(View.GONE);
            loadingPanel.get().setVisibility(View.VISIBLE);
            return true;
        }
        else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END){

            btnPlay.get().setVisibility(View.VISIBLE);
            loadingPanel.get().setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

        seekBar.get().setSecondaryProgress(percent * mp.getDuration() / 100);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        mp.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mp != null) {
            mp.release();
        }

        removeNotification();
    }

    // Play song
    public void prepareMediaPlayer() {
        try {
            mp.reset();

            if(localFileAvailableHere)
                mp.setDataSource(filePathAbsolute.get());
            else{

                if(isConnectedToInternet.get()) {

                    btnPlay.get().setVisibility(View.GONE);
                    loadingPanel.get().setVisibility(View.VISIBLE);
                    mp.setDataSource(getString(R.string.audio_source_server) + mSongId.get() + "/index.mp3");
                }
                else{

                    showToast("Playing songs requires an active Internet connection");
                    return;
                }
            }
            mp.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp){

        seekBar.get().setProgress(0);
        progressBarHandler.removeCallbacks(mUpdateTimeTask); /* Progress Update stop */
        btnPlay.get().setBackgroundResource(R.drawable.play);
        seekPosition.get().setText("");
        totalDuration.get().setText("");
        removeNotification();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressBarHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        progressBarHandler.removeCallbacks(mUpdateTimeTask);

        mp.seekTo(seekBar.getProgress());
        updateProgressBar();
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:

                // If mediaPlayer is prepared and ready and only pasued
                // then resume playback
                if(isPause) {
                    mp.start();
                    isPause = false;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:

                // Lost focus for an unbounded amount of time: stop playback and release media player
                resetMediaPlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:

                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mp.isPlaying()) {
                    mp.pause();
                    isPause = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mp.isPlaying()) mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /** AudioFocus */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            audioFocusObtained = true;
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {

        if(audioFocusObtained) {

            audioFocusObtained = false;
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
        }
        return false;
    }

    public void initNotification(String songTitle, Context mContext) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent resultIntent = new Intent(this, DescribeSongActivity.class);

        resultIntent.putExtra("albumId", mmAlbumId);
        resultIntent.putExtra("albumTitle", mAlbumTitle.get());
        resultIntent.putExtra("songId", mSongId.get());
        resultIntent.putExtra("songTitle", mSongTitle.get());
        resultIntent.putExtra("songSinger", mSongSinger.get());
        resultIntent.putExtra("songWriter", mSongWriter.get());

        Intent backIntent = new Intent(this, DescribeAlbumActivity.class);
        backIntent.putExtra("albumId", mmAlbumId);
        backIntent.putExtra("albumTitle", mAlbumTitle.get());

        backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent resultPendingIntent  = PendingIntent.getActivities(this, 0, new Intent[]{homeIntent,backIntent,resultIntent}, PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int imageId = getResources().getIdentifier(mmAlbumId, "drawable", this.getPackageName());

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), imageId);

        mBuilder.setSmallIcon(R.drawable.ic_stat_play_arrow)
                .setLargeIcon(largeIcon)
                .setContentTitle(mSongTitle.get())
                .setContentText(mSongWriter.get())
                .setContentInfo("Now Playing")
                .setShowWhen(false)
                .setOngoing(true);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(101, mBuilder.build());

        //Request audio focus and quit if can't gain focus
        if (!requestAudioFocus()) stopSelf();
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(101);

//        Audio focus is removed along with Notification here
        removeAudioFocus();
    }

    public void showToast(final String message){

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void initMediaPlayer() {

        mp = new MediaPlayer();
        mp.reset();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void initVariables() {

        mAlbumId = new WeakReference<>(DescribeSongActivity.mAlbumId);
        mAlbumTitle = new WeakReference<>(DescribeSongActivity.mAlbumTitle);
        mSongId = new WeakReference<>(DescribeSongActivity.mSongId);
        mSongTitle = new WeakReference<>(DescribeSongActivity.mSongTitle);
        mSongSinger = new WeakReference<>(DescribeSongActivity.mSongSinger);
        mSongWriter = new WeakReference<>(DescribeSongActivity.mSongWriter);

        localFileAvailable = new WeakReference<>(DescribeSongActivity.localFileAvailable);
        isConnectedToInternet = new WeakReference<>(DescribeSongActivity.isConnectedToInternet);
        filePathAbsolute = new WeakReference<>(DescribeSongActivity.filePathAbsolute);

        mmSongId = mSongId.get();
        mmAlbumId = mAlbumId.get();

        localFileAvailableHere = localFileAvailable.get();
    }

    private void initUI() {
        btnPlay = new WeakReference<>(DescribeSongActivity.btnPlay);
        loadingPanel = new WeakReference<>(DescribeSongActivity.loadingPanel);

        seekPosition = new WeakReference<>(DescribeSongActivity.seekPosition);
        totalDuration = new WeakReference<>(DescribeSongActivity.totalDuration);

        seekBar = new WeakReference<>(DescribeSongActivity.seekBar);

        if(mp == null) initMediaPlayer();
        initListeners();
    }

    private void initListeners() {

        seekBar.get().setOnSeekBarChangeListener(this);
        btnPlay.get().setOnClickListener(this);

        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnInfoListener(this);
    }

    public void resetMediaPlayer() {

        if(mp == null) return;

        mp.release();
        mp = null;
        btnPlay.get().setBackgroundResource(R.drawable.play);
        seekBar.get().setProgress(0);
        seekPosition.get().setText("");
        totalDuration.get().setText("");
        progressBarHandler.removeCallbacks(mUpdateTimeTask); /* Progress Update stop */
        removeNotification();
        stopSelf();
    }
}
