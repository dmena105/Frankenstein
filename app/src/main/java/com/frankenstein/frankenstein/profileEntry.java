package com.frankenstein.frankenstein;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by davidmena on 3/4/18.
 *
 * This entry is used by the local database
 */

@Entity(tableName = "entries")
public class profileEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "nickName")
    private String Nickname;  // Nickname
    @ColumnInfo(name = "photo1")
    private String Photo1;   //
    @ColumnInfo(name = "photo2")
    private String Photo2;   // Second Part of Picture

    // Getters
    public long getId() { return id; }

    // Setters
    public void setId(Long id) { this.id = id; }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String mNickname) {
        this.Nickname = mNickname;
    }

    public String getPhoto1() {
        return Photo1;
    }
    public String getPhoto2() {
        return Photo2;
    }

    public void setPhoto1(String mPhoto) {
        this.Photo1 = mPhoto;
    }

    public void setPhoto2(String mPhoto) {
        this.Photo1 = mPhoto;
    }
}
