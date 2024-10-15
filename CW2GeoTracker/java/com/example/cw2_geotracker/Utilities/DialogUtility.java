package com.example.cw2_geotracker.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.StringRes;

import com.example.cw2_geotracker.R;

//Utility class for generating dialogs, based onhttps://developer.android.com/develop/ui/views/components/dialogs
//Available under Apache 2.0 license
public class DialogUtility {

    Context context;

    public DialogUtility(Context context) {
        this.context = context;
    }

    //Shows basic dialog to give user a message, with an Ok button to dismiss
    //(Yes/No dialogs are built in the activity code, due to complexity)
    public void showDialog(@StringRes int id) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(id)
                .setNeutralButton(R.string.confirmation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
