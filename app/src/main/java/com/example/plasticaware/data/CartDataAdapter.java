package com.example.plasticaware.data;

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

public class CartDataAdapter extends FirestoreRecyclerAdapter<CartData, CartDataAdapter.ProductHolder> {
  private OnItemClickListener listener;
  private FirestoreRecyclerOptions<CartData> options;
  private int itemCount=0;
  public CartDataAdapter(@NonNull FirestoreRecyclerOptions<CartData> options) {
    super(options);
    this.options = options;
  }

  @Override
  protected void onBindViewHolder(@NonNull ProductHolder holder, int position, @NonNull CartData model) {
    holder.textViewTitle.setText(model.getTitle());
    Picasso.get().load(model.getImage()).into(holder.imageUrl);
    holder.textViewPriority.setText(String.valueOf(model.getScore()));
    String Quantity ="x"+ model.getQuantity();
    holder.textViewQuantity.setText(Quantity);
    holder.textViewQuantity.setVisibility(View.VISIBLE);
  }


  @NonNull
  @Override
  public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
    return new ProductHolder(v);
  }

  public void deleteItem(int position) {
    getSnapshots().getSnapshot(position).getReference().delete();
  }
  public void setItemCount(int count) {
    itemCount=count;
  }


  // adapter has default getItemCount, but need this for dealing with async tasks
  public int getCount() {
    return  itemCount;
  }


  class ProductHolder extends RecyclerView.ViewHolder {
    TextView textViewTitle;
    ImageView imageUrl;
    TextView textViewPriority;
    TextView textViewQuantity;

    ProductHolder(View itemView) {


      super(itemView);
      textViewTitle = itemView.findViewById(R.id.text_view_title);
      imageUrl=itemView.findViewById(R.id.product_Image);
      textViewPriority = itemView.findViewById(R.id.text_view_score);
      textViewQuantity=itemView.findViewById(R.id.text_view_Quantity);
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
}