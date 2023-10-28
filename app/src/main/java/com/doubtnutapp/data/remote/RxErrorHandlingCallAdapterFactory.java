package com.doubtnutapp.data.remote;

import android.content.Context;

import com.doubtnut.analytics.EventConstants;
import com.doubtnutapp.DoubtnutApp;
import com.doubtnutapp.Log;
import com.doubtnut.core.entitiy.AnalyticsEvent;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
    public Context context;
    private RxJava2CallAdapterFactory original;

    public RxErrorHandlingCallAdapterFactory(Context context) {
        this.context = context;
        original = RxJava2CallAdapterFactory.create();
    }

    public RxErrorHandlingCallAdapterFactory() {
        original = RxJava2CallAdapterFactory.create();
    }

    public static CallAdapter.Factory create() {
        return new RxErrorHandlingCallAdapterFactory();
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        CallAdapter<?, ?> wrapped = original.get(returnType, annotations, retrofit);
        if (wrapped != null) {
            return new RxCallAdapterWrapper(retrofit, wrapped, context);
        } else {
            return null;
        }
    }

    private static class RxCallAdapterWrapper<R> implements CallAdapter<R, Object> {
        private final Retrofit retrofit;
        private final CallAdapter<R, Object> wrapped;
        private Context context;

        public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, Object> wrapped, Context context) {
            this.retrofit = retrofit;
            this.wrapped = wrapped;
            this.context = context;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @Override
        public Object adapt(Call<R> call) {
            Object result = wrapped.adapt(call);
            if (result instanceof Single) {
                return ((Single) result).onErrorResumeNext(new Function<Throwable, SingleSource>() {
                    @Override
                    public SingleSource apply(@NonNull Throwable throwable) throws Exception {
                        logError(throwable, call);
                        return Single.error(throwable);
                    }
                });
            }
            if (result instanceof Observable) {
                return ((Observable) result).onErrorResumeNext(new Function<Throwable, ObservableSource>() {
                    @Override
                    public ObservableSource apply(@NonNull Throwable throwable) throws Exception {
                        logError(throwable, call);
                        return Observable.error(throwable);
                    }
                });
            }

            if (result instanceof Completable) {
                return ((Completable) result).onErrorResumeNext(new Function<Throwable, CompletableSource>() {
                    @Override
                    public CompletableSource apply(@NonNull Throwable throwable) throws Exception {
                        logError(throwable, call);
                        return Completable.error(throwable);
                    }
                });
            }

            return result;
        }

        private void logError(Throwable throwable, Call call) {
            String url = call.request().url().toString();
            HashMap<String, Object> eventParams = new HashMap<>();
            eventParams.put("url", url);
            eventParams.put("exception", throwable.getClass().toString());
            if (throwable instanceof HttpException) {
                // handled in GlobalErrorHandler
            } else if (throwable instanceof IOException) {
                Log.INSTANCE.e(new Throwable("IOException in api url " + url, throwable), "NetworkError");
                DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
                        new AnalyticsEvent(
                                EventConstants.EVENT_NETWORK_ERROR,
                                eventParams,
                                false,
                                true,
                                false,
                                true,
                                true,
                                false,
                                false
                        )
                );
            } else {
                Log.INSTANCE.e(new Throwable("UnknownError in api url " + url, throwable), "UnknownError");
                DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
                        new AnalyticsEvent(
                                EventConstants.EVENT_UNKNOWN_ERROR,
                                eventParams,
                                false,
                                true,
                                false,
                                true,
                                true,
                                false,
                                false
                        )
                );
            }
        }
    }
}