package kr.rokoroku.mbus.util;

import android.os.AsyncTask;

import kr.rokoroku.mbus.BaseApplication;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

/**
 * Created by rok on 2015. 5. 31..
 */
public class AsyncDownloadTask extends AsyncTask<Void, Long, File> {

    private String url;
    private Callback callback;
    private Throwable failReason;

    public AsyncDownloadTask(String url, Callback callback) {
        this.url = url;
        this.callback = callback;
    }

    @Override
    protected File doInBackground(Void... params) {
        OkHttpClient httpClient = new OkHttpClient();
        Call call = httpClient.newCall(new Request.Builder().url(url).get().build());

        try {
            Response response = call.execute();
            if (response.code() == 200) {
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    //setup input stream
                    inputStream = response.body().byteStream();
                    byte[] buff = new byte[1024 * 4];
                    long downloaded = 0;
                    long target = response.body().contentLength();

                    //setup output stream
                    String filename = response.header("filename");
                    File file = new File(BaseApplication.getInstance().getCacheDir(), filename);
                    outputStream = new FileOutputStream(file);

                    //initial progress
                    publishProgress(0L, target);

                    //start download
                    while (true) {
                        //read buff
                        int readed = inputStream.read(buff);
                        if (readed == -1) break;

                        //write buff
                        outputStream.write(buff);

                        //update progress
                        downloaded += readed;
                        publishProgress(downloaded, target);
                        if (isCancelled()) {
                            failReason = new InterruptedIOException("Download has been cancelled");
                        }
                    }
                    return file;

                } catch (IOException e) {
                    e.printStackTrace();
                    failReason = e;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } else {
                failReason = new FileNotFoundException(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
            failReason = e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        callback.onProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(File result) {
        if(result != null) {
            callback.onSuccess(result);
        } else {
            callback.onFail(failReason);
        }
    }

    @Override
    protected void onCancelled(File file) {
        if(file != null && file.exists()) {
            file.deleteOnExit();
        }
        callback.onFail(failReason);
    }

    public interface Callback {
        void onProgressUpdate(long progress, long goal);
        void onSuccess(File file);
        void onFail(Throwable failReason);
    }

//    public File download(String url) throws IOException {
//
//        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder()
//                .url(url)
//                .addHeader("Content-Type", "application/text")
//                .build();
//        Response response = client.newCall(request).execute();
//
//        String filename = response.header("filename");
//        File downloadedFile = new File(BaseApplication.getInstance().getCacheDir(), filename);
//
//        BufferedSink sink = null;
//        sink = Okio.buffer(Okio.sink(downloadedFile));
//        sink.writeAll(response.body().source());
//        sink.close();
//
//        return downloadedFile;
//    }

}
