package com.srirangadigital.tungalahari;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by root on 21/4/17.
 */

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

    private Context mContext;
    private List<Song> songList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView index, title, writer, duration;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.index);
            title = (TextView) view.findViewById(R.id.title);
            writer = (TextView) view.findViewById(R.id.writer);
            duration = (TextView) view.findViewById(R.id.duration);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public SongsAdapter(Context mContext, List<Song> songList) {
        this.mContext = mContext;
        this.songList = songList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Song song = songList.get(position);
        String indexString = (position + 1) + ".";

        String songTitle = song.getTitle();

        if(isLocalFileAvailable(song.getId()))
            songTitle += " ✓";

        holder.index.setText(indexString);
        holder.title.setText(songTitle);

        String songWriter = song.getWriter();

        if(!("".equals(songWriter))) {
            holder.writer.setText(songWriter);
            holder.writer.setVisibility(View.VISIBLE);
        }
        else{
            holder.writer.setVisibility(View.GONE);
        }

        holder.duration.setText(song.getDuration());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public boolean isLocalFileAvailable (String pSongID) {

        File downloadDir = Utility.getAlbumStorageDir(mContext, "Tungalahari");
        String mSongIdPath = pSongID.replace("song_", "").replace("_", "/");
        String filePath = downloadDir.getPath().concat("/" + mSongIdPath + "/index.mp3");

        File file = new File(filePath);

        return file.exists();
    }
}
