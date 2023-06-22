package com.isc.hermes.controller;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.isc.hermes.R;
import com.isc.hermes.utils.Animations;

public class NavigationDirectionController {
    static boolean isActive;
    private final Context context;
    private final RelativeLayout directionsForm;
    private final MapWayPointController mapWayPointController;


    public NavigationDirectionController(Context context, MapWayPointController mapWayPointController) {
        this.context = context;
        this.mapWayPointController = mapWayPointController;
        directionsForm = ((AppCompatActivity)context).findViewById(R.id.directions_form);

    }

    private void setButtons() {

    }

    void updateUiPointsComponents() {
        if (isActive) {
            directionsForm.startAnimation(Animations.entryAnimation);
            directionsForm.setVisibility(View.VISIBLE);
        }
    }

    public RelativeLayout getDirectionsForm() {
        return directionsForm;
    }

}