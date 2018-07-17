package com.scqkzqtz.update.update;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.scqkzqtz.update.R;
import com.scqkzqtz.update.utils.PreferenceUtils;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.service.DownloadService;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2018/5/21.
 * sj: 10点35分
 * name: ts
 * 功能：下载apk的工具类
 */

public class UpdateUtils {
    public static final String UpdateData = "updatedate";//最后一次弹框时间
    public static final String UpdateDay = "updateday";//弹框间隔

    private Activity mActivity; //上下文对象
    private String version = ""; //更新的版本号
    private String content = ""; //更新的内容
    private String downloadUrl = ""; //apk的url
    private boolean isCompulsory = false; //是否强制更新
    private boolean isFlag = false; //是否判断时间规则
    private boolean isUpdate = false; //是否正在下载更新

    public UpdateDialog dialog;

    public UpdateUtils(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * 版本号
     *
     * @param version
     * @return
     */
    public UpdateUtils setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * 更新内容
     *
     * @param content
     * @return
     */
    public UpdateUtils setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 更新地址
     *
     * @param downloadUrl
     * @return
     */
    public UpdateUtils setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    /**
     * 是否强制更新
     *
     * @param compulsory
     * @return
     */
    public UpdateUtils setCompulsory(boolean compulsory) {
        isCompulsory = compulsory;
        return this;
    }

    /**
     * 是否判断时间规则
     *
     * @param flag
     * @return
     */
    public UpdateUtils setFlag(boolean flag) {
        isFlag = flag;
        return this;
    }

    /**
     * 开始
     */
    public void start() {
        if (!downloadUrl.equalsIgnoreCase("")) {
            if (isCompulsory) {//强制更新
                showDialog();
                return;
            }
            if (isFlag) {
                String time = PreferenceUtils.getString(UpdateData, "");
                int day = PreferenceUtils.getInt(UpdateDay, 0);
                if (!TextUtils.isEmpty(time) && day != 0) {
                    if ((Integer.valueOf(getTimeDifference(time, getCurrentTime())) / 24) >= day) {
                        showDialog();//满足时间规则展示
                    }
                } else {
                    showDialog(); //第一次显示
                }
            } else {
                showDialog(); //手动点击更新
            }
        }
    }

    /**
     * 展示更新的弹窗
     */
    public void showDialog() {
        dialog = new UpdateDialog(mActivity, mActivity.getString(R.string.app_name) + " v_" + version, content, isCompulsory);
        dialog.setConfirmListener(new UpdateDialog.OnConfirmListener() {
            @Override
            public void onSure() {
                //立即更新
                checkSelfPermission();
            }

            @Override
            public void onDismiss() {
                if (isUpdate) {
                    Toast.makeText(mActivity, "正在后台下载...", Toast.LENGTH_SHORT).show();
                }
                if (isFlag) {
                    PreferenceUtils.commitString(UpdateData, getCurrentTime());
                    int newDay = PreferenceUtils.getInt(UpdateDay, -1);
                    if (newDay == 0) {
                        newDay += 1;
                    } else {
                        newDay += 2;
                    }
                    PreferenceUtils.commitInt(UpdateDay, newDay);
                }
            }
        });
        dialog.show();
    }

    /**
     * //检查权限问题  权限是否打开
     */
    public void checkSelfPermission() {
        AndPermission.with(mActivity)
                .runtime()
                .permission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .rationale(new Rationale<List<String>>() {
                    @Override
                    public void showRationale(Context context, List<String> data, RequestExecutor executor) {
                        executor.execute();
                    }
                })
                .onGranted(permissions -> {
                    if (dialog != null) {
                        dialog.setStatus(isUpdate);
                    }
                    startDownApkFile();
                })
                .onDenied(permissions -> {
                    Toast.makeText(mActivity, "请打开存储权限!", Toast.LENGTH_LONG).show();
                    new PermissionPageUtils(mActivity).jumpPermissionPage();
                })
                .start();
    }

    /**
     * 下载apk
     */
    public void startDownApkFile() {
        String path = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
            try {
                path = mActivity.getExternalCacheDir().getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(path)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            }
        } else {
            path = mActivity.getCacheDir().getAbsolutePath();
        }


        UpdateAppBean updateAppBean = new UpdateAppBean();
        //设置 apk 的下载地址
        updateAppBean.setApkFileUrl(downloadUrl);
        //设置apk 的保存路径
        updateAppBean.setTargetPath(path);
        //实现网络接口，只实现下载就可以
        updateAppBean.setHttpManager(new UpdateAppHttpUtil());

        UpdateAppManager.download(mActivity, updateAppBean, new DownloadService.DownloadCallback() {
            @Override
            public void onStart() {
                dialog.setProgressSpeed(0);
                Log.e("UpdateAppManager", "onStart() called");
            }

            @Override
            public void onProgress(float progress, long totalSize) {
                Log.e("UpdateAppManager", "onProgress() called with: progress = [" + progress + "], totalSize = [" + totalSize + "]");
                dialog.setProgressSpeed((int) (progress * 100));
            }

            @Override
            public void setMax(long totalSize) {
                Log.e("UpdateAppManager", "setMax() called with: totalSize = [" + totalSize + "]");
            }

            @Override
            public boolean onFinish(File file) {
                Log.e("UpdateAppManager", "onFinish() called with: file = [" + file.getAbsolutePath() + "]");
                setUpdate(false);
                PreferenceUtils.commitInt(UpdateDay, 0);
                return true;
            }

            @Override
            public void onError(String msg) {
                setUpdate(false);
                Log.e("UpdateAppManager", "onError() called with: msg = [" + msg + "]");
            }

            @Override
            public boolean onInstallAppAndAppOnForeground(File file) {
                Log.e("UpdateAppManager", "onInstallAppAndAppOnForeground() called with: file = [" + file + "]");
                return false;
            }
        });
        setUpdate(true);
    }

    /**
     * 计算时间差
     *
     * @param starTime 开始时间
     * @param endTime  结束时间
     * @return 返回时间差
     */
    private static String getTimeDifference(String starTime, String endTime) {
        String timeString = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date parse = dateFormat.parse(starTime);
            Date parse1 = dateFormat.parse(endTime);
            long diff = parse1.getTime() - parse.getTime();
            long hour1 = diff / (60 * 60 * 1000);
            timeString = hour1 + "";

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeString;

    }

    /**
     * 获取当前时间
     */
    private static String getCurrentTime() {
        long time = System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        String t1 = format.format(d1);
        Log.e("msg", t1);
        return t1;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
        dialog.setStatus(update);
    }

    public boolean isUpdate() {
        return isUpdate;
    }
}
