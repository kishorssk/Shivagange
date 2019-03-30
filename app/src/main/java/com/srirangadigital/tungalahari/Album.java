package com.srirangadigital.tungalahari;

/**
 * Created by root on 17/4/17.
 */

public class Album {
    private String id;
    private String title;
    private int numOfSongs;
    private int thumbnail;

    public Album() {
    }

    public Album(String id, String title, int numOfSongs, int thumbnail) {
        this.id = id;
        this.title = title;
        this.numOfSongs = numOfSongs;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumOfSongs() {
        return numOfSongs;
    }

    public void setNumOfSongs(int numOfSongs) {
        this.numOfSongs = numOfSongs;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
