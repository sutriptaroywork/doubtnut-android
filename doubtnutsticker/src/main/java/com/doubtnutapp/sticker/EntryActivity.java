/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.doubtnutapp.sticker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.annotation.Nullable;

import com.doubtnut.core.utils.ToastUtils;

public class EntryActivity extends BaseActivity implements StickerDownloadManager.OnStickerDownloadedListener {
    private View progressBar;
    private LoadListAsyncTask loadListAsyncTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        overridePendingTransition(0, 0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        progressBar = findViewById(R.id.entry_activity_progress);

        boolean stickerDirPresent =
                StickerDownloadManager.getInstance(getApplicationContext()).isStickerDirPresent();
        boolean isStickerPackDownloading =
                StickerDownloadManager.getInstance(getApplicationContext()).isStickerDownloading();
        if (stickerDirPresent) {
            StickerContentProvider.notifyStickerDownloaded(getApplicationContext());
            loadListAsyncTask = new LoadListAsyncTask(this);
            loadListAsyncTask.execute();
        } else if (isStickerPackDownloading) {
            ToastUtils.makeText(
                    getApplicationContext(),
                    "Downloading Stickers in progress...",
                    Toast.LENGTH_LONG
            ).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            ToastUtils.makeText(
                    getApplicationContext(),
                    "Downloading Stickers Started...",
                    Toast.LENGTH_LONG
            ).show();
            StickerDownloadManager.getInstance(getApplicationContext()).downloadSticker(this);
        }

    }

    private void showStickerPack(ArrayList<StickerPack> stickerPackList) {
        progressBar.setVisibility(View.GONE);
        if (stickerPackList.size() > 1) {
            final Intent intent = new Intent(this, StickerPackListActivity.class);
            intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA, stickerPackList);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            final Intent intent = new Intent(this, StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPackList.get(0));
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void showErrorMessage(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Log.e("EntryActivity", "error fetching sticker packs, " + errorMessage);
        final TextView errorMessageTV = findViewById(R.id.error_message);
        errorMessageTV.setText(getString(R.string.error_message, errorMessage));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadListAsyncTask != null && !loadListAsyncTask.isCancelled()) {
            loadListAsyncTask.cancel(true);
        }
    }

    @Override
    public void onStickerDownloadSuccess() {
        loadListAsyncTask = new LoadListAsyncTask(this);
        loadListAsyncTask.execute();
    }

    @Override
    public void onStickerDownloadError() {
        progressBar.setVisibility(View.GONE);
        ToastUtils.makeText(
                getApplicationContext(),
                "Error in Downloading Stickers...",
                Toast.LENGTH_LONG
        ).show();
        finish();
    }

    static class LoadListAsyncTask extends AsyncTask<Void, Void, Pair<String, ArrayList<StickerPack>>> {
        private final WeakReference<EntryActivity> contextWeakReference;

        LoadListAsyncTask(EntryActivity activity) {
            this.contextWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Pair<String, ArrayList<StickerPack>> doInBackground(Void... voids) {
            ArrayList<StickerPack> stickerPackList;
            try {
                final Context context = contextWeakReference.get();
                if (context != null) {
                    stickerPackList = StickerPackLoader.fetchStickerPacks(context);
                    if (stickerPackList.size() == 0) {
                        return new Pair<>("could not find any packs", null);
                    }
                    for (StickerPack stickerPack : stickerPackList) {
                        StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
                    }
                    return new Pair<>(null, stickerPackList);
                } else {
                    return new Pair<>("could not fetch sticker packs", null);
                }
            } catch (Exception e) {
                Log.e("EntryActivity", "error fetching sticker packs", e);
                return new Pair<>(e.getMessage(), null);
            }
        }

        @Override
        protected void onPostExecute(Pair<String, ArrayList<StickerPack>> stringListPair) {

            final EntryActivity entryActivity = contextWeakReference.get();
            if (entryActivity != null) {
                if (stringListPair.first != null) {
                    entryActivity.showErrorMessage(stringListPair.first);
                } else {
                    entryActivity.showStickerPack(stringListPair.second);
                }
            }
        }
    }
}
