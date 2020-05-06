package com.example.plasticaware.data;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plasticaware.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import static java.security.AccessController.getContext;

public class ProductAdapter extends FirestoreRecyclerAdapter<Product, ProductAdapter.ProductHolder> {
    private OnItemClickListener listener;
    private int itemCount=0;
    public ProductAdapter(@NonNull FirestoreRecyclerOptions<Product> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductHolder holder, int position, @NonNull Product model) {
        holder.textViewTitle.setText(model.getTitle());
        Picasso.get().load(model.getImage()).resize(300,300).into(holder.imageUrl);
        holder.textViewPriority.setText(String.valueOf(model.getScore()));
        holder.textViewQuantity.setVisibility(View.GONE);
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item,
                parent, false);
        return new ProductHolder(v);
    }

    public void deleteItem(int position) {
        //getSnapshots().getSnapshot(position).getReference().delete();
    }

    class ProductHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        ImageView imageUrl;
        TextView textViewPriority;
        TextView textViewQuantity;
        public ProductHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            imageUrl=itemView.findViewById(R.id.product_Image);
            textViewPriority = itemView.findViewById(R.id.text_view_score);
            textViewQuantity = itemView.findViewById(R.id.text_view_Quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItemCount(int count) {
        itemCount=count;
    }


    // adapter has default getItemCount, but need this for dealing with async tasks
    public int getCount() {
        return  itemCount;
    }
}