/**
 * Copyright © 2016 Viro Media. All rights reserved.
 */
package com.viromedia.bridge.component;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.viro.renderer.jni.Image;
import com.viro.renderer.jni.PortalScene;
import com.viro.renderer.jni.Texture.TextureFormat;
import com.viro.renderer.jni.Texture;
import com.viro.renderer.jni.Vector;
import com.viromedia.bridge.component.node.VRTNode;
import com.viromedia.bridge.component.node.VRTScene;
import com.viromedia.bridge.utility.ImageDownloadListener;
import com.viromedia.bridge.utility.ImageDownloader;
import com.viromedia.bridge.utility.ViroEvents;

public class VRT360Image extends VRTNode {
    private static final float[] sDefaultRotation = {0, 0, 0};

    private ReadableMap mSourceMap;
    private float[] mRotation = sDefaultRotation;
    private Image mLatestImage;
    private Texture mLatestTexture;
    private String mStereoMode;
    private TextureFormat mFormat = TextureFormat.RGBA8;
    private Handler mMainHandler;
    private boolean mImageNeedsDownload;
    private Image360DownloadListener mDownloadListener;

    public VRT360Image(ReactApplicationContext context) {
        super(context);
        mMainHandler = new Handler(Looper.getMainLooper());
        mImageNeedsDownload = false;
    }

    public void setStereoMode(String mode){
        mStereoMode = mode;
    }

    public void setSource(ReadableMap source) {
        mSourceMap = source;
        mImageNeedsDownload = true;
    }

    public void setRotation(ReadableArray rotation) {
        if (rotation == null) {
            mRotation = sDefaultRotation;
        } else {
            float[] rotationArr = {(float) rotation.getDouble(0),
                    (float) rotation.getDouble(1), (float) rotation.getDouble(2)};
            mRotation = rotationArr;
        }
        if (getNodeJni() != null) {
            PortalScene portal = getNodeJni().getParentPortalScene();
            if (portal != null) {
                portal.setBackgroundRotation(new Vector(mRotation));
            }
        }
    }

    @Override
    public void onPropsSet() {
        super.onPropsSet();
        if (!mImageNeedsDownload || mSourceMap == null) {
            return;
        }

        ImageDownloader downloader = new ImageDownloader(getContext());
        downloader.setTextureFormat(mFormat);

        imageDownloadDidStart();

        mDownloadListener = new Image360DownloadListener();
        downloader.getImageAsync(mSourceMap, mDownloadListener);

        mImageNeedsDownload = false;
    }

    @Override
    public void onTearDown() {
        super.onTearDown();
        if (mDownloadListener != null) {
            mDownloadListener.invalidate();
        }

        if (mLatestImage != null) {
            mLatestImage.destroy();
            mLatestImage = null;
        }

        if (mLatestTexture != null) {
            mLatestTexture.dispose();
            mLatestTexture = null;
        }
    }

    @Override
    public void setScene(VRTScene scene) {
        super.setScene(scene);
        if (mLatestTexture != null) {
            if (getNodeJni() != null) {
                PortalScene portal = getNodeJni().getParentPortalScene();
                if (portal != null) {
                    portal.setBackgroundTexture(mLatestTexture);
                    portal.setBackgroundRotation(new Vector(mRotation));
                }
            }
        }
    }

    public void setFormat(String format) {
        mFormat = TextureFormat.forString(format);
        mImageNeedsDownload = true;
    }

    private void imageDownloadDidStart() {
        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ViroEvents.ON_LOAD_START,
                null
        );
    }

    private void imageDownloadDidFinish() {
        mReactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                getId(),
                ViroEvents.ON_LOAD_END,
                null
        );
    }

    private class Image360DownloadListener implements ImageDownloadListener {
        private boolean mIsValid = true;

        public void invalidate() {
            mIsValid = false;
        }

        @Override
        public boolean isValid() {
            return mIsValid;
        }

        @Override
        public void completed(final Bitmap result) {
            mMainHandler.post(new Runnable() {
                public void run() {
                    if (!isValid()) {
                        return;
                    }
                    if (mLatestImage != null) {
                        mLatestImage.destroy();
                    }

                    if (mLatestTexture != null) {
                        mLatestTexture.dispose();
                    }

                    mLatestImage = new Image(result, mFormat);
                    mLatestTexture = new Texture(mLatestImage, mFormat, true, false, mStereoMode);

                    if (getNodeJni() != null) {
                        PortalScene portal = getNodeJni().getParentPortalScene();
                        if (portal != null) {
                            portal.setBackgroundTexture(mLatestTexture);
                            portal.setBackgroundRotation(new Vector(mRotation));
                        }
                    }
                    imageDownloadDidFinish();
                    mDownloadListener = null;
                }
            });
        }

        @Override
        public void failed(String error) {
            if (!isValid()) {
                return;
            }
            onError(error);
        }
    }
}