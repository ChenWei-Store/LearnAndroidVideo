package com.learnvideo.capturevideo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by Chenwei on 2017/11/24.
 */

public class PermissionUtils {
    private AfterRequestPermissionCallback callback;
    private Context context;
    private final int permissionRequestCode = 12345;
    private int settingRequestCode = 12346;
    private AlertDialog alertDialog;
    public PermissionUtils(Context context, AfterRequestPermissionCallback callback){
        this.context = context;
        this.callback = callback;
    }

    /**
     * 检查是否需要请求权限
     * @param permission
     * @param activity
     */
    public  void checkPermission(String permission, Activity activity){
        checkCallbackNullable();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            if(context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, new String[]{permission}, permissionRequestCode);
            }else{
                callback.afterRequestPermissionCallback();
            }
        }else {
            callback.afterRequestPermissionCallback();
        }
    }

    public void onGoSettingResult(int requestCode, String permission){
        checkCallbackNullable();
        if(requestCode == settingRequestCode){
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            int result = ContextCompat.checkSelfPermission(context, permission);
            if(result == PackageManager.PERMISSION_GRANTED){
                callback.afterRequestPermissionCallback();
            }
        }

    }

    /**
     * 请求权限回调
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionResult(final Activity activity, String permission,
                                        int requestCode, int[] grantResults, String permissionName){
        checkCallbackNullable();
        if(requestCode == this.permissionRequestCode){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                boolean result = activity.shouldShowRequestPermissionRationale(permission);
                if(!result){
                    alertDialog = new AlertDialog.Builder(activity)
                            .setTitle(permissionName + "权限不可用")
                            .setMessage("请在-应用设置-权限-中，开启"+ permissionName +"权限")
                            .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 跳转到应用设置界面
                                    Intent intent = new Intent();

                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                    intent.setData(uri);

                                    activity.startActivityForResult(intent, settingRequestCode);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setCancelable(false).show();
                }else {

                    callback.afterRequestPermissionCallback();
                }
            }else {

                callback.afterRequestPermissionCallback();
            }
        }
    }

    private void checkCallbackNullable(){
        if(callback == null){
            throw new NullPointerException("AfterRequestPermissionCallback object == null");
        }
    }

    public interface AfterRequestPermissionCallback{
        void afterRequestPermissionCallback();
    }

}
