package com.example.plasticaware.fragments;

import android.os.Bundle;

import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.data.Product;
import com.example.plasticaware.data.ProductAdapter;
import com.example.plasticaware.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProductListFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference allproducts = db.collection("products");

    private ProductAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //NavigationView navigation = (NavigationView)getActivity().findViewById(R.id.nav_view);

        NavigationView navigation = getActivity().findViewById(R.id.nav_view);
        navigation.setVisibility(View.VISIBLE);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_list);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
        ((Toolbar_drawer) getActivity()).setDrawerEnabled(true);
        ((Toolbar_drawer) getActivity()).setAction(false);
        ((Toolbar_drawer) getActivity()).setTitle("Products");
        View rootView = inflater.inflate(R.layout.fragment_product_list, container, false);

        setUpRecyclerView(rootView);

        return rootView;
    }

    private void setUpRecyclerView(View rootView) {
        if(!((Toolbar_drawer) getActivity()).haveNetworkConnection()){
            Toast.makeText(getActivity(), "You appear to have no internet connection, this will impact the results you see here (if any)!!!!", Toast.LENGTH_LONG).show();
        }
        Query query = allproducts.orderBy("title", Query.Direction.DESCENDING).limit(50);

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot Snapshot, int position) {
                FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment SelectedProduct= new SelectProductFragment();

                Bundle bundle = new Bundle();
                Product product = Snapshot.toObject(Product.class);
                bundle.putString("barcode",Snapshot.getId());
                bundle.putSerializable("product_selected", product);
                SelectedProduct.setArguments(bundle);

                ft.replace(R.id.fragment_container, SelectedProduct);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}