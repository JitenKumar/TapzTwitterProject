package com.jiten.tapztwitter;

//import android.content.Context;
//import android.database.Cursor;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.CursorAdapter;
//import android.view.View;
//import android.widget.TextView;
///**
// * Created by jiten on 26/8/17.
// */

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.jiten.tapztwitter.MainActivity;

public class UpdateAdapter extends CursorAdapter {
    public UpdateAdapter(MainActivity thread, Cursor cursor) {
        super(thread, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.activity_main, parent, false);
    }
    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template

        TextView uname = (TextView) view.findViewById(R.id.Uname);
        TextView timest = (TextView) view.findViewById(R.id.timeStamp);
        // Extract properties from cursor
        TextView url  = (TextView) view.findViewById(R.id.URLLink);
        TextView tID = (TextView) view.findViewById(R.id.TweetID);

        String UBody = cursor.getString(cursor.getColumnIndexOrThrow("userid"));
        String urlLink= cursor.getString(cursor.getColumnIndexOrThrow("url"));
        String tstamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
        String tid = cursor.getString(cursor.getColumnIndexOrThrow("tweetid"));

        // Populate fields with extracted properties

        uname.setText(UBody);
        timest.setText(tstamp);
        url.setText(urlLink);
        tID.setText(tid);

    }
}


