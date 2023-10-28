package com.doubtnutapp.data.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BitmapDiskSerializeWrapper implements Parcelable {
    private Bitmap src;

    public BitmapDiskSerializeWrapper(@NonNull final Bitmap bmp) {
        src = bmp;
    }

    public void setBitmap(@NonNull final Bitmap bmp) {
        this.src = bmp;
    }

    public Bitmap getImageBitmap() {
        return this.src;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        File outFile = getRandomFile();
        try {
            writeBitmapToFile(src, outFile);
        } catch (IOException ignore) {
        }

        dest.writeString(outFile.getAbsolutePath());
    }

    @NonNull
    private File getRandomFile() {
        File baseDir = Environment.getExternalStorageDirectory();
        String randName = UUID.randomUUID().toString().replace("-", "");
        String fileName = "intent_" + randName + ".png";

        return new File(baseDir, fileName);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BitmapDiskSerializeWrapper createFromParcel(Parcel in) {
            String bmpFilePath = in.readString();
            Bitmap bmp = BitmapFactory.decodeFile(bmpFilePath);

            File bmpFile = new File(bmpFilePath);
            boolean ignore = bmpFile.delete();

            return new BitmapDiskSerializeWrapper(bmp);
        }

        public BitmapDiskSerializeWrapper[] newArray(int size) {
            return new BitmapDiskSerializeWrapper[size];
        }
    };

    private void writeBitmapToFile(@NonNull final Bitmap bmp, @NonNull final File outFile)
            throws IOException {
        OutputStream oStream = new FileOutputStream(outFile);
        bmp.compress(Bitmap.CompressFormat.PNG, 85, oStream);
        oStream.flush();
        oStream.close();
    }
}
