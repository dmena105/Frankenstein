package com.frankenstein.frankenstein;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

public class GalleryTimeline extends AppCompatActivity {

    private static final String TAG = "GalleryTimeline";

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_view_time_line);
        //Find the List View that will hold the pictures
        mListView = (ListView) findViewById(R.id.GalleryTimeLineListView);

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
}
