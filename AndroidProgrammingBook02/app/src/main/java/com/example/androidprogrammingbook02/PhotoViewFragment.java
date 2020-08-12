package com.example.androidprogrammingbook02;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class PhotoViewFragment extends DialogFragment {
    private static final String ARG_PATH = "photo_path";
    private String photoPath;
    private ImageView mImageView;

    public static PhotoViewFragment newInstance(String path) {

        Bundle args = new Bundle();
        args.putString(ARG_PATH, path);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoPath = getArguments().getString(ARG_PATH);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment, container, false);

        mImageView = v.findViewById(R.id.dialog_fragment_image_view);
        //Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        mImageView.setImageBitmap(PictureUtils.getScaledBitmap(photoPath, getActivity()));

        return v;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment, null);
        photoPath = getArguments().getString(ARG_PATH);
        mImageView = v.findViewById(R.id.dialog_fragment_image_view);
        mImageView.setImageBitmap(PictureUtils.getScaledBitmap(photoPath, getActivity()));
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
