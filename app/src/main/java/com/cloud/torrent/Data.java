package com.cloud.torrent;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;


import java.util.ArrayList;
import java.util.List;


public final class Data {


    private Data() {

    }

    @NonNull
    private static Request getFetchRequests() {
            final Request request = new Request(MainActivity.surls, getFilePath(MainActivity.surls));
            return request;
    }

    @NonNull
    public static Request getFetchRequestWithGroupId(final int groupId) {
        final Request requests = getFetchRequests();
            requests.setGroupId(groupId);
            return requests;
    }

    @NonNull
    private static String getFilePath(@NonNull final String url) {
        final Uri uri = Uri.parse(url);
        final String fileName = uri.getLastPathSegment();
        final String dir = getSaveDir();
        return (dir + fileName);
    }

    @NonNull
    public static String getSaveDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
    }

}
