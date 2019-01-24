package com.example.ycb.mywuzi.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.ycb.mywuzi.R;

/**
 * Created by biao on 2018/5/14.
 * 下载进度弹窗
 */

public class DownloadDialog extends Dialog {

    private ProgressBar progressBar;

    private TextView tv_bottom;

    public DownloadDialog(@NonNull Context context) {
        super(context);
        init();
    }

    public void setProcess(int process){
        progressBar.setProgress(process);

        if(process == 100){
            dismiss();
        }
    }

    private void init() {
        setContentView(R.layout.download_dialog);
        setCanceledOnTouchOutside(false);
        progressBar = findViewById(R.id.progressBar);
        tv_bottom = findViewById(R.id.tv_bottom);
    }
}
