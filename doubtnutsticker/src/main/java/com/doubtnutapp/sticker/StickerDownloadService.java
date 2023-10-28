package com.doubtnutapp.sticker;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface StickerDownloadService {
    @GET
    Single<ResponseBody> downloadSticker(@Url String downloadUrl);
}
