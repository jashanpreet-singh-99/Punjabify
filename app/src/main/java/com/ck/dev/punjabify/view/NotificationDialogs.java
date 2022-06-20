package com.ck.dev.punjabify.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ck.dev.punjabify.R;

public class NotificationDialogs extends Dialog  {

    private Button   cancelBtn;
    private Button   okBtn;
    private TextView titleTxt;
    private TextView infoTxt;

    private String title;
    private String info;
    private String cancelBtnTxt;
    private String okBtnTxt;

    private boolean cancelBtnVisible;
    private boolean okBtnVisible;

    public NotificationDialogs(@NonNull Context context, int themeResId, String title, String info, String cancelBtnTxt, String okBtnTxt, boolean cancelBtnVisible, boolean okBtnVisible) {
        super(context, themeResId);
        this.title = title;
        this.info = info;
        this.cancelBtnTxt = cancelBtnTxt;
        this.okBtnTxt = okBtnTxt;
        this.cancelBtnVisible = cancelBtnVisible;
        this.okBtnVisible = okBtnVisible;
    }

    public NotificationDialogs(@NonNull Context context, String title, String info, String cancelBtnTxt, String okBtnTxt, boolean cancelBtnVisible, boolean okBtnVisible) {
        super(context);
        this.title = title;
        this.info = info;
        this.cancelBtnTxt = cancelBtnTxt;
        this.okBtnTxt = okBtnTxt;
        this.cancelBtnVisible = cancelBtnVisible;
        this.okBtnVisible = okBtnVisible;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notification);
        cancelBtn = this.findViewById(R.id.dialog_cancel_btn);
        okBtn     = this.findViewById(R.id.dialog_ok_btn);
        titleTxt  = this.findViewById(R.id.dialog_title);
        infoTxt   = this.findViewById(R.id.dialog_information);

        titleTxt.setText(title);
        infoTxt.setText(info);
        okBtn.setText(okBtnTxt);
        cancelBtn.setText(cancelBtnTxt);

        btnVisible(okBtnVisible, okBtn);
        btnVisible(cancelBtnVisible, cancelBtn);
    }

    private void btnVisible(boolean stat, Button btn) {
        if (stat) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.GONE);
        }
    }

    public void setTitleTxt(String title) {
        titleTxt.setText(title);
    }

    public void setInformationTxt(String information) {
        infoTxt.setText(information);
    }

    public void setCancelBtnListener(View.OnClickListener onClickListener) {
        cancelBtn.setOnClickListener(onClickListener);
    }

    public void setOkBtnListener(View.OnClickListener onClickListener) {
        okBtn.setOnClickListener(onClickListener);
    }

    public void setCancelBtnVisibility(int mode) {
        cancelBtn.setVisibility(mode);
    }

    public void setOkBtnVisibility(int mode) {
        okBtn.setVisibility(mode);
    }


}
