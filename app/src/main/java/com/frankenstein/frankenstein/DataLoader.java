package com.frankenstein.frankenstein;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by davidmena on 3/4/18.
 *
 * A syncTask for loading the local Database
 */

public class DataLoader extends AsyncTaskLoader<List<profileEntry>> {
    public Context mContext;
    public DataLoader(Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<profileEntry> loadInBackground() {
        List<profileEntry> entryList = AppDatabase.getAppDatabase(getContext()
                .getApplicationContext()).myDao().loadAllEntries();
        return entryList;
    }
}
