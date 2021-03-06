package com.srirangadigital.shivagange;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //private String mAlbumId;

    TextView textView;

    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;
    private List<Album> albumList;
//    private List<Song> songList;
//    private SongsAdapter songadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        textView = (TextView)findViewById(R.id.txt_About);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        albumList = new ArrayList<>();
        adapter = new AlbumsAdapter(this, albumList);

//        songList = new ArrayList<>();
//        songadapter = new SongsAdapter(this,songList );

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        prepareAlbums();

        try {
            Glide.with(this).load(R.drawable.cover).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView.addOnItemTouchListener(

                new RecyclerItemClickListener(recyclerView.getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {

                        // do whatever

                        Intent songIntent = new Intent(view.getContext(), DescribeAlbumActivity.class);
                        songIntent.putExtra("albumId", albumList.get(position).getId());
                      //  mAlbumId = albumList.get(position).getId();
                    //    prepareSongs();

                         songIntent.putExtra("albumTitle", albumList.get(position).getTitle());
//                        Log.d("song",position + "");
//                        songIntent.putExtra("songId", songList.get(position).getId());
//                        Log.d("songIntent",songIntent + "");
//                        songIntent.putExtra("songTitle", songList.get(position).getTitle());
//                        songIntent.putExtra("songSinger", songList.get(position).getSinger());
//                        songIntent.putExtra("songWriter", songList.get(position).getWriter());

                        startActivity(songIntent);
                    }


                    @Override
                    public void onLongItemClick(View view, int position) {
                        // Reserved for long clicks
                    }

                })
        );


    }

    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
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
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    /**
     * Adding metadata of albums from json
     */
    private void prepareAlbums() {

        JSONObject albumDetails = JsonOperator.loadJSONObjectFromAsset(recyclerView.getContext(), "albums/albums.json");

        try {

            JSONArray albumsList = albumDetails.getJSONArray("albums");
            for (int i = 0; i < albumsList.length(); i++) {

                JSONObject album = albumsList.getJSONObject(i);

                int resId = getResources().getIdentifier(album.getString("id"), "drawable", this.getPackageName());
                albumList.add(new Album(album.getString("id"), album.getString("title"), album.getInt("numOfSongs"), resId));
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
    }

//    private void prepareSongs() {
//
//        String albumID = mAlbumId.replace("album_", "");
//
//        JSONObject albumDetails = JsonOperator.loadJSONObjectFromAsset(recyclerView.getContext(), "albums/" + albumID + "/albumDetails.json");
//
//        try {
//
//            JSONArray songsList = albumDetails.getJSONArray("songs");
//            Log.d("song",songsList + "");
//            JSONObject info = albumDetails.getJSONObject("info");
//
//            String albumSinger = info.getString("vocals");
//
//            for (int i = 0; i < songsList.length(); i++) {
//
//                JSONObject song = songsList.getJSONObject(i);
//
//                String singer = (song.has("vocals")) ? song.getString("vocals") : albumSinger;
//
//                String id = song.getString("id").replace("song_", "song_" + albumID + "_");
//                Log.d("songID", id + "");
//                songList.add(new Song(id, song.getString("title"), singer, song.getString("writer"), song.getString("duration")));
//            }
//
//        } catch (JSONException e) {
//
//            e.printStackTrace();
//        }
//        songadapter.notifyDataSetChanged();
//    }


    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
