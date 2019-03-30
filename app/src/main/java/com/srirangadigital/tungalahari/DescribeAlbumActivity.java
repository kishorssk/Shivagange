package com.srirangadigital.tungalahari;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DescribeAlbumActivity extends AppCompatActivity {

    private String mAlbumId;
    private String mAlbumTitle;

    private TextView albumTitle;
    private RecyclerView recyclerView;
    private SongsAdapter adapter;
    private List<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_describe_album);

        mAlbumId = getIntent().getStringExtra("albumId");
        mAlbumTitle = getIntent().getStringExtra("albumTitle");

        albumTitle = (TextView) findViewById(R.id.album_title);
        albumTitle.setText(mAlbumTitle);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        songList = new ArrayList<>();
        adapter = new SongsAdapter(this, songList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareSongs();

        try {

            int imageId = getResources().getIdentifier(mAlbumId, "drawable", this.getPackageName());
            Glide.with(this).load(imageId).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView.addOnItemTouchListener(

            new RecyclerItemClickListener(recyclerView.getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                @Override
                public void onItemClick(View view, int position) {
                    // do whatever

                    Intent songIntent = new Intent(view.getContext(), DescribeSongActivity.class);
                    songIntent.putExtra("albumId", mAlbumId);
                    songIntent.putExtra("albumTitle", mAlbumTitle);
                    songIntent.putExtra("songId", songList.get(position).getId());
                    songIntent.putExtra("songTitle", songList.get(position).getTitle());
                    songIntent.putExtra("songSinger", songList.get(position).getSinger());
                    songIntent.putExtra("songWriter", songList.get(position).getWriter());
                    startActivity(songIntent);
                }

                @Override
                public void onLongItemClick(View view, int position) {
                    // Reserved for long clicks
                }
            })
        );
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
                    collapsingToolbar.setTitle(mAlbumTitle);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void prepareSongs() {

        String albumID = mAlbumId.replace("album_", "");

        JSONObject albumDetails = JsonOperator.loadJSONObjectFromAsset(recyclerView.getContext(), "albums/" + albumID + "/albumDetails.json");

        try {

            JSONArray songsList = albumDetails.getJSONArray("songs");
            JSONObject info = albumDetails.getJSONObject("info");

            String albumSinger = info.getString("vocals");

            for (int i = 0; i < songsList.length(); i++) {

                JSONObject song = songsList.getJSONObject(i);

                String singer = (song.has("vocals")) ? song.getString("vocals") : albumSinger;

                String id = song.getString("id").replace("song_", "song_" + albumID + "_");
                songList.add(new Song(id, song.getString("title"), singer, song.getString("writer"), song.getString("duration")));
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }
}
