package com.isc.hermes.controller.PopUp;

import android.app.Activity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

/**
 * A class that represents a popup dialog for editing an account.
 * Extends the PopUp class.
 */
public class PopUpEditAccount extends PopUp{

    private Button button;
    private AutoCompleteTextView fullName;
    private AutoCompleteTextView username;
    private AutoCompleteTextView comboBoxField;

    /**
     * Warning Popup constructor class within which the dialog, activity and buttons are initialized
     *
     * @param activity  The activity we were in before the popup opened
     */
    public PopUpEditAccount(Activity activity) {
        super(activity, TypePopUp.EDIT_POP_UP);
    }

    /**
     * Handles the click event for the view.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == super.confirmButton) {
            button.setVisibility(View.INVISIBLE);
            fullName.setEnabled(false);
            username.setEnabled(false);
            comboBoxField.setEnabled(false);
        } dismiss();
    }

    /**
     * Sets the button and text fields for the account edit.
     *
     * @param button    The button for account editing.
     * @param fullName  The text field for the full name.
     * @param username  The text field for the username.
     * @param comboBoxField The text field fo combo-box.
     */
    public void setInformationToAbelEdit(Button button, AutoCompleteTextView fullName,
                                         AutoCompleteTextView username, AutoCompleteTextView comboBoxField) {
        this.button = button;
        this.fullName = fullName;
        this.username = username;
        this.comboBoxField = comboBoxField;
    }
}