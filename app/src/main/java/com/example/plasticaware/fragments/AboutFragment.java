package com.example.plasticaware.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.plasticaware.R;
import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.google.android.material.navigation.NavigationView;


public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        ((Toolbar_drawer) getActivity()).setDrawerEnabled(false);
        ((Toolbar_drawer) getActivity()).setAction(true);
        ((Toolbar_drawer) getActivity()).setTitle("About PLASTICAware");
        //setUpRecyclerView(rootView);

        return inflater.inflate(R.layout.about_us, container, false);
    }

}
