package com.parse.starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Administrator on 2015/12/18.
 */
public class ErrorDialogFragment extends DialogFragment {
    private String msg;

    public void setMsg(String Msg){
        this.msg = Msg;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(msg)
                .setPositiveButton("Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                /*Not sure what to do yet*/
                            }
                        }
                ).create();
    }
}

