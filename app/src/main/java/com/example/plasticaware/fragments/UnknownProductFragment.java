package com.example.plasticaware.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.plasticaware.R;
import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.data.UnknownProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class UnknownProductFragment extends Fragment {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String barcode;
    private String title ;
    private String description;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        barcode = bundle.getString("barcode");

        ((Toolbar_drawer) getActivity()).setDrawerEnabled(false);
        ((Toolbar_drawer) getActivity()).setAction(true);
        ((Toolbar_drawer) getActivity()).setTitle("Unknown Product Scanned");
        View rootView = inflater.inflate(R.layout.fragment_uknown_product, container, false);
        TextView barcode_value = rootView.findViewById(R.id.barcode_value);

        String barcodeDisplay = "barcode scanned = " + barcode;
        barcode_value.setText(barcodeDisplay);
        editTextTitle = rootView.findViewById(R.id.edit_text_title);
        editTextDescription = rootView.findViewById(R.id.edit_text_description);


        FloatingActionButton button = rootView.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                title = editTextTitle.getText().toString();
                                description = editTextDescription.getText().toString();

                                if (title.trim().isEmpty() || description.trim().isEmpty()) {
                                    Toast.makeText(getActivity(), "Please insert a title and description", Toast.LENGTH_SHORT).show();

                                } else {
                                    UnknownProduct UserInput =new UnknownProduct(title, description);
                                    CreateUnknownProduct(UserInput);
                                }


                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;


                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure the details are correct?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        //setUpRecyclerView(rootView);

        return rootView;
    }
    private void CreateUnknownProduct(UnknownProduct userInput){


        db.collection("UnknownItem").document(barcode).set(userInput, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Product under review", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), "Thank you for helping us!!!", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Error! something went wrong, please try again", Toast.LENGTH_SHORT).show();
                        getActivity().onBackPressed();
                    }
                });

    }

}
