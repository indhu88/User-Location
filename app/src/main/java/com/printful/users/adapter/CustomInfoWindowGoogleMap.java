package com.printful.users.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.printful.users.R;
import com.printful.users.model.UserModel;

/**
 * This class is to show the user details in th custom window when click the user in particular location in map
 */

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {
    private Context context;
    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.map_custom_infowindow, null);
        TextView name_tv = view.findViewById(R.id.tv_name);
        TextView address_tv = view.findViewById(R.id.tv_address);
        ImageView img = view.findViewById(R.id.tv_user_pic);
        UserModel infoWindowData = (UserModel) marker.getTag();
        name_tv.setText(infoWindowData.getName());
        address_tv.setText(infoWindowData.getAddress());
        img.setImageResource(R.drawable.user_img);
        return view;
    }
}
