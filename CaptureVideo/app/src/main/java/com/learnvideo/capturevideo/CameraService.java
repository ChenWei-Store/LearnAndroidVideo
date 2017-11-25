package com.learnvideo.capturevideo;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;



import java.util.List;

/**
 * Created by Chenwei on 2017/11/8.
 */
public class CameraService {
    private static CameraService cameraService;

    private Camera camera;
    private static final String TAG = CameraService.class.getSimpleName();

    private int imgWidth = 1920;
    private int imgHeight = 1080;
    private boolean isStart;
    private CameraService(){
    }

    public static CameraService getInstance(){
        if(cameraService == null){
            synchronized (CameraService.class){
                if(cameraService == null){
                    cameraService = new CameraService();
                }
            }
        }
        return cameraService;
    }

    public void open(Context context, int cameraId, Camera.PreviewCallback callback
    , SurfaceHolder surfaceHolder){
        if(camera == null) {
            Log.d(TAG, "open camera");
            try {
                camera = Camera.open(cameraId);
                if(camera == null) {
                    return;
                }
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = calBestPreviewSize(parameters, imgWidth,
                        imgHeight);
                parameters.setPictureSize(size.width, size.height);
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPreviewFormat(ImageFormat.NV21);
                int orientation = context.getResources().getConfiguration().orientation;
//                if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    camera.setDisplayOrientation(180);
//                }else
                if(orientation == Configuration.ORIENTATION_PORTRAIT) {
                    camera.setDisplayOrientation(90);
                }
                camera.setPreviewDisplay(surfaceHolder);
                camera.setPreviewCallback(callback);
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }
    }

    public void open(Context context, int cameraId, Camera.PreviewCallback callback
           , SurfaceTexture surfaceTexture){
        if(camera == null) {
            Log.d(TAG, "open camera");
            try {
                camera = Camera.open(cameraId);
                if(camera == null) {
                    return;
                }
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = calBestPreviewSize(parameters, imgWidth,
                        imgHeight);
                parameters.setPictureSize(size.width, size.height);
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPreviewFormat(ImageFormat.NV21);
                int orientation = context.getResources().getConfiguration().orientation;
//                if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    camera.setDisplayOrientation(180);
//                }else
                if(orientation == Configuration.ORIENTATION_PORTRAIT) {
                    camera.setDisplayOrientation(90);
                }
                camera.setPreviewTexture(surfaceTexture);
                camera.setPreviewCallback(callback);
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }
    }

    public void close(){
        if(camera != null){
            Log.d(TAG, "closeCamera");
            isStart = false;

            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }




    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */

    public Camera.Size calBestPreviewSize(Camera.Parameters camPara, int w, int h) {
        List<Camera.Size> sizes = camPara.getSupportedPreviewSizes();
        if (sizes == null)
            return null;

        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;


        Camera.Size optimalSize = null;

        // Start with max value and refine as we iterate over available preview sizes. This is the
        // minimum difference between view and camera height.
        double minDiff = Double.MAX_VALUE;

        // Target view height
        int targetHeight = h;

        // Try to find a preview size that matches aspect ratio and the target view size.
        // Iterate over all available sizes and pick the largest size that can fit in the view and
        // still maintain the aspect ratio.
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public Camera getCamera(){
        return camera;
    }

    public void startPreview(){
        if(camera == null){
            Log.e(TAG, "startPreview: camera == null");
            return;
        }
        if(!isStart) {
            Log.d(TAG, "startPreview");
            camera.startPreview();
            isStart = true;
        }
    }
}
