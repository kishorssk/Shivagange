package com.srirangadigital.tungalahari;

/**
 * Created by root on 21/4/17.
 */

public class Song {

    private String id;
    private String title;
    private String singer;
    private String writer;
    private String duration;

    public Song(String id, String title, String singer, String writer, String duration) {
        this.id = id;
        this.title = title;
        this.singer = singer;
        this.writer = writer;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
