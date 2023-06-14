package com.isc.hermes.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.isc.hermes.R;
import com.isc.hermes.model.WayPoint;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlaceViewHolder> {

    private List<WayPoint> wayPoints;
    private WayPointClickListener wayPointClickListener;

    public PlacesAdapter(List<WayPoint> wayPoints, WayPointClickListener wayPointClickListener) {
        this.wayPoints = wayPoints;
        this.wayPointClickListener = wayPointClickListener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new PlaceViewHolder(view, wayPointClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(wayPoints.get(position));
    }

    @Override
    public int getItemCount() {
        return wayPoints.size();
    }

    public void addWayPoint(WayPoint wayPoint) {
        this.wayPoints.add(wayPoint);
        notifyItemInserted(wayPoints.size() - 1);
    }

    public void clearWayPoints() {
        this.wayPoints.clear();
        notifyDataSetChanged();
    }
}
