package com.learnvideo.androidcodecdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Chenwei on 2017/11/26.
 */

public class LoadingDialog extends AlertDialog {


    protected LoadingDialog(@NonNull Context context) {
        super(context);
    }

    protected LoadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, null);
        setContentView(rootView);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }


}
