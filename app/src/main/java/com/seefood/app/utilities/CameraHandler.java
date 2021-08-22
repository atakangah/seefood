package com.seefood.app.utilities;

/*
 * Copyright 2017 The Android Things Samples Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.seefood.app.overrides.AutoFitTextureView;

/**
 * CameraHandler handles the Camera2 Android APIs, including initializing, configuring the actual
 * camera and requesting a new image. ImagePreprocessor converts the image into a format compatible
 * with the TensorFlow model.
 */
public class CameraHandler {
    private static final String TAG = "DEBUG";

    private static final int MAX_IMAGES = 2;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;

    private String mCameraId;
    private Context mContext;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mPreviewReader;
    private AutoFitTextureView mTextureView;
    private Surface mSurface;
    private ImageReader.OnImageAvailableListener mImageAvailableListener;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraManager mCameraManager;

    private Size previewSize;
    private static final Size inputSize = new Size(640, 640);
    private static final int MINIMUM_PREVIEW_SIZE = 320;

    private Integer mDeviceOrientation;
    private Integer mCameraSensorOrientation;

    private PreviewDimChosen cameraConnectionCallback;
    private Handler mBackgroundHandler;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private int FLASH_MODE = CaptureRequest.FLASH_MODE_OFF;


    interface PreviewDimChosen {
        void onPreviewSizeChosen(Size size, int sensorOrientation);

        int getDisplayOrientation();
    }


    public final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(final SurfaceTexture texture, final int width, final int height) {
                    startCamera(width, height);
                    Log.d(TAG, "surfaceavailable");
                }

                @Override
                public void onSurfaceTextureSizeChanged(final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height);
                    Log.d(TAG, "surface changed");
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };


    /**
     * Callback handling device state changes
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "Opened camera.");
            mCameraDevice = cameraDevice;
            preparePreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "Camera disconnected, closing.");
            closeCaptureSession();
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            Log.d(TAG, "Camera device error, closing.");
            closeCaptureSession();
            cameraDevice.close();
        }

        @Override
        public void onClosed(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "Closed camera, releasing");
            mCameraDevice = null;
        }
    };


    private static class InstanceHolder {
        private static CameraHandler mCamera = new CameraHandler();
    }

    public static CameraHandler getInstance() {
        return InstanceHolder.mCamera;
    }

    public void initializeCamera(Context context,
                                 int orientation,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageAvailableListener,
                                 AutoFitTextureView textureView, PreviewDimChosen callback) {

        mBackgroundHandler = backgroundHandler;
        mTextureView = textureView;
        mImageAvailableListener = imageAvailableListener;
        mContext = context;
        mDeviceOrientation = orientation;
        cameraConnectionCallback = callback;
    }

    @SuppressLint("MissingPermission")
    public void startCamera(int width, int height) {

        // Discover the camera instance
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = mCameraManager.getCameraIdList();

            if (camIds.length < 1) {
                Log.d(TAG, "No cameras found");
                return;
            }

            String id = camIds[0];
            mCameraId = id;

        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs", e);
        }

        setUpCameraOutputs();
        configureTransform(width, height);

        // Open the camera resource
        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException cae) {
            Toast.makeText(mContext, "Camera access exception" + cae, Toast.LENGTH_LONG).show();
        }
    }

    public void toggleFlash(int flashState) {
        this.FLASH_MODE = flashState;
        preparePreviewSession();
    }


    private void setUpCameraOutputs() {
        final CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);

            final StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // For still image captures, we use the largest available size.
            final Size largest =
                    Collections.max(
                            Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                            new CompareSizesByArea());

            mCameraSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            previewSize =
                    chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                            largest.getWidth(),
                            largest.getHeight());

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            if (mDeviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                mTextureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                mTextureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }
        } catch (final CameraAccessException e) {
            Log.d(TAG, e.toString());
        } catch (final NullPointerException e) {
            Log.d(TAG, "NULLPOINTER... NO CAMERA2 SUPPORT");
        }

        cameraConnectionCallback.onPreviewSizeChosen(previewSize, mDeviceOrientation);
    }


    private void configureTransform(final int viewWidth, final int viewHeight) {
        if (null == mTextureView || null == previewSize) {
            return;
        }
        final int rotation = cameraConnectionCallback.getDisplayOrientation();
        Log.d(TAG, "device rotation" + rotation);
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {

            Log.d(TAG, "horizontal device orientation");

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());

            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            final float scale = Math.max((float) viewHeight / previewSize.getHeight(), (float) viewWidth / previewSize.getWidth());

            Log.d(TAG, "scale cals" + (float) viewHeight / previewSize.getHeight() + " and " + (float) viewWidth / previewSize.getWidth());

            matrix.postScale(scale, scale, centerX, centerY);

            matrix.postRotate(90 * (rotation - 2), centerX, centerY);

        } else if (Surface.ROTATION_180 == rotation) {
            Log.d(TAG, "vertical device orientation");

            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the minimum of both, or an exact match if possible.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width   The minimum desired width
     * @param height  The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        Log.i(TAG, "Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        Log.i(TAG, "Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        Log.i(TAG, "Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");


        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.max(bigEnough, new CompareSizesByArea());
            return chosenSize;
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    private void preparePreviewSession() {
        if (mCameraDevice == null) {
            Log.w(TAG, "Cannot capture image. Camera not initialized.");
            return;
        }
        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            final SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            mSurface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mSurface);

            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, this.FLASH_MODE);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            // Finally, we start displaying the camera preview.
            mPreviewRequest = mPreviewRequestBuilder.build();

            mCameraDevice.createCaptureSession(
                    Arrays.asList(mSurface),
                    mSessionCallback,
                    mBackgroundHandler
            );

        } catch (CameraAccessException cae) {
            Log.d(TAG, "access exception while preparing pic", cae);
        }
    }


    /**
     * Callback handling session state changes
     */
    private CameraCaptureSession.StateCallback mSessionCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // The camera is already closed
                    if (mCameraDevice == null) {
                        return;
                    }
                    Log.d(TAG, "ON CONFIGURED");

                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = cameraCaptureSession;

                    try {
                        mCaptureSession.setRepeatingRequest(
                                mPreviewRequest,
                                new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureProgressed(
                                            final CameraCaptureSession session,
                                            final CaptureRequest request,
                                            final CaptureResult partialResult)
                                    { }

                                    @Override
                                    public void onCaptureCompleted(
                                            final CameraCaptureSession session,
                                            final CaptureRequest request,
                                            final TotalCaptureResult result)
                                    { }
                                }, mBackgroundHandler);

                    } catch (CameraAccessException cae) {
                        Log.d(TAG, "Continuous preview access failed", cae);
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "Failed to configure camera");
                }
            };

    /**
     * Execute a new capture request within the active session
     */
    public void triggerImageCapture() {
        try {
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            closeCaptureSession();

            mPreviewReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, MAX_IMAGES);
            mPreviewReader.setOnImageAvailableListener(mImageAvailableListener, mBackgroundHandler);

            mCameraDevice.createCaptureSession(
                    Arrays.asList(mPreviewReader.getSurface()),
                    //Arrays.asList(mSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                            mCaptureSession = cameraCaptureSession;

                            try {
                                final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                // captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 360);
                                captureBuilder.addTarget(mPreviewReader.getSurface());
                                // Log.d(TAG, "orientation: " + mCameraSensorOrientation);
                                mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException cae) {
                                Log.d(TAG, cae.toString());
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    },
                    mBackgroundHandler
            );


        } catch (CameraAccessException cae) {

            Log.d(TAG, "camera capture exception L2");
        }
    }


    private int getOrientation() {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        Log.d(TAG, "Sensor orientation " + mCameraSensorOrientation);
        return (ORIENTATIONS.get(mDeviceOrientation) + mCameraSensorOrientation + 270) % 360;
    }


    /**
     * Callback handling capture session events
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                                @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    Log.d(TAG, "Partial result");
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    closeCaptureSession();
                    Log.d(TAG, "CaptureSession closed");
                }
            };


    private void closeCaptureSession() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.close();
            } catch (Exception ex) {
                Log.e(TAG, "Could not close capture session", ex);
            }
            mCaptureSession = null;
        }
    }


    /**
     * Close the camera resources
     */
    public void shutDown() {
        closeCaptureSession();
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }


    /**
     * Helpful debugging method:  Dump all supported camera formats to log.  You don't need to run
     * this for normal operation, but it's very helpful when porting this code to different
     * hardware.
     */
    public static void dumpFormatInfo(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Log.d(TAG, "No cameras found");
        }
        String id = camIds[0];
        Log.d(TAG, "Using camera id " + id);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            StreamConfigurationMap configs = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            for (int format : configs.getOutputFormats()) {
                Log.d(TAG, "Getting sizes for format: " + format);
                for (Size s : configs.getOutputSizes(format)) {
                    Log.d(TAG, "\t" + s.toString());
                }
            }
            int[] effects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
            for (int effect : effects) {
                Log.d(TAG, "Effect available: " + effect);
            }
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting characteristics.");
        }
    }
}
