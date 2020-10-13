package com.zero.tunea.classes;

public class Song {

    public int id;
    public String title;
    public String artist;
    public String album;
    public long albumId;
    public String genre;
    public String path;
    public long duration;
    public byte[] image;

    @Override
    public String toString()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setAlbum(String album)
    {
        this.album = album;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public void setAlbumId(long albumId)
    {
        this.albumId = albumId;
    }

    public void setImageByte(byte[] image)
    {
        this.image = image;
    }
}
