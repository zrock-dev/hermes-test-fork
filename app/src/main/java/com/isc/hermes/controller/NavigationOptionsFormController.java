package com.isc.hermes.controller;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.isc.hermes.R;
import com.isc.hermes.utils.Animations;
import com.isc.hermes.view.IncidentTypeButton;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;


public class NavigationOptionsFormController {
    private final Context context;
    private final RelativeLayout navOptionsForm;
    private final Button cancelButton, startButton, chooseStartPointButton,
            startPointButton, finalPointButton;
    private final LinearLayout transportationTypesContainer;
    private final TextView navOptionsText;
    private final TextInputLayout chooseTextField;
    public static String incidentType, startPointName, finalPointName;
    private final MapWayPointController mapWayPointController;
    private boolean isStartPointFromMapSelected;
    private LatLng startPoint, finalPoint;

    /**
     * This is the constructor method. Init all the necessary components.
     *
     * @param context Is the context application.
     * @param mapWayPointController Is the controller of the map.
     */
    public NavigationOptionsFormController(Context context, MapWayPointController mapWayPointController) {
        this.context = context;
        this.isStartPointFromMapSelected = false;
        this.mapWayPointController = mapWayPointController;
        navOptionsForm = ((AppCompatActivity)context).findViewById(R.id.navOptions_form);
        cancelButton = ((AppCompatActivity) context).findViewById(R.id.cancel_navOptions_button);
        startButton = ((AppCompatActivity) context).findViewById(R.id.start_button);
        chooseStartPointButton = ((AppCompatActivity) context).findViewById(R.id.choose_startPoint_button);
        startPointButton = ((AppCompatActivity) context).findViewById(R.id.startPoint_button);;
        finalPointButton = ((AppCompatActivity) context).findViewById(R.id.finalPoint_Button);;
        transportationTypesContainer = ((AppCompatActivity) context).findViewById(R.id.transportationTypesContainer);
        navOptionsText = ((AppCompatActivity) context).findViewById(R.id.navOptions_Text);
        chooseTextField = ((AppCompatActivity) context).findViewById(R.id.choose_text_field);
        setButtonsOnClick();
        setNavOptionsUiComponents();
    }

    /**
     * This method assigns functionality to the buttons of the view.
     */
    private void setButtonsOnClick() {
        cancelButton.setOnClickListener(v -> handleHiddeItemsView());
        startButton.setOnClickListener(v -> handleAcceptButtonClick());
        chooseStartPointButton.setOnClickListener(v -> handleChooseStartPointButton());
        setPointsButtonShownTexts();
    }

    private void setPointsButtonShownTexts() {
        startPointButton.setText((startPoint != null)?
                formatLatLng(startPoint.getLatitude(),startPoint.getLongitude()):"Your Location");
        finalPointButton.setText((finalPoint != null)?
                formatLatLng(finalPoint.getLatitude(),finalPoint.getLongitude()):"not selected");
    }

    private String formatLatLng(double latitude, double longitude) {
        DecimalFormat decimalFormat = new DecimalFormat("#.######");
        String formattedLatitude = decimalFormat.format(latitude);
        String formattedLongitude = decimalFormat.format(longitude);
        return "Lt: "+formattedLatitude + "\n" + "Lg: "+formattedLongitude;
    }

    private void handleChooseStartPointButton() {
        isStartPointFromMapSelected = true;
        handleHiddeItemsView();
    }

    /**
     * This method handles the actions performed when the cancel button is clicked.
     */
    private void handleHiddeItemsView() {
        mapWayPointController.setMarked(false);
        navOptionsForm.startAnimation(Animations.exitAnimation);
        navOptionsForm.setVisibility(View.GONE);
        mapWayPointController.deleteMarks();
    }

    /**
     * This method handles the actions performed when the accept button is clicked.
     */
    private void handleAcceptButtonClick() {
        handleHiddeItemsView();
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Integer responseCode) {
                handleUploadResponse(responseCode);
            }
        };
        task.execute();
    }
    /**
     * This method handles the response received after uploading the incident to the database.
     *
     * @param responseCode the response code received after uploading the incident
     */
    private void handleUploadResponse(Integer responseCode) {
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Toast.makeText(context, R.string.incidents_uploaded, Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(context, R.string.incidents_not_uploaded, Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * This method assigns values to the incident components.
     *
     * <p>
     *     This method assign values and views to the incident components such as the incident type
     *     spinner, incident estimated time spinner and incident estimated time number picker.
     * </p>
     */
    public void setNavOptionsUiComponents() {
        String[] navOptionsTypes = context.getResources().getStringArray(R.array.navOptions_type);
        String[] navOptionTypeColors = context.getResources().getStringArray(R.array.navOptions_type_colors);
        String[] navOptionTypeIcons = context.getResources().getStringArray(R.array.navOptions_type_icons);

        if (navOptionsTypes.length == navOptionTypeColors.length &&
                navOptionTypeIcons.length == navOptionsTypes.length) {
            for (int i = 0; i < navOptionsTypes.length; i++) {
                Button button = IncidentTypeButton.getIncidentTypeButton(
                        context,
                        navOptionsTypes[i].toLowerCase(),
                        Color.parseColor((String) navOptionTypeColors[i]),
                        navOptionTypeIcons[i]);
                setTransportButtonAction(button);
                transportationTypesContainer.addView(button);
            }
        } else {
            Timber.i(String.valueOf(R.string.array_size_text_timber));
        }
    }


    /**
     * This is a getter method to Incident form layout.
     * @return Return a layout.
     */
    public RelativeLayout getNavOptionsForm() {
        return navOptionsForm;
    }

    /**
     * This method change the title of the type incident, based in a string.
     * @param title new title
     */
    public void changeTypeTitle(String title) {
        navOptionsText.setText(title);
    }

    /**
     * This method set the event to the incident buttons.
     * @param typeButton button to set the event.
     */
    private void setTransportButtonAction(Button typeButton) {
        typeButton.setOnClickListener(
                v -> {
                    IncidentFormController.incidentType = typeButton.getText().toString();
                    changeTypeTitle("Transportation Type: " + typeButton.getText());
                }
        );
    }

    /**
     * This method clear the fields of the form.
     */
    private void clearForm() {
        incidentType = null;
        Objects.requireNonNull(chooseTextField.getEditText()).setText("");
    }

    public boolean isStartPointFromMapSelected() {
        return isStartPointFromMapSelected;
    }

    public void setStartPointFromMapSelected(boolean startPointFromMapSelected) {
        isStartPointFromMapSelected = startPointFromMapSelected;
    }

    public void setStartPoint(LatLng point) {
        startPoint = point;
        updateUiPointsComponents();
    }

    private void updateUiPointsComponents() {
        //isStartPointFromMapSelected = false;
        setPointsButtonShownTexts();
    }

    public void setFinalNavigationPoint(LatLng point) {
        this.finalPoint = point;
        updateUiPointsComponents();
    }
}
