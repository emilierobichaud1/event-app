package com.example.teamrocketeventapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class UserProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();
        String userId = user.getUid();

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_events:
                        return true;
                    case R.id.navigation_home:
                        //mTextMessage.setText(R.string.profile);
                        Intent intent1 = new Intent(UserProfileActivity.this, EventIndexActivity.class); //temporary change for search testing
                        startActivity(intent1);
                        return true;
                }
                return false;
            }
        };
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        String node = "users/" + userId;
        myRef.child(node).addValueEventListener(valueListener);


    }



    public void updateUserInfo(UserProperties currentUser){
        //Update user information on UI after retrieving data from database
        TextView usernameTextView = findViewById(R.id.display_username);
        TextView addressTextView = findViewById(R.id.display_address);
        usernameTextView.setText("Username: " + currentUser.getUsername());
        addressTextView.setText("Address: " + currentUser.getAddress());
    }

    ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Get user from database and use the values to update the UI
            UserProperties value = dataSnapshot.getValue(UserProperties.class);
            updateUserInfo(value);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

}
