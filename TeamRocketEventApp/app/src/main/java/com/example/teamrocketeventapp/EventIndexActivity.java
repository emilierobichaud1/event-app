package com.example.teamrocketeventapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptor;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class EventIndexActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String userId;
    public static final String EXTRA_MESSAGE = "";
    private TextView mTextMessage;
    private SearchView searchView;
    private ListView listView;
    private ListView CatagorylistView;
    private ArrayAdapter<EventProperties> adapter;
    private ArrayAdapter Catagoryadapter;
    private ArrayList<String> CatagoryNames = new ArrayList<>();
    private ArrayList<String> searchNames = new ArrayList<>();
    private SimpleLocation location;
    private MapFragment mapFragment;

    DatabaseReference eventsRef;
    private FirebaseDatabase database;
    ValueEventListener valueEventListener = new ValueEventListener() {

        private void addEventToMap(EventProperties event) {
            Category eventCategory = CategoryFactory.getCategory(event.getCategory());
            BitmapDescriptor markerIcon = eventCategory.getMarkerIcon();
            List<Double> eventCoordinates = event.getCoordinates();
            LatLng eventPosition = new LatLng(eventCoordinates.get(0), eventCoordinates.get(1));
            mMap.addMarker(new MarkerOptions()
                    .position(eventPosition)
                    .title(event.getName())
                    .icon(markerIcon));
        }

        @Override
        //method that activates upon query
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                searchNames = new ArrayList<>();
                adapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventProperties event = snapshot.getValue(EventProperties.class);
                    if (event != null) {
                        searchNames.add(event.name);
                        adapter.add(event);
                        addEventToMap(event);
                    }
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            if (query.length() == 0) {
                resetSearchView();
                loadFromDb(null, null);
            } else {
                setSearchView();
                loadFromDb(null, query);
            }
            adapter.notifyDataSetChanged();

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (newText.length() == 0) {
                resetSearchView();
                loadFromDb(null, null);
            } else {
                setSearchView();
                loadFromDb(null, newText);
            }
            adapter.getFilter().filter(newText);
            adapter.notifyDataSetChanged();

            return false;
        }
    };


    private ListView.OnItemClickListener searchResultsClickListener = (parent, view, position, id) -> {
        EventProperties event = (EventProperties) parent.getItemAtPosition(position);
        //event.getId()
        Intent intent = new Intent(view.getContext(), EventActivity.class);
        intent.putExtra("eventid", event.getId());
        startActivity(intent);
    };


    private ListView.OnItemClickListener CatagoryClickListener = (parent, view, position, id) -> {

        String cat = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent(view.getContext(), EventCategoryListActivity.class);
        intent.putExtra("category", cat);
        startActivity(intent);
    };


    private boolean inArea(EventProperties event) {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        List<Double> coordinateList = event.getCoordinates();
        LatLng coordinates = new LatLng(coordinateList.get(0), coordinateList.get(1));
        return bounds.contains(coordinates);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_index);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_events:
                    return true;
                case R.id.navigation_profile:
                    //mTextMessage.setText(R.string.profile);
                    Intent intent2 = new Intent(EventIndexActivity.this, UserProfileActivity.class); //temporary change for search testing
                    intent2.putExtra(EXTRA_MESSAGE, userId);
                    startActivity(intent2);
                    return true;
            }
            return false;
        };
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.getMenu().getItem(0).setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        loadFromDb(null, null);

        Intent intent = getIntent();
        userId = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);

        location = new SimpleLocation(this);

        //search by name stuff
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView = findViewById(R.id.searchList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(searchResultsClickListener);
        adapter.notifyDataSetChanged();
        searchView = findViewById(R.id.searchBar);
        searchView.setOnQueryTextListener(searchListener);

        //event categories search view
        Catagoryadapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, CatagoryNames);
        CatagorylistView = (ListView) findViewById(R.id.eventListView);
        CatagorylistView.setAdapter(Catagoryadapter);
        CatagorylistView.setOnItemClickListener(CatagoryClickListener);
        Catagoryadapter.notifyDataSetChanged();
        initializeCatagories();

        getPermissions();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void create(View view) {
        Intent intent = new Intent(this, EventCreateActivity.class);
        startActivity(intent);
    }

    //pulls loads info from the database into the page
    public void loadFromDb(View view, String searchQuery) {

        //query setup
        Query query;
        eventsRef = FirebaseDatabase.getInstance().getReference("events");
        //eventsRef.addListenerForSingleValueEvent(valueEventListener);
        if (searchQuery == null) {
            query = FirebaseDatabase.getInstance().getReference("events").orderByChild("name");
        } else {
            query = FirebaseDatabase.getInstance().getReference("events").orderByChild("name").startAt(searchQuery).endAt(searchQuery + "\uf8ff");
        }
        query.addListenerForSingleValueEvent(valueEventListener);

    }


    //has the list view overlap all of the other elements by setting it to visible and setting everything else to gone
    private void setSearchView() {
        findViewById(R.id.searchList).setVisibility(View.VISIBLE);
        findViewById(R.id.map).setVisibility(View.GONE);
        findViewById(R.id.eventListView).setVisibility(View.GONE);
        findViewById(R.id.navigation).setVisibility(View.GONE);
    }

    // resets stuff done by setSearchView
    private void resetSearchView() {
        findViewById(R.id.searchList).setVisibility(View.GONE);
        findViewById(R.id.map).setVisibility(View.VISIBLE);
        findViewById(R.id.eventListView).setVisibility(View.VISIBLE);
        findViewById(R.id.navigation).setVisibility(View.VISIBLE);
    }

    //loads catagory names into an adapter
    private void initializeCatagories() {
        Catagoryadapter.add("Art");
        Catagoryadapter.add("Career");
        Catagoryadapter.add("Causes");
        Catagoryadapter.add("Educational");
        Catagoryadapter.add("Film");
        Catagoryadapter.add("Fitness");
        Catagoryadapter.add("Food");
        Catagoryadapter.add("Games");
        Catagoryadapter.add("Literature");
        Catagoryadapter.add("Music");
        Catagoryadapter.add("Religion");
        Catagoryadapter.add("Social");
        Catagoryadapter.add("Tech");
        Catagoryadapter.add("Other");
        Catagoryadapter.notifyDataSetChanged();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Default location is Toronto
        double latitude = 43.653908;
        double longitude = -79.384293;

        LatLng defaultLocation = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
    }

    @AfterPermissionGranted(123)//use constant for requestCode
    private void getPermissions() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            Toast.makeText(this, "Showing user location on map", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Permission required to show user location on map", 123, perms);
        }

        checkGPS();
    }

    private void checkGPS() {
        if (!location.hasLocationEnabled()) {
            AlertDialog.Builder gpsBuilder = new AlertDialog.Builder(this);
            gpsBuilder.setMessage("GPS required to show location on map.");
            gpsBuilder.setCancelable(true);

            gpsBuilder.setPositiveButton(
                    "Enable GPS",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SimpleLocation.openSettings(EventIndexActivity.this);
                            dialog.cancel();
                        }

                    });

            gpsBuilder.setNegativeButton(
                    "Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog gpsDialog = gpsBuilder.create();
            gpsDialog.show();
        }
    }

    private void updateMap() {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        LatLng user = new LatLng(lat, lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 10));
    }


}
