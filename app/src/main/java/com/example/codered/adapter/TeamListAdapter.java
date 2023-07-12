package com.example.codered.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codered.R;
import com.example.codered.model.TeamModel;

import java.util.ArrayList;

public class TeamListAdapter extends RecyclerView.Adapter<TeamListAdapter.TeamViewHolder> {
    private final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();
    private ArrayList<TeamModel> data;
    private final Context context;

    public TeamListAdapter(ArrayList<TeamModel> teams, Context context) {
        this.data = teams;
        this.context = context;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_view_cards, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        holder.team_name.setText(data.get(holder.getAdapterPosition()).getTeam_name());

        if(data.get(holder.getAdapterPosition()).isSelected())
            holder.team_name.setTextColor(Color.BLUE);
        else
           holder.team_name.setTextColor(Color.BLACK);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        layoutManager.setInitialPrefetchItemCount(data.get(holder.getAdapterPosition()).getMembers().size());

        IndividualListAdapter iAdapter = new IndividualListAdapter(data.get(holder.getAdapterPosition()).getMembers(), context);

        iAdapter.setListener((isFullySelected) -> {
            data.get(holder.getAdapterPosition()).setSelected(isFullySelected);
            if(data.get(holder.getAdapterPosition()).isSelected())
                holder.team_name.setTextColor(Color.BLUE);
            else
                holder.team_name.setTextColor(Color.BLACK);
        });

        holder.team_list_view.setAdapter(iAdapter);
        holder.team_list_view.setLayoutManager(layoutManager);
        holder.team_list_view.setRecycledViewPool(recycledViewPool);

        holder.team_name.setOnClickListener(view -> {
            IndividualListAdapter individualListAdapter = (IndividualListAdapter) holder.team_list_view.getAdapter();
            data.get(holder.getAdapterPosition()).setSelected(!data.get(holder.getAdapterPosition()).isSelected());
            if(individualListAdapter != null) individualListAdapter.selectTeam(data.get(holder.getAdapterPosition()).isSelected());

            if(data.get(holder.getAdapterPosition()).isSelected()) holder.team_name.setTextColor(Color.BLUE);
            else holder.team_name.setTextColor(Color.BLACK);
        });


    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setNewTeams(ArrayList<TeamModel> teams) {
        this.data = teams;
        notifyDataSetChanged();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        private final TextView team_name;
        private final RecyclerView team_list_view;


        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            this.team_name = itemView.findViewById(R.id.teamName);
            this.team_list_view = itemView.findViewById(R.id.recycler_child);
        }
    }
}
