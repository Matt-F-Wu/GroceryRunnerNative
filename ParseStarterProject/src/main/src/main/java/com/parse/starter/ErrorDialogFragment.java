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

    private String nag_button, pos_button;

    private FCallback fCallback;

    public void setfCallback(FCallback callback){
        this.fCallback = callback;
    }

    public void setMsg(String Msg){
        this.msg = Msg;
    }

    public void setNag(String nag){
        this.nag_button = nag;
    }

    public void setPos(String pos){
        this.pos_button = pos;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String pos_w = "Okay";

        if (pos_button != null){
            pos_w = pos_button;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)  /*Hao: Set this to a sad face to be cuter*/
                .setMessage(msg)
                .setPositiveButton(pos_w,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                /*Not sure what to do yet*/
                                if (fCallback != null) {
                                    fCallback.callBack();
                                }
                            }
                        }
                );

        if(nag_button != null ){
            builder.setNegativeButton(nag_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /*Don't do anything*/
                }
            });
        }

        return builder.create();
    }
}

