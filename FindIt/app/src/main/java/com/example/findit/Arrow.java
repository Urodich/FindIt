package com.example.findit;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.view.View;
import android.widget.ImageView;

public class Arrow {

    Context context;
    private ImageView imageView;
    private float currentRotation = 0f;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;

    public Arrow(Context _context, ImageView View){
        imageView = View;
        context=_context;

        // загружаем изображение из ресурсов
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.raw.arrow);
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(v -> imageView.setVisibility(View.GONE));
        imageView.setVisibility(View.GONE);

        sensorManager = (SensorManager) _context.getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    private Location pos, targ;
    public boolean Navigate(Location position, Location target){
        if(position==null || target==null) return false;
        pos=position;
        targ=target;
        Rotate();
        return true;
    }
    void Rotate(){
        float bearing = pos.bearingTo(targ)-90 - azimuth;
        if(f){
            AddLis();
            f=false;
        }
        // поворачиваем ImageView на вычисленный угол
        imageView.setRotation(bearing);
    }
    boolean f= true;
    void AddLis(){
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                onSensorChange(event);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                onSensorChange(event);
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void SetVisible(boolean value){
        if(value) imageView.setVisibility(View.VISIBLE);
        else imageView.setVisibility(View.GONE);
    }

    public void onSensorChange(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = orientation[0];
                azimuth = (float) Math.toDegrees(azimuth);
                azimuth = (azimuth + 360) % 360;

                Rotate();
            }
        }
    }

}
