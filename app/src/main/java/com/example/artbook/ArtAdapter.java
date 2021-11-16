package com.example.artbook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    ArrayList<Art> list;
    public ArtAdapter(ArrayList<Art> list){
        this.list = list;
    }
    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);
        return new ArtHolder(recyclerRowBinding);

    }

    @Override
    public void onBindViewHolder(ArtAdapter.ArtHolder holder, int position) {
        int pos = position;
        holder.binding.recyclerViewTextView.setText(list.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("artId",list.get(pos).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
