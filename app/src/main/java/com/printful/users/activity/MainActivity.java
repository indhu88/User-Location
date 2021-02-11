package com.printful.users.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.printful.users.R;
import com.printful.users.TcpClient;
import com.printful.users.adapter.CustomInfoWindowGoogleMap;
import com.printful.users.model.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
/**********************
 This Class is to show Map with list of users
 */
public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String LOG_TAG = "ExampleApp";
    private static final String USER_LIST = "users.json";
    private static final String UPDATE_USER_LIST = "newusers.json";
    private static boolean USER_LIST_SELECTED = true;
    private GoogleMap mMap;
    ArrayList<UserModel> userList;
    TcpClient mTcpClient;
    ImageView imgRefresh;
    Marker userMarker;
    List<Marker> AllMarkers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgRefresh = (ImageView) findViewById(R.id.imageViewplaces);
        // new ConnectTask().execute("");
        getUserCollections(USER_LIST);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Create URL
        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList.clear();
                removeAllMarkers();

                if (USER_LIST_SELECTED) {
                    getUserCollections(UPDATE_USER_LIST);
                    USER_LIST_SELECTED = false;
                } else {
                    getUserCollections(USER_LIST);
                    USER_LIST_SELECTED = true;
                }
                addMarker(userList, mMap);
            }
        });
    }

    /**
     * Add multiple markers in user locations
     * @param userList - list of users to display in map
     * @param mMap -  Object of map
     */
    private void addMarker(ArrayList<UserModel> userList, GoogleMap mMap) {
        for (int i = 0; i < userList.size(); i++) {

            createMarker(userList.get(i).getLat(), userList.get(i).getLongitude(), userList.get(i).getName(), userList.get(i).getAddress(), mMap);
        }
    }

    /**
     * Get users list for json file
     * @param userList -  list of users
     */
    private void getUserCollections(String userList) {
        this.userList = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(readUserJSON(userList));
            JSONArray array = object.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {

                JSONObject jsonObject = array.getJSONObject(i);
                String id = jsonObject.getString("hcid");
                String name = jsonObject.getString("name");
                double lat = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                String address = jsonObject.getString("address");
                UserModel model = new UserModel();
                model.setId(id);
                model.setName(name);
                model.setLat(lat);
                model.setLongitude(longitude);
                model.setAddress(address);
                this.userList.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the user from JSON file
     * @param userList - list od users
     * @return
     */
    private String readUserJSON(String userList) {
        String json = null;
        try {
            // Opening data.json file
            InputStream inputStream = getAssets().open(userList);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            inputStream.read(buffer);
            inputStream.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return json;
        }
        return json;
    }

    /**
     * This method to check network is available or not
     * @param context - context of the class
     * @return
     */
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!isNetworkAvailable(this)) {
            Toast.makeText(this, "Please Check your Internet Connection", Toast.LENGTH_SHORT).show();
        } else {
            removeAllMarkers();
            mMap = googleMap;
            for (int i = 0; i < userList.size(); i++) {
                createMarker(userList.get(i).getLat(), userList.get(i).getLongitude(), userList.get(i).getName(), userList.get(i).getAddress(), mMap);
            }
        }
    }

    /**
     * Plot the marker in user location to the map
     * @param lat - latitude of the user
     * @param longitude - longitude of the user
     * @param name - name of the user
     * @param address - address of the user
     * @param mMap - object of the map
     */
    private void createMarker(double lat, double longitude, String name, String address, GoogleMap mMap) {

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(this);
        mMap.setInfoWindowAdapter(customInfoWindow);
        userMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, longitude))
                .anchor(0.5f, 0.5f)
                .title(name)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.person_pin)));
//        else{
//
//            LatLng newLatLng = new LatLng(lat,longitude);
//
//            userMarker= MarkerAnimation.animateMarkerToGB(userMarker, newLatLng, new LatLngInterpolator.Spherical());
//        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAddress(address);
        userMarker.setTag(userModel);
        AllMarkers.add(userMarker); // add the marker to array
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longitude), 5.6f));

    }

    /**
     * Remove all makers in the map
     */
    private void removeAllMarkers() {
        for (Marker mLocationMarker : AllMarkers) {
            mLocationMarker.remove();
        }
        AllMarkers.clear();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * This class is to get users list from the server
     *
     */
    public class ConnectTask extends AsyncTask<String, String, TcpClient> {

        @Override
        protected TcpClient doInBackground(String... message) {

            //we create a TCPClient object
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            //response received from server
            Log.d("test", "response " + values[0]);
            //process server response here....

        }
    }
}