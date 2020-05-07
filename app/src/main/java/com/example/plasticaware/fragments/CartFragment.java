package com.example.plasticaware.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.R;
import com.example.plasticaware.data.CartData;
import com.example.plasticaware.data.CartDataAdapter;
import com.example.plasticaware.data.Product;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CartFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference cart;
    private CartDataAdapter adapter;
    private TextView noCart;
    private TextView scoreText;
    private RecyclerView filledView;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        ((Toolbar_drawer) getActivity()).setDrawerEnabled(true);
        ((Toolbar_drawer) getActivity()).setAction(false);
        ((Toolbar_drawer) getActivity()).setTitle("Shopping Cart");
        final View rootView;
        NavigationView navigation = getActivity().findViewById(R.id.nav_view);
        navigation.setVisibility(View.VISIBLE);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_Cart);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null){
            cart = db.collection("UserData").document(firebaseAuth.getUid()).collection("cart");

        }


        Query query = cart.orderBy("score", Query.Direction.DESCENDING);


        FirestoreRecyclerOptions<CartData> options = new FirestoreRecyclerOptions.Builder<CartData>().setQuery(query, CartData.class).build();

        adapter = new CartDataAdapter(options);

        rootView = inflater.inflate(R.layout.fragment_cart, container, false);

        filledView = rootView.findViewById(R.id.recycler_view);
        noCart  = rootView.findViewById(R.id.emptyTextView);



        setUpRecyclerView(rootView);
        updateScore(rootView);


        return rootView;
    }


    private void updateScore(final View rootView){
        cart.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    double quantity = 0;
                    double score;
                    double CartScore = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        quantity = quantity+ document.getDouble("quantity");
                        score =  document.getDouble("score");
                        CartScore= CartScore+(score*document.getDouble("quantity"));
                    }

                    DecimalFormat df = new DecimalFormat("#.##");
                    df.setRoundingMode(RoundingMode.CEILING);
                    double FinalScore = ((CartScore)/(quantity*10))*100;
                    String finalScore = "score = " + df.format(FinalScore);
                    scoreText=   rootView.findViewById(R.id.Overall_score);
                    scoreText.setText(finalScore);


                }
            }
        });
    }

    private void setUpRecyclerView(final View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setHasFixedSize(true);


        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {


            public void onItemRangeInserted(int positionStart, int itemCount) {
                adapter.setItemCount(adapter.getItemCount());

                if(adapter.getItemCount()>0){
                    filledView.setVisibility(View.VISIBLE);
                    noCart.setVisibility(View.GONE);
                }

            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
                adapter.setItemCount(adapter.getItemCount()-1);
                updateScore(rootView);
                if(adapter.getCount()==0){
                    filledView.setVisibility(View.GONE);
                    noCart.setVisibility(View.VISIBLE);
                }

            }

        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new CartDataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot Snapshot, int position) {
                FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment SelectedProduct= new SelectProductFragment();

                Bundle bundle = new Bundle();
                Product product = Snapshot.toObject(Product.class);
                bundle.putString("barcode",Snapshot.getId());
                bundle.putSerializable("product_selected", product);
                boolean fromCart=true;
                bundle.putBoolean("fromCart",fromCart);
                SelectedProduct.setArguments(bundle);

                ft.replace(R.id.fragment_container, SelectedProduct);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        Log.d("totalInsert4", String.valueOf(adapter.getCount()));
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
