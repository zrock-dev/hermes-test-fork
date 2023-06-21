package com.isc.hermes.controller.PopUp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import com.isc.hermes.SignUpActivityView;
import com.isc.hermes.database.AccountInfoManager;

/**
 * A class that represents a popup dialog for deleting an account.
 * Extends the PopUp class.
 */
public class PopUpDeleteAccount extends PopUp{

    /**
     * Warning Popup constructor class within which the dialog, activity and buttons are initialized
     *
     * @param activity The activity we were in before the popup opened
     */
    public PopUpDeleteAccount(Activity activity) {
        super(activity, TypePopUp.DELETE_POP_UP);
    }

    /**
     * Handles the click event for the view.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v == super.confirmButton){
            AccountInfoManager accountInfoManager = new AccountInfoManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                accountInfoManager.deleteUser("6492381ce2a844fa31b4cc14");
                System.out.println("SDfaaaaaaaaaaaaaaaaaaaaaaaaaa");
            }googleAuthentication.revokeAccess(getContext());
            Intent intent = new Intent(this.activity, SignUpActivityView.class);
            activity.startActivity(intent);
        } dismiss();
    }
}
