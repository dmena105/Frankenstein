package com.frankenstein.frankenstein;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by davidmena on 3/4/18.
 *
 * The DAO used by the ROOM Database
 */

@Dao
public interface MyDao {
    @Query("SELECT * FROM entries")
    public List<profileEntry> loadAllEntries();

    @Query("SELECT * FROM entries WHERE id = :my_id")
    public profileEntry loadEntry(long my_id);

    @Delete
    public void delete(profileEntry entry);

    @Insert
    public void insertEntry(profileEntry entry);
}
