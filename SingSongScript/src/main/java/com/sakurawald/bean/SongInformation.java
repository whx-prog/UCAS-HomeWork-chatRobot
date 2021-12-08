package com.sakurawald.bean;

import java.util.ArrayList;

//用于描述一首音乐的对象
public class SongInformation {

    private String music_Name = null;
    private long music_ID = 0;
    private int music_Length = 0;
    private String music_introduction = null;
    private String music_Page_URL = null;
    private String author = null;
    private String music_File_URL = null;
    private String music_MID = null;
    private String img_URL = null;
    private String hash = null;
    // 描述这首歌来源于哪里：网易，酷狗，QQ音乐等
    private String sourceType = null;
    // 对该音乐的总结.
    private String summary = null;

    /**
     * 纯净的SongInformation, 需要后续手动set变量
     **/
    public SongInformation() {
        super();
    }

    public SongInformation(String music_name, long music_ID, String sourceType) {
        super();
        this.music_Name = music_name;
        this.music_ID = music_ID;
        this.sourceType = sourceType;
    }

    public SongInformation(String music_name, long music_ID, String author,
                           String music_File_URL, String sourceType) {
        super();
        this.music_Name = music_name;
        this.music_ID = music_ID;
        this.author = author;
        this.music_File_URL = music_File_URL;
        this.sourceType = sourceType;
    }

    public SongInformation(String music_name, long music_ID, String author,
                           String music_File_URL, String music_MID, String sourceType) {
        super();
        this.music_Name = music_name;
        this.music_ID = music_ID;
        this.author = author;
        this.music_File_URL = music_File_URL;
        this.music_MID = music_MID;
        this.sourceType = sourceType;
    }

    public SongInformation(String music_name, String music_File_URL,
                           String music_MID, String sourceType) {
        super();
        this.music_Name = music_name;
        this.music_File_URL = music_File_URL;
        this.music_MID = music_MID;
        this.sourceType = sourceType;
    }

    public static String getSongInformationsBody(ArrayList<SongInformation> sis) {

        StringBuffer sb = new StringBuffer();

        for (SongInformation si : sis) {

            sb.append("【歌曲】" + si.getMusic_Name() + "\n" + "〖作者〗"
                    + si.getAuthor() + "\n" + "〖URL〗" + si.getMusic_File_URL()
                    + "\n\n");

        }

        return sb.toString().trim();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getImg_URL() {
        return img_URL;
    }

    public void setImg_URL(String img_URL) {
        this.img_URL = img_URL;
    }

    public String getMusic_File_URL() {
        return music_File_URL;
    }

    public void setMusic_File_URL(String music_File_URL) {
        this.music_File_URL = music_File_URL;
    }

    public long getMusic_ID() {
        return music_ID;
    }

    public void setMusic_ID(long music_ID) {
        this.music_ID = music_ID;
    }

    public String getMusic_introduction() {
        return music_introduction;
    }

    public void setMusic_introduction(String music_introduction) {
        this.music_introduction = music_introduction;
    }

    public int getMusic_Length() {
        return music_Length;
    }

    public void setMusic_Length(int music_Length) {
        this.music_Length = music_Length;
    }

    public String getMusic_MID() {
        return music_MID;
    }

    public void setMusic_MID(String music_MID) {
        this.music_MID = music_MID;
    }

    public String getMusic_Name() {
        return music_Name;
    }

    public void setMusic_Name(String music_name) {
        this.music_Name = music_name;
    }

    public String getMusic_Page_URL() {
        return music_Page_URL;
    }

    public void setMusic_Page_URL(String music_Page_URL) {
        this.music_Page_URL = music_Page_URL;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public String toString() {
        return "SongInformation{" +
                "music_Name='" + music_Name + '\'' +
                ", music_ID=" + music_ID +
                ", music_Length=" + music_Length +
                ", music_introduction='" + music_introduction + '\'' +
                ", music_Page_URL='" + music_Page_URL + '\'' +
                ", author='" + author + '\'' +
                ", music_File_URL='" + music_File_URL + '\'' +
                ", music_MID='" + music_MID + '\'' +
                ", img_URL='" + img_URL + '\'' +
                ", hash='" + hash + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }
}
