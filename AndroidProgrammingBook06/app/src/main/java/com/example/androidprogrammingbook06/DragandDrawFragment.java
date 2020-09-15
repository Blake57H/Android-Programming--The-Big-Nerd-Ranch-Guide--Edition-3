package com.example.androidprogrammingbook06;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DragandDrawFragment extends Fragment {
    public static DragandDrawFragment newInstance() {

        Bundle args = new Bundle();

        DragandDrawFragment fragment = new DragandDrawFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drag_and_draw, container, false);
    }
}
