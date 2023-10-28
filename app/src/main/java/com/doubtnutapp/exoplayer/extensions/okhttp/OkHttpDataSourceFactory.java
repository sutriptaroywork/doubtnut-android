package com.doubtnutapp.exoplayer.extensions.okhttp;


import static com.google.android.exoplayer2.ExoPlayerLibraryInfo.DEFAULT_USER_AGENT;

import androidx.annotation.Nullable;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource.Factory;
import com.google.android.exoplayer2.upstream.TransferListener;
import okhttp3.CacheControl;
import okhttp3.Call;

/**
 * A {@link Factory} that produces {@link OkHttpDataSource}.
 */
public final class OkHttpDataSourceFactory extends BaseFactory {

    private final Call.Factory callFactory;
    @Nullable private final String userAgent;
    @Nullable private final TransferListener listener;
    @Nullable private final CacheControl cacheControl;

    /**
     * Creates an instance.
     *
     * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
     *     by the sources created by the factory.
     */
    public OkHttpDataSourceFactory(Call.Factory callFactory) {
        this(callFactory, DEFAULT_USER_AGENT, /* listener= */ null, /* cacheControl= */ null);
    }

    /**
     * Creates an instance.
     *
     * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
     *     by the sources created by the factory.
     * @param userAgent An optional User-Agent string.
     */
    public OkHttpDataSourceFactory(Call.Factory callFactory, @Nullable String userAgent) {
        this(callFactory, userAgent, /* listener= */ null, /* cacheControl= */ null);
    }

    /**
     * Creates an instance.
     *
     * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
     *     by the sources created by the factory.
     * @param userAgent An optional User-Agent string.
     * @param cacheControl An optional {@link CacheControl} for setting the Cache-Control header.
     */
    public OkHttpDataSourceFactory(
            Call.Factory callFactory, @Nullable String userAgent, @Nullable CacheControl cacheControl) {
        this(callFactory, userAgent, /* listener= */ null, cacheControl);
    }

    /**
     * Creates an instance.
     *
     * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
     *     by the sources created by the factory.
     * @param userAgent An optional User-Agent string.
     * @param listener An optional listener.
     */
    public OkHttpDataSourceFactory(
            Call.Factory callFactory, @Nullable String userAgent, @Nullable TransferListener listener) {
        this(callFactory, userAgent, listener, /* cacheControl= */ null);
    }

    /**
     * Creates an instance.
     *
     * @param callFactory A {@link Call.Factory} (typically an {@link okhttp3.OkHttpClient}) for use
     *     by the sources created by the factory.
     * @param userAgent An optional User-Agent string.
     * @param listener An optional listener.
     * @param cacheControl An optional {@link CacheControl} for setting the Cache-Control header.
     */
    public OkHttpDataSourceFactory(
            Call.Factory callFactory,
            @Nullable String userAgent,
            @Nullable TransferListener listener,
            @Nullable CacheControl cacheControl) {
        this.callFactory = callFactory;
        this.userAgent = userAgent;
        this.listener = listener;
        this.cacheControl = cacheControl;
    }

    @Override
    protected OkHttpDataSource createDataSourceInternal(
            HttpDataSource.RequestProperties defaultRequestProperties) {
        OkHttpDataSource dataSource =
                new OkHttpDataSource(
                        callFactory,
                        userAgent,
                        cacheControl,
                        defaultRequestProperties);
        if (listener != null) {
            dataSource.addTransferListener(listener);
        }
        return dataSource;
    }
}