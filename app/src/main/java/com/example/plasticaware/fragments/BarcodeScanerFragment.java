package com.example.plasticaware.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.R;
import com.example.plasticaware.data.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;


import java.io.IOException;
import java.util.Objects;


public class BarcodeScanerFragment extends Fragment {
    private SurfaceView surfaceView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private ToneGenerator toneGen1;
    private TextView barcodeText;
    private String barcodeData;
    private BarcodeDetector barcodeDetector;
    private Bundle bundle;
    private  DocumentReference docRef;
    private DocumentSnapshot document;
    private Button btn_again;
    private Button stop_btn;
    private View rectangle;
    private boolean pressed;
    private boolean detected;
    private Handler handler;
    private  TextView beforeScan;
    private boolean  fragmentStopped;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        ((Toolbar_drawer) getActivity()).setDrawerEnabled(true);
        ((Toolbar_drawer) getActivity()).setAction(false);
        ((Toolbar_drawer) getActivity()).setTitle("Barcode Scanner");
        btn_again= rootView.findViewById(R.id.btn_again);
        stop_btn= rootView.findViewById(R.id.btn_stop);

        NavigationView navigation = getActivity().findViewById(R.id.nav_view);
        Menu drawer_menu = navigation.getMenu();
        MenuItem menuItem;
        menuItem = drawer_menu.findItem(R.id.nav_barcode);
        if(!menuItem.isChecked())
        {
            menuItem.setChecked(true);
        }
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        surfaceView = rootView.findViewById(R.id.surface_view);
        barcodeText= rootView.findViewById(R.id.barcodeText);
        rectangle = rootView.findViewById(R.id.Rectangle);
        rectangle.setVisibility(View.GONE);

        beforeScan = rootView.findViewById(R.id.before_scan);
        beforeScan.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.GONE);
        fragmentStopped=true;
        pressed =false;
        detected=false;
        stop_btn.setVisibility(View.GONE);
        initialiseDetectorsAndSources();
        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!pressed) {
                    handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            closeScanner();
                        }
                    }, 30000);
                    pressed =true;
                    btn_again.setText("Press to scan when boundary is green!");
                    beforeScan.setVisibility(View.GONE);
                    surfaceView.setVisibility(View.VISIBLE);
                    rectangle.setVisibility(View.VISIBLE);
                    stop_btn.setVisibility(View.VISIBLE);

                }

                if(detected){
                    detected=false;
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    barcodeDetector.release();
                    ProductFinder();

                }
            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_again.setEnabled(true);
                beforeScan.setVisibility(View.VISIBLE);
                surfaceView.setVisibility(View.GONE);
                rectangle.setVisibility(View.GONE);
                stop_btn.setEnabled(false);

                FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment barcodeReset= new BarcodeScanerFragment();
                ft.replace(R.id.fragment_container, barcodeReset);
                ft.commit();

            }
        });
        return rootView;
    }

    private void initialiseDetectorsAndSources() {
        fragmentStopped=false;
        // Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.UPC_A|Barcode.UPC_E|Barcode.CODE_128|Barcode.CODE_39|Barcode.CODE_93|Barcode.EAN_8|Barcode.EAN_13| Barcode.ITF | Barcode.CODABAR )
                .build();

        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
                .setRequestedPreviewSize(1024, 768)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();





        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (!(ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getActivity(), "You must accept this permission in order to use the barcode scanner!!!", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(), new
                            String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

                } else {
                    try {
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }



            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {


                    barcodeText.post(new Runnable() {


                        @Override
                        public void run() {
                            barcodeData = barcodes.valueAt(0).displayValue;
                            barcodeText.setText(barcodeData);
                            detected=true;
                            Log.d("myTag", String.valueOf(barcodeData));
                            rectangle.setBackground(getActivity().getDrawable(R.drawable.rectangle_detected));


                        }
                    });

                } else{
                    detected=false;
                    rectangle.setBackground(getActivity().getDrawable(R.drawable.rectangle));
                }
            }
        });
    }


    private void ProductFinder()
    {
        handler.removeCallbacksAndMessages(null);
        docRef = db.collection("products").document(barcodeData);
        docRef.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                boolean cacheCheck =false;
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document.exists()) {
                        cacheCheck =true;
                        SelectProductFragment();
                        Log.d("myTag11111111", "DocumentSnapshot data: " + document.getData());

                    }

                }


                if(!cacheCheck && ((Toolbar_drawer) getActivity()).haveNetworkConnection()) {
                    docRef = db.collection("products").document(barcodeData);
                    docRef.get(Source.SERVER).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                document = task.getResult();
                                if (document.exists()) {
                                    SelectProductFragment();
                                    Log.d("myTaginserver", "DocumentSnapshot data: " + document.getData());
                                } else {

                                    GoToUnknownFragment();
                                    Log.d("myTag", "No such document");
                                }
                            } else {
                                GoToUnknownFragment();
                                Log.d("myTag", "get failed with ", task.getException());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("myTaglastResort", "No such document");
                            GoToUnknownFragment();
                        }
                    });
                }


                if(!((Toolbar_drawer) getActivity()).haveNetworkConnection() && !cacheCheck){
                    Toast.makeText(getActivity(), "You appear to have no internet connection, for a more thorough scan, please reconnect to the internet", Toast.LENGTH_LONG).show();
                    closeScanner();
                }
            }


        });

    }


    private void SelectProductFragment(){

        FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        Fragment SelectProduct= new SelectProductFragment();
        Product product = document.toObject(Product.class);


        bundle = new Bundle();
        bundle.putString("barcode",barcodeData);
        bundle.putSerializable("product_selected", product);
        SelectProduct.setArguments(bundle);

        ft.replace(R.id.fragment_container, SelectProduct);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void GoToUnknownFragment(){
        FragmentTransaction ft =  getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        Fragment UnknownProduct= new UnknownProductFragment();

        bundle = new Bundle();
        bundle.putString("barcode",barcodeData);
        UnknownProduct.setArguments(bundle);

        ft.replace(R.id.fragment_container, UnknownProduct);
        ft.addToBackStack(null);
        ft.commit();
    }

      @Override
      public void onPause() {
         super.onPause();
         if(handler!=null) {
             handler.removeCallbacksAndMessages(null);
         }
         //getSupportActionBar().hide();
         cameraSource.release();
     }

      @Override
    public void onResume() {
       super.onResume();
          if(fragmentStopped){
              closeScanner();
          }
     //getSupportActionBar().hide();
      initialiseDetectorsAndSources();
     }

    @Override
    public void onStop() {
        super.onStop();
        surfaceView.setVisibility(View.GONE);
        fragmentStopped=true;

        if(handler!=null) {
            handler.removeCallbacksAndMessages(null);
        }

        //getSupportActionBar().hide();
    }
    private void closeScanner() {
        //Toast.makeText(getActivity(), "Scanner stopped to preserve battery life", Toast.LENGTH_SHORT).show();
        FragmentTransaction ft =  Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        Fragment barcodeReset= new BarcodeScanerFragment();
        ft.replace(R.id.fragment_container, barcodeReset);
        ft.commit();
    }
}
