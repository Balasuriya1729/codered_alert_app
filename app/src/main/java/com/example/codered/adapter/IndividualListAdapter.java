package com.example.codered.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.codered.R;
import com.example.codered.Store;
import com.example.codered.listener.TeamDataChangeEvent;
import com.example.codered.model.MemberModel;

import java.util.ArrayList;


public class IndividualListAdapter extends RecyclerView.Adapter<IndividualListAdapter.IndividualViewHolder>{
    private ArrayList<MemberModel> data;
    private final Context context;
    private TeamDataChangeEvent listener = null;
    private int selectedAll;

    public IndividualListAdapter(ArrayList<MemberModel> data, Context context) {
        this.data = data;
        this.context = context;
        selectedAll = 0;
        for (MemberModel mem :
                data) {
            if(mem.isSelected()) selectedAll++;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public IndividualViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.individual_card, parent, false);
        return new IndividualViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IndividualViewHolder holder, int position) {
        holder.name.setText(data.get(holder.getAdapterPosition()).getName());
        holder.position.setText(data.get(holder.getAdapterPosition()).getPosition());

        if(data.get(holder.getAdapterPosition()).isSelected()){
            holder.parent.setCardElevation(20);
            holder.profile.setElevation(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                holder.parent.setOutlineAmbientShadowColor(Color.RED);
                holder.parent.setOutlineSpotShadowColor(Color.RED);
            }
        }
        else{
            holder.parent.setCardElevation(0);
            holder.profile.setElevation(5);
        }

        if(data.get(holder.getAdapterPosition()).getImageUrl()!=null){
            Glide.with(context).asBitmap().load(data.get(holder.getAdapterPosition()).getImageUrl()).into(holder.profile);
        }
        else {
            if(data.get(holder.getAdapterPosition()).getGender().equals("Male"))
                Glide.with(context).asDrawable().load(R.drawable.avatar_men).into(holder.profile);
            else
                Glide.with(context).asDrawable().load(R.drawable.avatar_female).into(holder.profile);
        }

        if(Store.inTeamSelectionPage || !holder.parent.hasOnClickListeners()) {
            holder.parent.setOnClickListener(view -> {
                if (!data.get(holder.getAdapterPosition()).isSelected()) {
                    data.get(holder.getAdapterPosition()).setSelected(true);
                    if (++selectedAll == data.size() && listener != null)
                        listener.onTeamDataChange(true);

                } else {
                    data.get(holder.getAdapterPosition()).setSelected(false);
                    if (selectedAll-- == data.size() && listener != null)
                        listener.onTeamDataChange(false);
                }

                notifyItemChanged(holder.getAdapterPosition());
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setListener(TeamDataChangeEvent listener) {
        this.listener = listener;
    }
    public void selectTeam(boolean isSelected) {
        for (int i = 0; i < data.size(); i++) {
            if(data.get(i).isSelected() != isSelected) {
                data.get(i).setSelected(isSelected);
                notifyItemChanged(i);
            }
        }
        this.selectedAll = data.size();
    }
    public void setNewMembers(ArrayList<MemberModel> newMembers){
        data = newMembers;
        notifyDataSetChanged();
    }

    public static class IndividualViewHolder extends RecyclerView.ViewHolder {
        private final CardView parent;
        private final TextView name, position;
        private final ImageView profile;

        public IndividualViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = itemView.findViewById(R.id.itemParent);
            name = itemView.findViewById(R.id.nameHolder);
            position = itemView.findViewById(R.id.positionHolder);
            profile = itemView.findViewById(R.id.profile);
            parent.setCardElevation(0);
        }
    }
}
