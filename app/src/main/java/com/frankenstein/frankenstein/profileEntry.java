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
    private String mNickname;  // Manual, GPS or automatic
    @ColumnInfo(name = "photo")
    private String mPhoto;   // Running, cycling etc

    // Getters
    public long getId() { return id; }
    public String getNickname() { return mNickname; }
    public String getPhoto() { return mPhoto; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setNickname(String nickname) { this.mNickname = nickname;}
    public void setPhoto(String photo) { this.mPhoto = photo;}

}

