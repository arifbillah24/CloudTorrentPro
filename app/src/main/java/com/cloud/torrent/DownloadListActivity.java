package com.cloud.torrent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tonyodev.fetch2.AbstractFetchListener;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class DownloadListActivity extends AppCompatActivity implements ActionListener {

    private static final int STORAGE_PERMISSION_CODE = 200;
    private static final long UNKNOWN_REMAINING_TIME = -1;
    private static final long UNKNOWN_DOWNLOADED_BYTES_PER_SECOND = 0;
    private static final int GROUP_ID = "listGroup".hashCode();
    static final String FETCH_NAMESPACE = "DownloadListActivity";

    private View mainView;
    private FileAdapter fileAdapter;
    private Fetch fetch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar5);
        setSupportActionBar(toolbar);
        setUpViews();
        final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .setNamespace(FETCH_NAMESPACE)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        checkStoragePermissions();
    }

    private void setUpViews() {
        final SwitchCompat networkSwitch = findViewById(R.id.networkSwitch);
        final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mainView = findViewById(R.id.activity_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        networkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fetch.setGlobalNetworkType(NetworkType.WIFI_ONLY);
            } else {
                fetch.setGlobalNetworkType(NetworkType.ALL);
            }
        });
        fileAdapter = new FileAdapter(this);
        recyclerView.setAdapter(fileAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetch.getDownloadsInGroup(GROUP_ID, downloads -> {
            final ArrayList<Download> list = new ArrayList<>(downloads);
            Collections.sort(list, (first, second) -> Long.compare(first.getCreated(), second.getCreated()));
            for (Download download : list) {
                fileAdapter.addDownload(download);
            }
        }).addListener(fetchListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fetch.removeListener(fetchListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fetch.close();
    }

    private final FetchListener fetchListener = new AbstractFetchListener() {
        @Override
        public void onQueued(@NotNull Download download, boolean waitingOnNetwork) {
            fileAdapter.addDownload(download);
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onError(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliseconds, long downloadedBytesPerSecond) {
            fileAdapter.update(download, etaInMilliseconds, downloadedBytesPerSecond);
        }

        @Override
        public void onPaused(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onResumed(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onCancelled(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onRemoved(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }

        @Override
        public void onDeleted(@NotNull Download download) {
            fileAdapter.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND);
        }
    };

    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Snackbar.make(mainView, R.string.permission_not_enabled, Snackbar.LENGTH_INDEFINITE).show();
        }
    }



    @Override
    public void onPauseDownload(int id) {
        fetch.pause(id);
    }

    @Override
    public void onResumeDownload(int id) {
        fetch.resume(id);
    }

    @Override
    public void onRemoveDownload(int id) {
        fetch.remove(id);
    }

    @Override
    public void onRetryDownload(int id) {
        fetch.retry(id);
    }

}