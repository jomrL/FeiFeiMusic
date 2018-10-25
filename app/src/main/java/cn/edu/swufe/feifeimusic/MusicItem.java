package cn.edu.swufe.feifeimusic;

public class MusicItem {
    private int position;
    private Song song;

    public int getPosition() {
        return position;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setPosition(int position) {
        this.position = position;
    }


}
