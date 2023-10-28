package com.doubtnutapp.utils.videothumbnail;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import org.jetbrains.annotations.NotNull;

public final class VideoThumbnailModelLoader implements ModelLoader<String, Bitmap> {

    @NonNull
    @Override
    public LoadData<Bitmap> buildLoadData(@NotNull String model, int width, int height, @NotNull Options options) {
        return new LoadData<>(new ObjectKey(model), new VideoThumbnailDataFetcher(model));
    }

    @Override
    public boolean handles(@NonNull String model) {
        return model.contains(".mp4") && !model.contains(".png") && !model.contains(".jpg") && !model.contains(".jpeg") && !model.contains(".webp");
    }
}