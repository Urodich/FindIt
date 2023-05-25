package com.example.findit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;

public class MapActivity extends Activity {

    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private MapView mapView;
    private MapObjectCollection mapObjects;
    private Arrow arrow;
    private Location currentLocation;
    private android.location.LocationManager locationManager;
    private PlacemarkMapObject position;
    private float zoom=14;
    private Point targetPoint;

    LocationListener listener;

    MapFragment mapFragment;
    Button deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Создание MapView.
        setContentView(R.layout.activity_map);
        super.onCreate(savedInstanceState);

        deleteButton = findViewById(R.id.deleteMarkButton);
        deleteButton.setVisibility(View.INVISIBLE);
        Button add = findViewById(R.id.AddButton);
        add.setOnClickListener((v)->addPointDialog());
        Button clear = findViewById(R.id.ClearButton);
        clear.setOnClickListener((v)-> ClearPoints());
        Button navigate = findViewById(R.id.navigate);
        navigate.setOnClickListener((v)->MoveCameraToCurrentLocation());
        findViewById(R.id.BackButton).setOnClickListener((v)->Close());

        mapView = (MapView) findViewById(R.id.mapview);
        mapObjects = mapView.getMap().getMapObjects();

        Gson gson = new Gson();
        mapFragment = gson.fromJson(getIntent().getStringExtra("fragment"), MapFragment.class);
        load(mapFragment);
        if(mapFragment.startLocation!=null)
            mapView.getMap().move(
                    new CameraPosition(mapFragment.startLocation.ToYandexPoint(), zoom, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 0),
                    null);

        ImageView arrowView = findViewById(R.id.arrowView);
        arrow = new Arrow(this, arrowView);
        arrowView.setOnClickListener((v)->{
            deleteButton.setOnClickListener(null);
            deleteButton.setVisibility(View.INVISIBLE);
            arrowView.setVisibility(View.INVISIBLE);
        });
        position=null;
        GetLocation();

        //mapView.getMap().addCameraListener((map, cameraPosition, cameraUpdateReason, b) -> zoom=cameraPosition.getZoom());

    }
    void load(MapFragment mapFragment){
        if (mapFragment.points!=null)
            for (MapPoint point:mapFragment.points) {
                SetPointAt(point);
            }
        else Toast.makeText(this, "NO Points", Toast.LENGTH_SHORT).show();
        if (mapFragment.startLocation==null) return;
    }
    void addPointDialog(){
        if(currentLocation==null) {Toast.makeText(this, "No Location", Toast.LENGTH_SHORT).show(); return;}
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите текст");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString();
            SetPointAtLocation(userInput);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    void MoveCameraToCurrentLocation(){
        if (currentLocation == null){
            Toast.makeText(this, "NO Location", Toast.LENGTH_SHORT).show(); return;}
        mapView.getMap().move(
                new CameraPosition(new Point(currentLocation.getLatitude(), currentLocation.getLongitude()), zoom, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);
    }
    void GetLocation(){
        locationManager = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            return;
        }
        listener = location -> {
            currentLocation = location;
            if (currentLocation == null) return;
            if(position==null) {
                MoveCameraToCurrentLocation();
                position = mapObjects.addPlacemark(new Point(currentLocation.getLatitude(), currentLocation.getLongitude()));
                Toast.makeText(this, "Get Location", Toast.LENGTH_SHORT).show();
            }
            position.setGeometry(new Point(currentLocation.getLatitude(), currentLocation.getLongitude()));
            Navigate();
        };

        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 5000, 5, listener);
    }
    void SetPointAtLocation(String name){
        mapFragment.points.add(new MapPoint(name, currentLocation.getLatitude(), currentLocation.getLongitude()));
        PlacemarkMapObject placemark = mapObjects.addPlacemark(new Point(currentLocation.getLatitude(), currentLocation.getLongitude()));
        placemark.setOpacity(0.5f);
        //placemark.setDraggable(true);
        placemark.setText(name);
        setListeners(placemark);
    }
    void SetPointAt(MapPoint _point){
        PlacemarkMapObject placemark = mapObjects.addPlacemark(_point.ToYandexPoint());
        placemark.setOpacity(0.5f);
        placemark.setText(_point.name);
        setListeners(placemark);
    }
    void setListeners(PlacemarkMapObject placemark){
        placemark.addTapListener((mapObject, point) -> {
            targetPoint= placemark.getGeometry();
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener((v)->{
                for (MapPoint elem :mapFragment.points) {
                    if(elem.ToYandexPoint().getLongitude()==placemark.getGeometry().getLongitude() && elem.ToYandexPoint().getLatitude()==placemark.getGeometry().getLatitude()) {mapFragment.points.remove(elem); Log.i("info", "DELETE");}
                }
                mapObjects.clear();
                load(mapFragment);
                deleteButton.setVisibility(View.INVISIBLE);
                deleteButton.setOnClickListener(null);
            });
            if(!SetTarger(placemark.getGeometry())) return false;
            arrow.SetVisible(true);
            return false;
        });
    }
    boolean SetTarger(Point point){
        targetPoint = point;
        return Navigate();
    }
    boolean Navigate(){
        if (targetPoint==null) return false;
        Location location = new Location("");
        location.setLongitude(targetPoint.getLongitude());
        location.setLatitude(targetPoint.getLatitude());
        return arrow.Navigate(currentLocation, location);
    }
    void ClearPoints(){
        mapFragment.points.clear();
        mapObjects.clear();
        position = mapObjects.addPlacemark(new Point(currentLocation.getLatitude(), currentLocation.getLongitude()));
        arrow.SetVisible(false);
    }
    private void Close(){
        mapFragment.startLocation = new MapPoint("", mapView.getMap().getCameraPosition().getTarget());
        Intent result = new Intent();
        Gson gson=new Gson();
        result.putExtra("fragment", gson.toJson(mapFragment));
        setResult(Activity.RESULT_OK, result);
        finish();
    }


    @Override
    protected void onStop() {
        // Вызов onStop нужно передавать инстансам MapView и MapKit.
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        locationManager.removeUpdates(listener);
        super.onStop();
        Close();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        // Вызов onStart нужно передавать инстансам MapView и MapKit.
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
        if(listener!=null) locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 5000, 5, listener);
        else Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();

        load(mapFragment);
    }
}