package com.example.findit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AddItemDialogFragment extends DialogFragment {

    private EditText mTitleEditText;
    private ImageView mImageView;
    private MainActivity mainActivity;

    public AddItemDialogFragment(MainActivity activity){
        mainActivity = activity;
    }

    public static AddItemDialogFragment newInstance(MainActivity activity) {

        return new AddItemDialogFragment(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.add_new_map_fragment, null);


        mTitleEditText = view.findViewById(R.id.MapFragmentName);
        mImageView = view.findViewById(R.id.MapFragmentImage);

        mImageView.setOnClickListener(v -> {
            // Обработчик нажатия на изображение
            // Здесь можно открыть диалоговое окно для выбора изображения
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle("Add Map")
                .setView(view)
                .setPositiveButton("Ok", (dialog, which) -> {
                    MapFragment mapFragment = new MapFragment(mTitleEditText.getText().toString(), mImageView.getDrawable());
                    mainActivity.addMap(mapFragment);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Обработчик нажатия на кнопку "Cancel"
                    dismiss();
                })
                .create();
    }
}

