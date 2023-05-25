package com.example.findit;

import com.yandex.mapkit.geometry.Point;

import java.io.Serializable;

public class MapPoint implements Serializable {
    public String name;
    public double latitude;
    public double longitude;

    public MapPoint(String _name,double _latitude, double _longitude){
        name=_name;
        latitude=_latitude;
        longitude=_longitude;
    }

    public MapPoint(String _name, Point point){
        name = _name;
        latitude = point.getLatitude();
        longitude = point.getLongitude();
    }

    public Point ToYandexPoint(){
        return new Point(latitude,longitude);
    }
}
