package com.isc.hermes.controller;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.isc.hermes.R;

public class IncidentDialogController {
    private final Context context;
    private Dialog dialog;

    public IncidentDialogController(Context context) {
        this.context = context;
    }

    public void show(String title, String reason, String waitTime, String coordinates) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.incidents_dialog);
        dialog.setCancelable(true);

        TextView titleTextView = dialog.findViewById(R.id.titleIncidentDialog);
        TextView reasonTextView = dialog.findViewById(R.id.reasonTextIncidentDialog);
        TextView waitTimeTextView = dialog.findViewById(R.id.waitTimeTextIncidentDialog);
        TextView coordinatesTextView = dialog.findViewById(R.id.coordinatesTextIncidentDialog);

        titleTextView.setText(title);
        reasonTextView.setText(reason);
        waitTimeTextView.setText(waitTime);
        coordinatesTextView.setText(coordinates);

        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
