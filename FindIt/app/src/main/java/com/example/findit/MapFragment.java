package com.example.findit;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapFragment implements Serializable {
    public String name;
    public Drawable image;
    public List<MapPoint> points;
    public MapPoint startLocation;
    public float zoom=14;

    public MapFragment(String _name, Drawable _image){
        name=_name;
        image=_image;
        points = new ArrayList<>();
    }
}

