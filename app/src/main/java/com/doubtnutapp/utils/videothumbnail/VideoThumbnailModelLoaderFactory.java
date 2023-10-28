package com.doubtnutapp.utils.videothumbnail;

import android.graphics.Bitmap;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import org.jetbrains.annotations.NotNull;

public class VideoThumbnailModelLoaderFactory implements ModelLoaderFactory<String, Bitmap> {

    @NotNull
    @Override
    public ModelLoader<String, Bitmap> build(@NotNull MultiModelLoaderFactory unused) {
        return new VideoThumbnailModelLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}