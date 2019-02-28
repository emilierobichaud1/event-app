package com.example.teamrocketeventapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventActivity extends AppCompatActivity {

    EventProperties event;
    private String eventId;
    private String hostName;

    //needed to pull data from the database
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        //method that activates upon query
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    event = snapshot.getValue(EventProperties.class);
                    getHostName();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Bundle b = getIntent().getExtras();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                // TODO: add navigation for other buttons (needs those pages implemented)
                case R.id.navigation_events:
                    // Switch to event index when "Events" button is pressed
                    Intent intent = new Intent(this, EventIndexActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        });

        if(b != null){
            //get the eventproperties object
            eventId = (String) b.get("eventid");

            DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
            Query query = FirebaseDatabase.getInstance().getReference("events").orderByChild("id").equalTo(eventId);
            query.addListenerForSingleValueEvent(valueEventListener);
        }
    }


    private void getHostName(){
        Query query2 = FirebaseDatabase.getInstance().getReference("users");
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(event.attendees.get(0).toString().equals(snapshot.getRef().getKey())) {
                            UserProperties usr = snapshot.getValue(UserProperties.class);
                            hostName = usr.getUsername();
                            loadData();
                        }

                    }
                }
            }
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void loadData(){
        //sets textboxex
        TextView nameTextView = findViewById(R.id.eventName);
        TextView dateTextView = findViewById(R.id.date);
        TextView numAttendeesTextView = findViewById(R.id.attendeeCount);
        TextView timeTextView = findViewById(R.id.time);
        TextView locationTextView = findViewById(R.id.location);
        TextView hostTextView = findViewById(R.id.host);

        nameTextView.setText(event.name);
        dateTextView.setText(event.date);
        numAttendeesTextView.setText("# of Attendees: " + event.attendees.size());
        timeTextView.setText("Time: " + event.time);
        locationTextView.setText("Location: " + event.location);
        hostTextView.setText("Host: " + hostName);


    }

}
