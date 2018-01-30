package com.example.screen;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyService extends WallpaperService {
    public MyService() {
    }

    @Override
    public Engine onCreateEngine() {
        return new CameraEngine();
    }

    class CameraEngine extends Engine implements android.hardware.Camera.PreviewCallback{
            private Camera camera;
        private int count = 0;
        private long firClick = 0;
        private long secClick = 0;
        private final int interval = 500;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder){
            super.onCreate(surfaceHolder);

            startPreview();
            setTouchEventsEnabled(true);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                count++;
                if (1 == count){
                    firClick = System.currentTimeMillis();
                }else if (2 == count){
                    secClick = System.currentTimeMillis();
                    if (secClick  - firClick <interval){
                        camera.autoFocus(new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                camera.takePicture(null,null,mPicture);
                            }
                        });
                    }else {
                        firClick = secClick;
                        count = 1;
                    }
                    secClick = 0;
                }
            }
            // 时间处理:点击拍照,长按拍照
        }

        private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".jpg");
                try{
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    count = 0;
                    firClick = 0;
                    startPreview();
                }catch (Exception e){
                    Log.i("qwdfdf","保存失败");
                }
            }
        };

        @Override
        public void onDestroy() {
            super.onDestroy();
            stopPreview();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                startPreview();
            } else {
                stopPreview();
            }
        }


        public void startPreview() {
            camera = Camera.open();
            camera.setDisplayOrientation(90);

            try {
                camera.setPreviewDisplay(getSurfaceHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();

        }

        /**
         * 停止预览
         */
        public void stopPreview() {
            if (camera != null) {
                try {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    // camera.lock();
                    camera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camera = null;
            }
        }




        @Override
        public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
                camera.addCallbackBuffer(data);
        }
    }
}
