package com.example.justicecamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VideoInfo extends AppCompatActivity {
    static ProgressDialog pd;
    ProgressDialog mProgressDialog;
    Button buttonPlayVideo;
    Button buttonDownload;
    TextView textViewVVideoName;
    TextView textViewVCarModel;
    TextView textViewVCarMake;
    TextView textViewVCarColor;
    TextView textViewVCarNumber;
    TextView textViewVcategory;
    TextView textViewVcomment;
    TextView textViewVInfo;
    Violation thisViolation;
    List<Violation> listvViolation;
    String videoUrl;
    String objectId;
    VideoView video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      //  buttonBackToList = (Button)findViewById(R.id.buttonBackToList);
        buttonPlayVideo = (Button) findViewById(R.id.buttonPlayVideo);
        buttonDownload = (Button) findViewById(R.id.buttonDownload);
        textViewVVideoName = (TextView)findViewById(R.id.textViewVVideoName);
        textViewVCarModel = (TextView)findViewById(R.id.textViewVCarModel);
        textViewVCarMake = (TextView)findViewById(R.id.textViewVCarMake);
        textViewVCarColor = (TextView)findViewById(R.id.textViewVCarColor);
        textViewVCarNumber = (TextView)findViewById(R.id.textViewVCarNumber);
        textViewVcategory = (TextView)findViewById(R.id.textViewVcategory);
        textViewVcomment = (TextView)findViewById(R.id.textViewVcomment);
        textViewVInfo = (TextView)findViewById(R.id.textView10);
        listvViolation = new ArrayList<>();
        thisViolation = new Violation();
        video = (VideoView) findViewById(R.id.videoView);

        mProgressDialog = new ProgressDialog(VideoInfo.this);
        mProgressDialog.setMessage("Downloading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        Intent i = getIntent();
        objectId = i.getStringExtra(CheckedVideoList.OBJECTID);

        BackendlessDataQuery dataQuery2 = new BackendlessDataQuery();
        Backendless.Data.of(Violation.class).find(dataQuery2, new AsyncCallback<BackendlessCollection<Violation>>() {
            @Override
            public void handleResponse(BackendlessCollection<Violation> listOfViolatioons) {
              listvViolation = listOfViolatioons.getData();
                String textToShow = "Успешно скачано, количество элементов " + Integer.toString(listvViolation.size());
                for (int i = 0; i < listvViolation.size(); i++){
                    if (listvViolation.get(i).getObjectId().equals(objectId)){
                        thisViolation = listvViolation.get(i);
                    }
                }
                textViewVVideoName.setText(thisViolation.getName());
                textViewVCarMake.setText(thisViolation.getCarMake());
                textViewVCarModel.setText(thisViolation.getCarModel());
                textViewVCarColor.setText(thisViolation.getColor());
                textViewVCarNumber.setText(thisViolation.getCarNumber());
                textViewVcategory.setText(thisViolation.getCategory().getType());
                textViewVcomment.setText(thisViolation.getComment());
                videoUrl = thisViolation.getVideoUrl();
                String path = videoUrl;
                Uri uri = Uri.parse(path);
                video.setVideoURI(uri);

            }
            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Toast toast2 = Toast.makeText(getApplicationContext(),
                        "Ошибка загрузки " + "Server reported an error - " + backendlessFault.getMessage(), Toast.LENGTH_LONG);
                toast2.show();
            }
        });

        buttonPlayVideo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                /*
                pd = new ProgressDialog(VideoInfo.this);
                pd.setTitle("Подготовка видео");
                pd.setMessage("Подождите");
                pd.show();
*/
                video.setMediaController(new MediaController(VideoInfo.this));

                video.setOnCompletionListener(myVideoViewCompletionListener);
                video.setOnPreparedListener(MyVideoViewPreparedListener);
                video.setOnErrorListener(myVideoViewErrorListener);

                video.requestFocus();
                video.start();
            }
        });

        buttonDownload.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                final DownloadTask downloadTask = new DownloadTask(VideoInfo.this);
                downloadTask.execute(videoUrl);
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadTask.cancel(true);
                    }
                });
            }
        });
    }
    MediaPlayer.OnCompletionListener myVideoViewCompletionListener
            = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer arg0) {
            Toast.makeText(getApplicationContext(),
                    "End of Video",
                    Toast.LENGTH_LONG).show();
        }
    };

    MediaPlayer.OnPreparedListener MyVideoViewPreparedListener
            = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer arg0) {
          //  pd.dismiss();
            Toast.makeText(getApplicationContext(),
                    "Media file is loaded and ready to go",
                    Toast.LENGTH_LONG).show();
        }
    };

    MediaPlayer.OnErrorListener myVideoViewErrorListener
            = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            Toast.makeText(getApplicationContext(),
                    "Error!!!",
                    Toast.LENGTH_LONG).show();
            return true;
        }
    };


    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("storage/emulated/0/videoDownload.mp4");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
    }
}
