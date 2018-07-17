package com.scqkzqtz.update.update;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scqkzqtz.update.R;


/**
 * 版本更新弹框
 * Created by hghl on 2017/6/22.
 */

public class UpdateDialog extends Dialog {

    private Context context;
    private View binding;
    private String title = "", content = "";
    private TextView dialogTitle;
    private TextView dialogContent;
    private ImageView dialogCancel;
    private Button dialogSure;
    private boolean isCompulsory = false; //是否强制更新
    private ProgressBar progressBar;  // 进度跳
    private TextView tvNumber; //进度
    private LinearLayout lin_pro;

    public interface OnConfirmListener {
        void onSure();

        void onDismiss();
    }

    private OnConfirmListener mConfirmListener;

    public void setConfirmListener(OnConfirmListener mConfirmListener) {
        this.mConfirmListener = mConfirmListener;
    }

    /**
     * @param context
     * @param strTitle     更新头部
     * @param content      更新内容
     * @param isCompulsory 是否强制更新
     */
    public UpdateDialog(Context context, String strTitle, String content, boolean isCompulsory) {
        super(context);
        this.context = context;
        this.title = strTitle;
        this.content = content;
        this.isCompulsory = isCompulsory;
        initView();
    }

    private void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        binding = LayoutInflater.from(context).inflate(R.layout.dialog_update, null, false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(binding, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        initBindView();

        if (isCompulsory) {
            dialogCancel.setVisibility(View.GONE);
        } else {
            dialogCancel.setVisibility(View.VISIBLE);
        }
        dialogTitle.setText(title);
        dialogContent.setText(content);

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context == null || ((Activity) context).isFinishing()) return;
                if (mConfirmListener != null)
                    mConfirmListener.onDismiss();
                dismiss();
            }
        });
        dialogSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmListener != null)
                    mConfirmListener.onSure();
            }
        });
    }

    //findbyid View
    private void initBindView() {
        dialogTitle = binding.findViewById(R.id.dialog_title);
        dialogContent = binding.findViewById(R.id.dialog_content);
        dialogSure = binding.findViewById(R.id.dialog_sure);
        dialogCancel = binding.findViewById(R.id.dialog_cancel);
        progressBar = binding.findViewById(R.id.progressBar);
        tvNumber = binding.findViewById(R.id.tv_number);
        lin_pro = binding.findViewById(R.id.lin_pro);
    }

    //设置进度条进度
    public void setProgressSpeed(int progress) {
        lin_pro.setVisibility(View.VISIBLE);
        progressBar.setProgress(progress);
        tvNumber.setText(progress + " %");
    }

    //设置是否显示和隐藏
    public void setStatus(Boolean isUpdate) {
        if (isUpdate) {
            dialogSure.setText("正在更新");
            dialogSure.setEnabled(false);
            dialogSure.setBackgroundResource(R.drawable.background_red_solid_circle2);
        } else {
            dialogSure.setText("立即升级");
            dialogSure.setBackgroundResource(R.drawable.background_red_solid_circle);
            dialogSure.setEnabled(true);
            lin_pro.setVisibility(View.GONE);
        }
    }
}
