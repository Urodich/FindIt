package com.example.findit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.yandex.mapkit.MapKitFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements MyApplicationCallback{

    private static final int REQUEST_CODE = 001;
    private final String MAPKIT_API_KEY = "ddd90c8d-4a53-483d-91c8-351158e226fd";

    List<MapFragment> saves;
    MapAdapter adapter;

    Gson gson;
    String path = "base.json";
    File saveFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //onTerm
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.registerCallback(this);

        //BIND
        findViewById(R.id.AddNewMapFragment).setOnClickListener((v)-> ShowDialog());
        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.Recycler);

        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);

        gson = new Gson();
        //FILE SYSTEM
        saveFile = new File(getFilesDir(), path);

        if (!saveFile.exists())
            try {
                saveFile.createNewFile();
            }
            catch (IOException e){
                Toast.makeText(this, "cant create file", Toast.LENGTH_SHORT).show();
            }

        saves = new ArrayList<>();
        LoadSaves();

        adapter = new MapAdapter(saves);

        adapter.setOnItemClickListener(position -> onClickMapFragment(saves.get(position)));
        adapter.setOnItemLongClickListener((position, view)-> onLongClick(saves.get(position), view));

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }
    void onLongClick(MapFragment mapFragment, View v){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);

        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.context_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.rename:{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Введите название");

                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String userInput = input.getText().toString();
                        mapFragment.name = userInput;
                        adapter.notifyDataSetChanged();
                    });
                    builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

                    builder.show();
                    return true;}
                case R.id.delete:{
                    saves.remove(mapFragment);
                    adapter.notifyDataSetChanged();
                    return true;}
                default:
                    return false;
            }
        });

        popupMenu.show();
    }
    private void ShowDialog(){
        DialogFragment dialogFragment = new AddItemDialogFragment(this);
        dialogFragment.show(getSupportFragmentManager(), "addMap");
    }
    private void LoadSaves() {
        try {
            FileReader reader = new FileReader(saveFile);
            MapFragment[] arr = gson.fromJson(reader, MapFragment[].class);
            saves = new ArrayList<>(Arrays.asList(arr));
            Log.i("info", saves.toString());
            if (saves==null) saves=new ArrayList<>();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cant find saves", Toast.LENGTH_SHORT).show();
            saves=new ArrayList<>();
        }
    }
    @Override
    public void onCallback() {
        Save();
        Log.i("info", "SAVED");
    }
    private void Save(){
        String json = gson.toJson(saves);

        try {
            FileWriter writer = new FileWriter(saveFile);
            writer.write(json);
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error");
        }
    }
    public void addMap(MapFragment mapFragment){
        saves.add(mapFragment);
        adapter.notifyDataSetChanged();
        onClickMapFragment(mapFragment);
    }
    private void createListItems(MapFragment fragment) {

        View listItemView = getLayoutInflater().inflate(R.layout.map_list_element, null);
        ImageView imageView = listItemView.findViewById(R.id.mapFragmentImage);
        TextView textView = listItemView.findViewById(R.id.mapFragmentName);

        textView.append(fragment.name);
        imageView.setImageDrawable(fragment.image);

        listItemView.setOnClickListener((v)->onClickMapFragment(fragment));

    }
    private int currentMap;
    public void onClickMapFragment(MapFragment mapFragment){
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("fragment", gson.toJson(mapFragment));
        currentMap = saves.indexOf(mapFragment);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.hasExtra("fragment")) {
                MapFragment fragment = gson.fromJson(data.getStringExtra("fragment"), MapFragment.class);
                Log.i("info", data.getStringExtra("fragment"));
                saves.set(currentMap, fragment);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Save();
    }
}

