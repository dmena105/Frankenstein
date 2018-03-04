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
    private String Nickname;  // Manual, GPS or automatic
    @ColumnInfo(name = "photo")
    private String Photo;   // Running, cycling etc

    public long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String mNickname) {
        this.Nickname = mNickname;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String mPhoto) {
        this.Photo = mPhoto;
    }
}
