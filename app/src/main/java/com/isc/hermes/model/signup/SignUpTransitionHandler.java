package com.isc.hermes.model.signup;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.isc.hermes.database.SendEmailManager;
import com.isc.hermes.model.Validator;
import com.isc.hermes.model.user.User;
import com.isc.hermes.model.user.UserRoles;

/**
 * This class manages the transitions in the sign up process.
 */
public class SignUpTransitionHandler {

    /**
     * Launch's another activity, based on a role.
     *
     * @param user           UserRole such as Administrator or General
     * @param packageContext the context, so the activity can be launched.
     */
    public void transitionBasedOnRole(User user, Context packageContext) {
        Intent intent = new Intent(packageContext, RoleTransitionRepository.getInstance().get(user.getRole()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendVerificationCode(user.getRole(), user.getEmail());
        }
        packageContext.startActivity(intent);
    }

    /**
     * Sends a verification code to the specified email address, based on the user roles,
     * only "Administrator".
     *
     * @param roles The user roles object containing the role information.
     * @param email The email address to which the verification code will be sent.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendVerificationCode(UserRoles roles, String email) {
        if (roles.getRole().equals("Administrator")) {
            Validator validator = Validator.getValidator();
            validator.obtainVerificationCode();
            SendEmailManager sendEmailManager = new SendEmailManager();
            sendEmailManager.addEmail(email, validator.getCode());
            validator.setEmail(email);
        }
    }
}
