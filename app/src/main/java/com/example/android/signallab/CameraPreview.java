package com.example.android.signallab;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.io.IOException;
import java.util.*;

import static android.content.ContentValues.TAG;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int imageFormat;
    private boolean mProcessInProgress = false;
    private Bitmap mBitmap = null;
    private ImageView myCameraPreview;
    private int[] pixels = null;
    public int width = 640, height = 480;
    private Camera.Parameters params;
    private boolean rec = false;
    private ArrayList<double []> arrInt;
    private int frames = 1;
    protected Activity mActivity;
    /** Creates a camera preview */
    public CameraPreview(Context context, Camera camera, ImageView mCameraPreview, LinearLayout layout) {
        super(context);
        mActivity=(Activity)context;
        mCamera = camera;
        params = mCamera.getParameters();
        imageFormat = params.getPreviewFormat();
        //Make sure that the preview size actually exists, and set it to our values
        for (Camera.Size previewSize: mCamera.getParameters().getSupportedPreviewSizes())
        {
            if(previewSize.width == 640 && previewSize.height == 480) { // 640 width och 480 height
                params.setPreviewSize(previewSize.width, previewSize.height);
                break;
            }
        }
        mCamera.setParameters(params);
        myCameraPreview = mCameraPreview;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //myCameraPreview.setId(View.generateViewId());
        layout.addView(myCameraPreview, 1);



    }
    /**Tell the camera where to draw the preview. */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }
    /** Destroys surfaceholder*/
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Taken care of in our activity.
    }
    /** Called upon when surface have changed, sets the preview of display with
     * our holder and starts the preview again.*/
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e){
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();


        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
    /**Called upon when preview frames are displayed */
    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        if (imageFormat == ImageFormat.NV21){
            if(mProcessInProgress){
                mCamera.addCallbackBuffer(bytes);
            }
            if (bytes == null){
                return;
            }
            mCamera.addCallbackBuffer(bytes);
            /*
             * Here we rotate the byte array
             * if your picture is horizontal, delete the rotation of the byte array.
             * */
            bytes = rotateYUV420Degree90(bytes, width, height);

            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(height, width,
                        Bitmap.Config.ARGB_8888);
                myCameraPreview.setImageBitmap(mBitmap);
            }

            myCameraPreview.invalidate();
            mProcessInProgress = true;
            mCamera.addCallbackBuffer(bytes);
            // Start our background thread to process images
            new ProcessPreviewDataTask().execute(bytes);
            //Start calculation of mean values for every other frame
            if (frame_rate() && rec==true) {
                SelectNumbers num = new SelectNumbers();
                arrInt.add(num.pixel_save(bytes));
            }
        }
    }
    /** Start the calculation of mean value by changing rec from false to true
     *  and initializing the array in which mean values are stored.*/
    public void recording(){
        rec = true;
        arrInt = new ArrayList<>();

    }
    /** Stops the calculation of mean value by changing rec from false to true.*/
    public void notRecording(){
        rec = false;
    }
    /**Returns the list of mean values. */
    public ArrayList<double []> send() {
        return arrInt;
    }

    /** Returns true every other frame. */
    private boolean frame_rate(){
        frames= frames+1;
        if (frames%2==0){
            return true;
        }
        return false;
    }
    /**This class hold the method pixel_save that converts the current frame to mean values of R, G and B
     * signals from the little square located on the camera preview.*/
    public class SelectNumbers{
        private double[] pixel_save(byte[]... datas) {
            byte[] data = datas[0];
            ArrayList<Integer> p = new ArrayList<Integer>();
            int tempWidth = 480;
            int tempHeight = 640;
            pixels = decodeYUV420SP(data, tempWidth, tempHeight);
            double rtot = 0;
            double gtot = 0;
            double btot = 0;
            int count = 0;
            for (int i = 0; i < pixels.length; i++) {
                if (i % 480 > 161 && i % 480 < 319 && i > 480 * 481 && i < 480 * 519) {
                    double r = (pixels[i] >> 16) & 0xff;
                    double g = (pixels[i] >> 8) & 0xff;
                    double b = (pixels[i]) & 0xff;
                    rtot = r + rtot;
                    gtot = g + gtot;
                    btot = b + btot;
                    count = count + 1;

                }
            }
            double r_avg = rtot / count;
            double g_avg = gtot / count;
            double b_avg = btot / count;
            double[] mean_value_list = new double[3];
            mean_value_list[0] = r_avg;
            mean_value_list[1] = g_avg;
            mean_value_list[2] = b_avg;

            return mean_value_list;


        }
    }
    /** This class is run on another thread in the background, and when it's done with the decoding,
     * onPostExectue is called to set the new pixel array to the image we have.
     * In doInBackground you can change the values of the RGB pixel array
     * to create the white square in camera preview.*/
    private class ProcessPreviewDataTask extends AsyncTask<byte[], Void, Boolean> {
        @Override
        protected Boolean doInBackground(byte[]... datas) {
            byte[] data = datas[0];
            // I use the tempWidth and tempHeight because of the rotation of the image, if your
            // picture is horizontal, use width and height instead.
            int tempWidth = 480;
            int tempHeight = 640;
            // Here we decode the image to a RGB array.
            pixels = decodeYUV420SP(data, tempWidth, tempHeight);
            /*TODO here you're going to change pixel colors.*/
            int r,g,b;
            for (int i = 0; i < pixels.length; i++) {
                r = (pixels[i] >> 16) & 0xff;
                g = (pixels[i] >> 8) & 0xff;
                b = (pixels[i]) & 0xff;

                //Skapar de två sträcken i longitunell riktning
                if (i % 80 == 0 && i % 160 == 0 && i % 240 != 0 && i > 480*480 && i < 480*520){
                    r = 255;
                    g = 255;
                    b = 255;
                }
                //Skapar sträck i horisontell riktning övre sträck
                if (i>480*480+160 && i < 480*480+320){
                    r = 255;
                    g = 255;
                    b = 255;
                }
                //skapar sträck i horisontell riktning nedre sträck
                if (i>480*520+160 && i < 480*520+320){
                    r = 255;
                    g = 255;
                    b = 255;
                }


                //Visualisera den ruta vi vill spela in

               /* if (i % 480 > 161 && i % 480 < 319  && i > 480*481 && i < 480*519 && rec==true){
                    pixels_save[counter]=pixels[i];
                    counter = counter+1;
                    // r = 255;
                }*/



                pixels[i] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
            mCamera.addCallbackBuffer(data);
            mProcessInProgress = false;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            myCameraPreview.invalidate();
            mBitmap.setPixels(pixels, 0, 480,0, 0, 480, 640); // stride:480 width:480 height 640
            Matrix matrix = new Matrix();
            matrix.setScale( 1,-1);
            matrix.postTranslate( 0, mBitmap.getHeight());
            Bitmap rotatedBitmap = Bitmap.createBitmap(mBitmap, 0,0,480,640,matrix,true);
            myCameraPreview.setImageBitmap(rotatedBitmap);



        }
    }


    /** Decoding and rotating methods from github
     * This method rotates the NV21 image (standard image that comes from the preview)
     * since this is a byte array, it must be switched correctly to match the pixels*/

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
    /** Decodes the image from the NV21 format into an RGB-array with integers.
     * Since the NV21 array is made out of bytes, and one pixel is made out of 1.5 bytes, this is
     * quite hard to understand. If you want more information on this you can read about it on
     * */
    public int[] decodeYUV420SP(byte[] yuv, int width, int height) {

        final int frameSize = width * height;

        int rgb[] = new int[width * height];
        final int ii = 0;
        final int ij = 0;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, ci = ii; i < height; ++i, ci += di) {
            for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
                int y = (0xff & ((int) yuv[ci * width + cj]));
                int v = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
                int u = (0xff & ((int) yuv[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return rgb;
    }


}
