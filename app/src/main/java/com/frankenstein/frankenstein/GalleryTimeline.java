package com.frankenstein.frankenstein;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class GalleryTimeline extends AppCompatActivity {
    private ArrayList<Marker> mAllEntries;
    private static final String TAG = "GalleryTimeline";
    private int previousLast;
    private ArrayList<Card> list;
    private ListView mListView;
    private CustomListAdapter adapter;
    private int entriesLoaded = 0;
    private boolean doneLoading = false;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_view_time_line);
        mAllEntries = MapFragment.mAllMarkers;
        //Find the List View that will hold the pictures
        mListView = (ListView) findViewById(R.id.GalleryTimeLineListView);
        list = new ArrayList<>();
        //Set up the Toolbar
        toolbar = (android.support.v7.widget.Toolbar)findViewById(R.id.toolbarForGalleryTimeline);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        adapter = new CustomListAdapter(this, R.layout.activity_gallery_timeline, list);
        mListView.setAdapter(adapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!doneLoading) {
                    Log.d("debug", "not done");
                    switch (view.getId()) {
                        case R.id.GalleryTimeLineListView:
                            final int lastItem = firstVisibleItem + visibleItemCount;
                            if (lastItem == totalItemCount) {
                                DatabaseReference refUtil = MainActivity.databaseReference.child("users")
                                        .child(MainActivity.username).child("items");
                                Query query = refUtil.orderByChild("postTime");
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                            for (int i = 0; i < Math.min(entriesLoaded, dataSnapshot.getChildrenCount()); i++) {
                                                iterator.next();
                                            }
                                            for (int i = 0; i < 5; i++) {
                                                if (iterator.hasNext()) {
                                                    Log.d("debug", "onDataChange");
                                                    DataSnapshot dss = iterator.next();
                                                    double lat = dss.child("latitude").getValue(Double.class);
                                                    double lng = dss.child("longitude").getValue(Double.class);
                                                    String picKey = lat + "-" + lng;
                                                    String encodedImage;
                                                    if (MapFragment.mPictureCache.containsKey(picKey)){
                                                        encodedImage = MapFragment.mPictureCache.get(picKey);
                                                    }
                                                    else {
                                                        encodedImage = dss.child("picture").getValue(String.class);
                                                    }
                                                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
                                                    Calendar calendar = Calendar.getInstance();
                                                    calendar.setTimeInMillis(dss.child("postTime").getValue(Long.class));
                                                    adapter.add(new Card(encodedImage, formatter.format(calendar.getTime())));
                                                } else doneLoading = true;
                                            }
                                            entriesLoaded += 5;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                                if (previousLast != lastItem) {
                                    //to avoid multiple calls for last item
                                    Log.d("debug", "Last");
                                    previousLast = lastItem;
                                }
                            }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery_timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Listen to for when the user presses the save Button
        if (item.getItemId() == R.id.action_back) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
