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

    public long getId() { return id; }
    public String getmNickname() { return mNickname; }
    public String getmPhoto() { return mPhoto; }

    public void setId(Long id) { this.id = id; }
    public void setmNickname(String nickname) { this.mNickname = nickname;}
    public void setmPhoto(String photo) { this.mPhoto = photo;}

}
