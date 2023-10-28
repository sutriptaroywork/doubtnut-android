package com.doubtnutapp.sticker;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class StickerDownloadManager {

    interface OnStickerDownloadedListener{
        void onStickerDownloadSuccess();
        void onStickerDownloadError();
    }

    private final String TAG = StickerDownloadManager.class.getSimpleName();
    private Context context;
    private static StickerDownloadManager ourInstance;
    private boolean isStickerDownloading = false;
    private OnStickerDownloadedListener onStickerDownloadedListener = null;

    public static StickerDownloadManager getInstance(@NonNull Context context) throws IllegalArgumentException {
        if (ourInstance == null) {
            ourInstance = new StickerDownloadManager(context);
            init();
        }
        return ourInstance;
    }

    private static void init() {

    }

    private StickerDownloadManager(Context context) {
        if (context instanceof Application) {
            this.context = context;
        } else {
            throw new IllegalArgumentException("It must have to be Application context");
        }
    }

    public void downloadSticker(OnStickerDownloadedListener onStickerDownloadedListener) {
        this.onStickerDownloadedListener = onStickerDownloadedListener;

        if (isStickerDownloading || isStickerDirPresent()) {
            Log.d(TAG, "Sticker Dir Exist " + isStickerDirPresent() + " is sticker downloading " + isStickerDownloading);
            return;
        }

        isStickerDownloading = true;
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl("https://d10lpgp6xz60nq.cloudfront.net/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        StickerDownloadService stickerDownloadService = retrofit.create(StickerDownloadService.class);
        stickerDownloadService.downloadSticker("https://d10lpgp6xz60nq.cloudfront.net/stickers/stickers_asset.zip")
                .flatMap((Function<ResponseBody, SingleSource<Boolean>>) responseBody -> savePdfToDisk(responseBody))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    isStickerDownloading = false;
                }, throwable -> {
                    isStickerDownloading = false;
                    throwable.printStackTrace();
                });
    }

    public boolean isStickerDownloading() {
        return isStickerDownloading;
    }

    public boolean isStickerDirPresent() {
        return new File(context.getFilesDir() + File.separator + StickerContentProvider.STICKERS_ASSET).exists();
    }

    private Single<Boolean> savePdfToDisk(ResponseBody stickerResponseBody) {
        return Single.fromCallable(() -> {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(context.getFilesDir() + File.separator + "stickers_asset.zipr"));
            try {
                bufferedOutputStream.write(stickerResponseBody.bytes());
                unzip(context.getFilesDir() + File.separator + "stickers_asset.zipr", context.getFilesDir() + File.separator);
                new File(context.getFilesDir() + File.separator + "stickers_asset.zipr").delete();
                onStickerDownloadedListener.onStickerDownloadSuccess();
                return true;
            } catch (IOException ex) {
                bufferedOutputStream.close();
                onStickerDownloadedListener.onStickerDownloadError();
                return false;
            }
        });
    }

    public void unzip(String zipFilePath, String destDirectory) throws IOException {

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {

        int BUFFER_SIZE = 4096;

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
