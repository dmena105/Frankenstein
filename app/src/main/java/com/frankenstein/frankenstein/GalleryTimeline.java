package com.frankenstein.frankenstein;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class GalleryTimeline extends AppCompatActivity {

    private static final String TAG = "GalleryTimeline";

    private ListView mListView;
    android.support.v7.widget.Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_view_time_line);
        //Find the List View that will hold the pictures
        mListView = (ListView) findViewById(R.id.GalleryTimeLineListView);

        //Set up the Toolbar
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarForGalleryTimeline);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        ArrayList<Card> list = new ArrayList<>();

        list.add(new Card("drawable://" + R.drawable.yosemite, "Uploaded 12/4/23"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));
        list.add(new Card("drawable://" + R.drawable.yosemite, "Test"));

        CustomListAdapter adapter = new CustomListAdapter(this, R.layout.activity_gallery_timeline, list);

        mListView.setAdapter(adapter);
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
