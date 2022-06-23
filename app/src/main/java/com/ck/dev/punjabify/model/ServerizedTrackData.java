package com.ck.dev.punjabify.model;

import java.io.Serializable;

public class ServerizedTrackData implements Serializable {

    private int index;
    private String album;
    private String artist;
    private int gedi;
    private String gender;
    private int hipHop;
    private int jattism;
    private int legend;
    private String link;
    private int longDrive;
    private int mahfil;
    private int original;
    private int parental;
    private int party;
    private int pro;
    private int rap;
    private String release;
    private int romantic;
    private int sad;
    private String title;
    private int downloaded;
    private int error;

    public ServerizedTrackData() {
    }

    public ServerizedTrackData(int index, String album, String artist, int gedi, String gender, int hipHop, int jattism, int legend, String link, int longDrive, int mahfil, int original, int parental, int party, int pro, int rap, String release, int romantic, int sad, String title) {
        this.index = index;
        this.album = album;
        this.artist = artist;
        this.gedi = gedi;
        this.gender = gender;
        this.hipHop = hipHop;
        this.jattism = jattism;
        this.legend = legend;
        this.link = link;
        this.longDrive = longDrive;
        this.mahfil = mahfil;
        this.original = original;
        this.parental = parental;
        this.party = party;
        this.pro = pro;
        this.rap = rap;
        this.release = release;
        this.romantic = romantic;
        this.sad = sad;
        this.title = title;
        this.downloaded = 0;
        this.error = 0;
    }

    public ServerizedTrackData(int index, String album, String artist, int gedi, String gender, int hipHop, int jattism, int legend, String link, int longDrive, int mahfil, int original, int parental, int party, int pro, int rap, String release, int romantic, int sad, int downloaded, int error,String title) {
        this.index = index;
        this.album = album;
        this.artist = artist;
        this.gedi = gedi;
        this.gender = gender;
        this.hipHop = hipHop;
        this.jattism = jattism;
        this.legend = legend;
        this.link = link;
        this.longDrive = longDrive;
        this.mahfil = mahfil;
        this.original = original;
        this.parental = parental;
        this.party = party;
        this.pro = pro;
        this.rap = rap;
        this.release = release;
        this.romantic = romantic;
        this.sad = sad;
        this.title = title;
        this.error = error;
        this.downloaded = downloaded;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getGedi() {
        return gedi;
    }

    public void setGedi(int gedi) {
        this.gedi = gedi;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getHipHop() {
        return hipHop;
    }

    public void setHipHop(int hipHop) {
        this.hipHop = hipHop;
    }

    public int getJattism() {
        return jattism;
    }

    public void setJattism(int jattism) {
        this.jattism = jattism;
    }

    public int getLegend() {
        return legend;
    }

    public void setLegend(int legend) {
        this.legend = legend;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getLongDrive() {
        return longDrive;
    }

    public void setLongDrive(int longDrive) {
        this.longDrive = longDrive;
    }

    public int getMahfil() {
        return mahfil;
    }

    public void setMahfil(int mahfil) {
        this.mahfil = mahfil;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    public int getParental() {
        return parental;
    }

    public void setParental(int parental) {
        this.parental = parental;
    }

    public int getParty() {
        return party;
    }

    public void setParty(int party) {
        this.party = party;
    }

    public int getPro() {
        return pro;
    }

    public void setPro(int pro) {
        this.pro = pro;
    }

    public int getRap() {
        return rap;
    }

    public void setRap(int rap) {
        this.rap = rap;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public int getRomantic() {
        return romantic;
    }

    public void setRomantic(int romantic) {
        this.romantic = romantic;
    }

    public int getSad() {
        return sad;
    }

    public void setSad(int sad) {
        this.sad = sad;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }
}
