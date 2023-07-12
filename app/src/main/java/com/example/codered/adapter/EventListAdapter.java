package com.example.codered.adapter;

import static com.example.codered.Store.dpTopx;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codered.model.EventModel;
import com.example.codered.R;
import com.example.codered.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {
    private List<EventModel> events;
    private static Context context;
    private static List<Integer> list;
    private static int counter;

    public EventListAdapter(List<EventModel> events, Context context) {
        this.events = events;
        EventListAdapter.context = context;

        list = new ArrayList<>();
        for (int i=0; i<5; i++) list.add(i);
        Collections.shuffle(list);

        counter = 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.title.setText(events.get(holder.getAdapterPosition()).getEventTitle());
        holder.time.setText(events.get(holder.getAdapterPosition()).getEventTime());
        holder.status.setText(events.get(holder.getAdapterPosition()).getEventStatus());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<EventModel> eventModels) {
        events = eventModels;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder{
        private TextView title, time, status;
        private CardView parent;
        private boolean toggleAnimation = true;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            time = itemView.findViewById(R.id.eventTime);
            status = itemView.findViewById(R.id.eventStatus);
            parent = itemView.findViewById(R.id.parentEventCard);

            int[] colors = Store.colors;

            LayerDrawable drawable = (LayerDrawable) AppCompatResources.getDrawable(context, R.drawable.event_card_bg);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                        new int[] {context.getColor(R.color.white40), context.getColor(colors[list.get(counter)])});

            GradientDrawable gd2 = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[] {context.getColor(colors[list.get(counter)]), context.getColor(colors[list.get(counter++)])});



            gd.setCornerRadius(60f);
            gd2.setCornerRadius(60f);
            if(drawable!=null) {
                drawable.setDrawable(0, gd2);
                drawable.setDrawable(1, gd);
            }

            parent.setBackground(drawable);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0,0,0, dpTopx(context, 15));
            title.setLayoutParams(layoutParams);

            parent.setOnClickListener(view -> {
                animate(view, title, status);
                toggleAnimation = !toggleAnimation;
            });
        }

        private void animate(View view, TextView title, TextView status) {
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                RelativeLayout.LayoutParams viewParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) title.getLayoutParams();

                if(!toggleAnimation) {
                    viewParams.width = (int) (dpTopx(context,120)*interpolatedTime) + dpTopx(context,130);
                    textParams.bottomMargin = (int) (dpTopx(context,15) * interpolatedTime) + dpTopx(context,15);
                    status.setTranslationX(dpTopx(context, 120) * (1-interpolatedTime) + dpTopx(context, 80));
                }
                else {
                    viewParams.width = (int) (dpTopx(context,120)*(1 - interpolatedTime)) + dpTopx(context,130);
                    textParams.bottomMargin = (int) (dpTopx(context,15) * (1 - interpolatedTime)) + dpTopx(context,15);
                    status.setTranslationX(dpTopx(context, 120) * (interpolatedTime) + dpTopx(context, 80));
                }

                view.requestLayout();
                title.requestLayout();
                }
            };

            a.setDuration(500);
            title.startAnimation(a);
        }
    }
}
