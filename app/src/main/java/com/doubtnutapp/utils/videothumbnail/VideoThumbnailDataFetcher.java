package com.doubtnutapp.utils.videothumbnail;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.util.HashMap;

public class VideoThumbnailDataFetcher implements DataFetcher<Bitmap> {

    private final String model;
    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    VideoThumbnailDataFetcher(String model) {
        this.model = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        Bitmap bitmap = null;
        try {
            mediaMetadataRetriever.setDataSource(model, new HashMap<>());
            bitmap = mediaMetadataRetriever.getFrameAtTime();
            callback.onDataReady(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            release();
        }
    }

    @Override
    public void cleanup() {
        release();
    }

    @Override
    public void cancel() {
        //empty
    }

    private void release() {
        if (mediaMetadataRetriever != null) {
            try {
                mediaMetadataRetriever.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaMetadataRetriever = null;
        }
    }

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}