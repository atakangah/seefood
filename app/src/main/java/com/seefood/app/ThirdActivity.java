package com.seefood.app;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import com.seefood.app.views.AutoFitTextureView;

public class ThirdActivity extends AppCompatActivity implements CameraHandler.PreviewDimChosen {
    private static final String TAG = "DEBUG";

    private static final String LABELS_FILE = "seefoodlabel.txt";
    private static final String MODEL_FILE = "seefoodmodel.tflite";

    private final int CAMERA_REQUEST_ID = 12;
    private boolean flashState = false;
    private boolean hasFlash = false;

    private CameraHandler mCameraHandler;
    private MyImageProcessor mImagePreprocessor;

    private AutoFitTextureView mTextureView;
    private TextView mScanBtn;
    private ImageView mFlashBtn;

    private boolean snapClicked = false;

    private Interpreter mTensorFlowLite;
    private List<String> mLabels;

    public static Collection<Recognition> mRecognitionResults;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private int previewWidth;
    private int previewHeight;
    private Bitmap rgbFrameBitmap;

    private int mRotation;

    @Override
    public void onPreviewSizeChosen(Size size, int rotation) {

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
        mRotation = rotation;

        try {
            mImagePreprocessor = new MyImageProcessor(this);
            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);

        } catch (IOException modelIOException) {
            Toast.makeText(this, "ML Model File Not Fouond", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getDisplayOrientation() {
        return getWindowManager().getDefaultDisplay().getRotation();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextureView = findViewById(R.id.textureView);
        mScanBtn = findViewById(R.id.scanBtn);
        mFlashBtn = findViewById(R.id.flashBtn);

        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_ID);

        hasFlash = getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        mFlashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlash();
            }
        });

        initClassifier();
    }


    @Override
    public void onResume() {
        super.onResume();
        //Animate Processing Status Text hiding
        TextView statusTxt = findViewById(R.id.processingTxt);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                statusTxt,
                PropertyValuesHolder.ofFloat("translationY", -125f)
        );
        animator.setDuration(500);
        animator.start();

        //toggle controls click state
        mScanBtn.setClickable(true);
        mFlashBtn.setClickable(true);

        //start the background handler
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).

        if (mTextureView.isAvailable()) {
            Log.d(TAG, "TEXTURE VIEW AVAILABLE");
            initCamera();
            mCameraHandler.startCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mCameraHandler = CameraHandler.getInstance();
            initCamera();
            mTextureView.setSurfaceTextureListener(mCameraHandler.surfaceTextureListener);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        stopBackgroundThread();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            destroyClassifier();
            closeCamera();
        } catch (Throwable t) {
            // close quietly
        }
    }


    /**
     * Clean up the resources used by the classifier.
     */
    private void destroyClassifier() {
        mTensorFlowLite.close();
    }


    /**
     * Image capture process complete
     */
    private void onPhotoCaptured(final Bitmap bitmap) {
        toggleSnapClick();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final List<Recognition> results = mImagePreprocessor.recognizeImage(bitmap, getScreenOrientation());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onClassificationComplete(results);
                    }
                });
                Log.d(TAG, results.toArray().toString());
            }
        });
    }


    /**
     * Process an image and identify what is in it. When done, the method
     * {@link #onClassificationComplete(Collection)} must be called with the results of
     * the image recognition process.
     *
     * @param image Bitmap containing the image to be classified. The image can be
     *              of any size, but preprocessing might occur to resize it to the
     *              format expected by the classification process, which can be time
     *              and power consuming.
     */
    private void doIdentification(Bitmap image) {


        // Report the results with the highest confidence

    }


    /**
     * Image classification process complete
     */
    private void onClassificationComplete(Collection<Recognition> results) {

        mRecognitionResults = results;

        final Intent processIntent = new Intent(ThirdActivity.this, ResultsActivity.class);
        startActivity(processIntent);
    }


    /**
     * Initialize the camera that will be used to capture images.
     */
    private void initCamera() {
        mCameraHandler.initializeCamera(
                this,
                getResources().getConfiguration().orientation,
                mBackgroundHandler,
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Log.d(TAG, "On Image Available");
                        if (snapClicked) {
                            Image image = imageReader.acquireNextImage();

                            //CONVERT IMAGE TO BITMAP
                            rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                            ByteBuffer bb = image.getPlanes()[0].getBuffer();
                            rgbFrameBitmap = BitmapFactory.decodeStream(new MyImageProcessor.ByteBufferBackedInputStream(bb));
                            onPhotoCaptured(rgbFrameBitmap);
                        }
                    }
                }, mTextureView, this);
    }


    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initialize the classifier that will be used to process images.
     */
    private void initClassifier() {
        try {
            mTensorFlowLite = new Interpreter(TensorFlowHelper.loadModelFile(this, MODEL_FILE));
            mLabels = TensorFlowHelper.readLabels(this, LABELS_FILE);
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize TensorFlow Lite.", e);
        }
    }


    /**
     * Clean up resources used by the camera.
     */
    private void closeCamera() {
        mCameraHandler.shutDown();
    }

    /**
     * Load the image that will be used in the classification process.
     * When done, the method {@link #onPhotoCaptured(Bitmap)} must be called with the image.
     */
    private void takePhoto() {
        toggleSnapClick();
        //Animate Processing Status Text
        TextView statusTxt = findViewById(R.id.processingTxt);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                statusTxt,
                PropertyValuesHolder.ofFloat("translationY", 5f)
        );
        animator.setDuration(500);
        animator.start();

        //toggle controls click state
        mScanBtn.setClickable(false);
        mFlashBtn.setClickable(false);

        mCameraHandler.triggerImageCapture();
    }

    private void toggleFlash() {

        if (hasFlash) {
            this.flashState = !this.flashState;
            /*
             * Set Image according to flashState. If true, set flash on image
             * If false set flash off image
             */
            this.mFlashBtn.setImageResource(this.flashState ? R.drawable.ic_baseline_flash_on_24 : R.drawable.ic_baseline_flash_off_24);
            mCameraHandler.toggleFlash(this.flashState ? CaptureRequest.FLASH_MODE_TORCH : CaptureRequest.FLASH_MODE_OFF);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_ID:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
                } else {
                    // TO-DO
                    // DISABLE FLASH BUTTON FOR USER
                    mFlashBtn.setEnabled(false);
                    Toast.makeText(this, "Permission denied for camera access", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void toggleSnapClick() {
        snapClicked = !snapClicked;
    }

    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }
}
