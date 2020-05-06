package com.example.plasticaware.fragments;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.R;
import com.example.plasticaware.data.CartData;
import com.example.plasticaware.data.Product;
import com.example.plasticaware.data.ProductAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

public class SelectProductFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private ProductAdapter adapter;
    private CollectionReference similarProducts = db.collection("products");
    private boolean insideProduct=false;
    private TextView noOtherProduct;
    private RecyclerView recyclerView;
    private boolean fromCart = false;
    private String barcode;
    private View rootView;
    private DocumentReference cartProducts;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        final Product selectedItem= (Product) bundle.getSerializable("product_selected");
        fromCart= bundle.getBoolean("fromCart");
        insideProduct =bundle.getBoolean("inside_product");
        barcode=bundle.getString("barcode");

        ((Toolbar_drawer) getActivity()).setDrawerEnabled(false);
        ((Toolbar_drawer) getActivity()).setAction(true);
        ((Toolbar_drawer) getActivity()).setTitle(selectedItem.getTitle());

        firebaseAuth = FirebaseAuth.getInstance();

        cartProducts = db.collection("UserData").document(firebaseAuth.getUid()).collection("cart").document(barcode);

        NavigationView navigation= getActivity().findViewById(R.id.nav_view);
        navigation.setVisibility(View.GONE);

        rootView = inflater.inflate(R.layout.fragment_selected_item, container, false);

        TextView textViewUsername = rootView.findViewById(R.id.title);
        TextView textViewDescription = rootView.findViewById(R.id.description);
        TextView textViewScore = rootView.findViewById(R.id.score);

        textViewUsername.setText(selectedItem.getTitle());

        textViewDescription.setText(selectedItem.getDescription());

        String score = "Packaging Score = " +selectedItem.getScore();
        textViewScore.setText(score);

        ImageView image=rootView.findViewById(R.id.Select_Image);
        Picasso.get().load(selectedItem.getImage()).resize(300,300).into(image);

        setUpVariables(selectedItem);
        return rootView;
    }

    private void setUpVariables(final Product selectedItem){
        noOtherProduct  = rootView.findViewById(R.id.emptyTextView);
        recyclerView =  rootView.findViewById(R.id.recycler_view_in_selected);
        if(!insideProduct && !fromCart){
            recyclerView.setVisibility(View.VISIBLE);
            setUpRecyclerView(selectedItem);
        } else {
            noOtherProduct.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
        FloatingActionButton button = rootView.findViewById(R.id.floatingActionButton);

        if(fromCart){
            button.setImageResource(R.drawable.ic_delete);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please enter the amount you wish to remove from your cart!");
// Set up the input
                    final EditText input = new EditText(getActivity());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
// Set up the buttons
                    builder.setPositiveButton("enter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int quantity;
                            if (input.getText().toString().trim().isEmpty()  ) {
                                quantity =0;
                            }
                            else{
                                quantity = Integer.parseInt(input.getText().toString());
                            }

                            //Log.d("myTag", String.valueOf(quantity));
                            if (quantity ==0 ) {
                                Toast.makeText(getActivity(), "Please insert a valid number", Toast.LENGTH_SHORT).show();

                            } else {
                                deleteSpecificFromCart(selectedItem,quantity);
                            }

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });
        } else {
            button.setImageResource(R.drawable.ic_add);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please enter the amount you are adding to your cart!");
                    final EditText input = new EditText(getActivity());
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    builder.setView(input);
                    builder.setPositiveButton("enter", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int quantity;
                            if (input.getText().toString().trim().isEmpty()  ) {
                                quantity =0;
                            }
                            else{
                                 quantity = Integer.parseInt(input.getText().toString());
                            }

                           // Log.d("myTag", String.valueOf(quantity));
                            if (quantity ==0 ) {
                                Toast.makeText(getActivity(), "Please insert a valid number", Toast.LENGTH_SHORT).show();

                            } else {
                                CreateUserCart(selectedItem,quantity);}
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });
        }
        if(fromCart){
            noOtherProduct.setVisibility(View.GONE);
        }
    }

    private void setUpRecyclerView(Product selectedItem){

        Query query = similarProducts.whereEqualTo("group", selectedItem.getGroup())
                .whereLessThan("score",selectedItem.getScore()).orderBy("score",Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Product> options = new FirestoreRecyclerOptions.Builder<Product>()
                .setQuery(query, Product.class)
                .build();

        adapter = new ProductAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);


        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {


            public void onItemRangeInserted(int positionStart, int itemCount) {
                adapter.setItemCount(adapter.getItemCount());
                if(adapter.getItemCount()>0){
                    recyclerView.setVisibility(View.VISIBLE);
                    String similarItems="Below are some similar items with a better score!!!";
                    noOtherProduct.setText(similarItems);
                }
            }
        });

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

                bundle.putBoolean("inside_product",true);
                SelectedProduct.setArguments(bundle);
                onStop();
                ft.replace(R.id.fragment_container, SelectedProduct);
                ft.addToBackStack(null);
                ft.commit();

            }
        });
    }

    private void CreateUserCart(final Product selectedItem, final int inputQuantity){

        cartProducts.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    int quantity = documentSnapshot.getLong("quantity").intValue();
                    CartData cartData = new CartData(
                            selectedItem.getTitle(),
                            selectedItem.getDescription(),
                            selectedItem.getScore(),
                            selectedItem.getImage(),
                            quantity+inputQuantity);
                    db.collection("UserData").document(firebaseAuth.getUid()).collection("cart").document(barcode).set(cartData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getActivity(), "Product added to cart", Toast.LENGTH_SHORT).show();
                                    getActivity().onBackPressed();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error! something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                }
                            });

                } else{
                    CartData cartData = new CartData(
                            selectedItem.getTitle(),
                            selectedItem.getDescription(),
                            selectedItem.getScore(),
                            selectedItem.getImage(),
                            inputQuantity);
                    db.collection("UserData").document(firebaseAuth.getUid()).collection("cart").document(barcode).set(cartData, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getActivity(), "Product added to cart", Toast.LENGTH_SHORT).show();
                                    getActivity().onBackPressed();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Error! something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void deleteSpecificFromCart(final Product selectedItem, final int inputQuantity){


        cartProducts.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    int quantity = documentSnapshot.getLong("quantity").intValue();
                    if(quantity-inputQuantity<=0){
                        cartProducts.delete();
                        Toast.makeText(getActivity(), "Product removed from cart", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    } else {
                        CartData cartData = new CartData(
                                selectedItem.getTitle(),
                                selectedItem.getDescription(),
                                selectedItem.getScore(),
                                selectedItem.getImage(),
                                quantity - inputQuantity);
                        db.collection("UserData").document(firebaseAuth.getUid()).collection("cart").document(barcode).set(cartData, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), "Product removed from cart", Toast.LENGTH_SHORT).show();
                                        getActivity().onBackPressed();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), "Error! something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Error! something went wrong, please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if(!insideProduct && !fromCart){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(!insideProduct && !fromCart) {
            adapter.stopListening();
        }
    }
}
