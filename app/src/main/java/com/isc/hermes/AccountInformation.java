package com.isc.hermes;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.isc.hermes.controller.PopUp.PopUpDeleteAccount;
import com.isc.hermes.controller.PopUp.PopUpEditAccount;
import com.isc.hermes.controller.Utiils.ImageUtil;

import java.io.IOException;


import com.isc.hermes.controller.PopUp.PopUp;
import com.isc.hermes.model.User;

/**
 * This class represents the AccountInformation activity, which displays information about the account.
 */
public class AccountInformation extends AppCompatActivity {

    private Button buttonSaveInformation;
    private AutoCompleteTextView textFieldUserName;
    private AutoCompleteTextView textFieldFullName;
    private AutoCompleteTextView comboBoxField;
    private AutoCompleteTextView textFieldEmail;
    private ImageView imageView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private User userRegistered;
    private PopUpEditAccount popUpDialogEdit;
    private PopUp popUpDialogDelete;

    /**
     * Generates components for the combo box and returns the AutoCompleteTextView.
     * Creates an array of items for the combo box, sets up an ArrayAdapter,
     * and attaches it to the AutoCompleteTextView component to provide suggestions.
     *
     * @return The generated AutoCompleteTextView for the combo box.
     */
    private AutoCompleteTextView generateComponentsToComboBox() {
        String[] items = {"Administrator", "General"};
        ArrayAdapter<String> adapterItems = new ArrayAdapter<>(this, R.layout.combo_box_item, items);
        comboBoxField.setAdapter(adapterItems);
        return comboBoxField;
    }

    /**
     * Generates an action for the combo box.
     * Sets an item click listener on the AutoCompleteTextView component of the combo box,
     * which captures the selected item and updates the user's type accordingly.
     */
    private void generateActionToComboBox() {
        generateComponentsToComboBox().setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            Toast.makeText(getApplicationContext(), "Item: " + item,
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void updateTextFieldsByUser() {
        imageView.setImageURI(Uri.parse(userRegistered.getPathImageUser()));
        textFieldUserName.setText(userRegistered.getUserName());
        textFieldFullName.setText(userRegistered.getFullName());
        textFieldEmail.setText(userRegistered.getEmail());
        comboBoxField.setText(userRegistered.getTypeUser());
    }

    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState The saved instance state bundle, containing the activity's previous state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_information);
        assignValuesToComponentsView();
        generateActionToComboBox();
        getUserInformation();
        updateTextFieldsByUser();
        initializePopups();
    }

    /**
     * Sends a User object to another activity using an Intent.
     *
     * @param user The User object to be sent to the other activity.
     */
    private void sendUserBetweenActivities(User user) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userObtained", user);
        startActivity(intent);
    }

    /**
     * Navigates to the principal view within the current activity.
     *
     * @param view The view that triggers the navigation.
     */
    public void goToPrincipalView(View view) {
        sendUserBetweenActivities(userRegistered);
    }

    /**
     * This method is used to delete an account using on click action.
     */
    public void deleteAccountAction(View view) {
        popUpDialogDelete.show();
    }

    /**
     * This method is used to open gallery for then select the image account information using on click action.
     *
     * @param view The view that triggers the navigation.
     */
    public void uploadImageProfileAction(View view) {
        openGallery();
    }

    /**
     * Opens the gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the activity for selecting an image from the gallery.
     *
     * @param requestCode The request code for the activity.
     * @param resultCode  The result code from the activity.
     * @param data        The intent containing the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                Bitmap croppedBitmap = ImageUtil.cropToSquare(bitmap);

                imageView.setImageBitmap(croppedBitmap);
            } catch (IOException e) {e.printStackTrace();}
        }
    }

    /**
     * Retrieves the user information passed through the intent.
     * Gets the Parcelable "userObtained" extra from the intent and assigns it to the userRegistered variable.
     */
    private void getUserInformation() {
        Intent intent = getIntent();
        userRegistered = intent.getParcelableExtra("userObtained");
    }

    /**
     * This method is used to edit an account information using on click action.
     *
     * @param view The view that triggers the navigation.
     */
    public void editAccountAction(View view) {
        buttonSaveInformation.setVisibility(View.VISIBLE);
        textFieldFullName.setEnabled(true);
        comboBoxField.setEnabled(true);
        textFieldUserName.setEnabled(true);
    }

    private  void updateInformationUser() {
        userRegistered.setTypeUser(String.valueOf(comboBoxField.getText()));
        userRegistered.setUserName(String.valueOf(textFieldUserName.getText()));
        userRegistered.setFullName(String.valueOf(textFieldFullName.getText()));
    }
    /**
     * This method is used to edit an account information using on click action.
     *
     * @param view The view that triggers the navigation.
     */
    public void saveAccountInformationAction(View view) {
        updateInformationUser();
        popUpDialogEdit.setInformationToAbelEdit(buttonSaveInformation, textFieldFullName,
                textFieldUserName, comboBoxField);
        popUpDialogEdit.show();
    }

    /**
     * This method initialize the popup warning when we pressed on the delete account button
     */
    private void initializePopups(){
        this.popUpDialogEdit = new PopUpEditAccount(this);
        this.popUpDialogDelete = new PopUpDeleteAccount(this);
    }

    /**
     * Assigns values to various components/views in the activity or fragment.
     * This method initializes the corresponding variables with the view IDs retrieved from the layout XML file
     * and disables the text fields initially.
     */
    private void  assignValuesToComponentsView() {
        buttonSaveInformation =  findViewById(R.id.buttonSaveInformation);
        imageView = findViewById(R.id.imageUpload);
        comboBoxField = findViewById(R.id.textFieldUserType);
        textFieldFullName = findViewById(R.id.textFieldFullName);
        textFieldEmail = findViewById(R.id.textFieldEmail);
        textFieldUserName = findViewById(R.id.textFieldUserName);
        textFieldFullName.setEnabled(false);
        textFieldUserName.setEnabled(false);
        comboBoxField.setEnabled(false);
    }
}