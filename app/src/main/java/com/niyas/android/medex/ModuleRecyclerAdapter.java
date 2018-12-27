package com.niyas.android.medex;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.common.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import io.opencensus.internal.StringUtil;

public class ModuleRecyclerAdapter extends RecyclerView.Adapter<ModuleViewHolder> {

    private static final String TAG = "ModuleRecyclerAdapter";
    private ArrayList<Module> moduleArrayList;
    private FirebaseStorage storage;
    private ProgressBar progressBar;

    private Activity context;
    private static String extension = ".pdf";
    //private static DownloadManager downloadManager;
    private static ArrayList<String> downloads;
    private static ArrayList<String> downloadedFiles;

    /**
     * ModuleRecyclerAdapter Constructor
     * @param context activity instance
     * @param moduleArrayList Array list of data
     * @param storage firebase storage instacne
     * @param progressBar progress bar instance
     */
    public ModuleRecyclerAdapter(Activity context, ArrayList<Module> moduleArrayList, FirebaseStorage storage,
                                 ProgressBar progressBar) {
        this.moduleArrayList = moduleArrayList;
        this.storage = storage;
        this.progressBar = progressBar;
        this.context = context;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.module_item, parent, false);
        downloads = new ArrayList<>();
        downloadedFiles = new ArrayList<>();
        loadDownloadedFiles();
        //downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        return new ModuleViewHolder(view);
    }

    private void loadDownloadedFiles() {

        File path = new File(context.getFilesDir() + "/docs/");
        File list[] = path.listFiles();
        if(list != null){
            for(int i = 0; i < list.length; i++) {
                Integer index = list[i].getName().indexOf(".");
                if(index != -1) {
                    String fileName = list[i].getName().substring(0, index);
                    downloadedFiles.add(fileName);
                    Log.d(TAG, fileName);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ModuleViewHolder holder, int position) {

        holder.title.setText(moduleArrayList.get(position).getFileName());
        holder.sub_title.setText(moduleArrayList.get(position).getSubject());
        holder.type.setText(checkYear(moduleArrayList.get(position).getType()));
        SimpleDateFormat simpleDateFormat  =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

        Typeface raleway_bold = Typeface.createFromAsset(context.getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(context.getAssets(),"fonts/Raleway-Regular.ttf" );

        holder.title.setTypeface(raleway_bold);
        holder.sub_title.setTypeface(raleway_regular);
        holder.type.setTypeface(raleway_regular);

        if(!downloadedFiles.contains(moduleArrayList.get(position).getFileName())) {
            holder.delete.setVisibility(View.INVISIBLE);
            holder.download.setBackgroundResource(R.drawable.ic_download);
        }

        if(downloadedFiles.contains(moduleArrayList.get(position).getFileName())) {
            holder.delete.setVisibility(View.VISIBLE);
            holder.download.setBackgroundResource(R.drawable.ic_view);
        }

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadFile(holder.getAdapterPosition());
                holder.delete.setVisibility(View.VISIBLE);
                holder.download.setBackgroundResource(R.drawable.ic_view);
            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDeleted = deleteFile(holder.getAdapterPosition());
                if(isDeleted){
                    holder.delete.setVisibility(View.INVISIBLE);
                    holder.download.setBackgroundResource(R.drawable.ic_download);
                }
            }
        });
    }

    private boolean deleteFile(int position) {

        Log.i("Deleting file", moduleArrayList.get(position).getUrl());
        String filePath = moduleArrayList.get(position).getUrl();
        final String fileName = moduleArrayList.get(position).getFileName();
        extension = filePath.substring(filePath.lastIndexOf("."));
        File path = new File(context.getFilesDir() + "/docs/");
        if (!path.exists()) {
            if(path.mkdir());
        }
        File file = new File(context.getFilesDir() + File.separator + "docs" + File.separator + fileName + extension);
        if(file.delete()) {
            Toast.makeText(context, fileName + " deleted successfully", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, fileName + " doesn't exists", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private String checkYear(Long type) {
        if(type == 1) {
            return "Ist MBBS";
        } else if(type == 2) {
            return "IInd MBBS";
        } else if(type == 3) {
            return "IIIrd MBBS Part 1";
        } else if (type == 4) {
            return "IIIrd MBBS Part 2";
        }
        return "No data";
    }

    /**
     * download file
     * @param position position of the file clicked for download.
     */
    private void downloadFile(final int position) {

        progressBar.setVisibility(View.VISIBLE);
        Log.i("Downloading File", moduleArrayList.get(position).getUrl());
        String filePath = moduleArrayList.get(position).getUrl();
        final String fileName = moduleArrayList.get(position).getFileName();
        extension = filePath.substring(filePath.lastIndexOf("."));
        File path = new File(context.getFilesDir() + "/docs/");
        if (!path.exists()) {
            if(path.mkdir());
        }
        File file = new File(context.getFilesDir() + File.separator + "docs" + File.separator + fileName + extension);
        if(file.exists()) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(context, "Opening file from downloads", Toast.LENGTH_SHORT).show();
            openFile(file, moduleArrayList.get(position).getUrl());
        } else if(downloads.size() > 0) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(context, "Multiple download not supported", Toast.LENGTH_SHORT).show();
        } else {
            downloads.add(fileName);
            Log.d(TAG, fileName + " added to queue");
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
            storage.getReference(moduleArrayList.get(position).getUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("Download Link", uri.toString());
                    progressBar.setVisibility(View.INVISIBLE);
                    DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(context, progressBar);
                    downloadFileFromURL.execute(uri.toString(), moduleArrayList.get(position).getFileName(),
                            moduleArrayList.get(position).getSubject(), fileName, moduleArrayList.get(position).getFileId());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Log.e("Fetching URL failed", moduleArrayList.get(position).getUrl());
                    downloads.remove(fileName);
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return moduleArrayList.size();
    }

    private void openFile(File file, String url) {
        Uri path = Uri.fromFile(file);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = "*/*";
        if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(path.toString()))){
            mimeType = MimeTypeMap.getFileExtensionFromUrl(path.toString());
        }
        Log.d(TAG, mimeType + " " + extension);
        Log.i(TAG, String.valueOf(path));
        if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg")){
            //OpenIntent.setDataAndType(fileURI, "image/" + mimeType);
            Intent intent = new Intent(context, ImageViewer.class);
            intent.putExtra("file", file);
            intent.putExtra("url", url);
            progressBar.setVisibility(View.INVISIBLE);
            context.startActivity(intent);
        } else {
            //OpenIntent.setDataAndType(fileURI, "application/" + mimeType);
            Intent intent = new Intent(context, PdfViewer.class);
            intent.putExtra("file", file);
            intent.putExtra("url", url);
            progressBar.setVisibility(View.INVISIBLE);
            context.startActivity(intent);
        }
    }

/*    private static BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title;
            Bundle extras = intent.getExtras();
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = downloadManager.query(query);
            if(c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if(status == DownloadManager.STATUS_SUCCESSFUL) {
                    title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    downloads.remove(title);
                    Toast.makeText(context, title + " downloaded", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };*/

    private static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private WeakReference<ProgressBar> progressBarRef;
        private WeakReference<Context> mContextRef;

        DownloadFileFromURL(Context context, ProgressBar progressBar){
            this.mContextRef = new WeakReference<>(context);
            this.progressBarRef = new WeakReference<>(progressBar);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar progressBar = progressBarRef.get();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... strings) {

            int count;
            Context context = mContextRef.get();
            File path = new File(context.getFilesDir() + "/docs/");
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();
                Log.d(TAG, "Length of file: " + lengthOfFile);

                InputStream inputStream = new BufferedInputStream(url.openStream());
                File file = new File(path, strings[1] + extension);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int)((total*100) / lengthOfFile));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage(), e);
                return "failed";
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return "failed";
            }

       /*     Uri uri = Uri.parse(strings[0]);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(strings[1]);
            request.setDescription(strings[2]);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, strings[1] + extension);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            if (downloadManager != null) {
                downloadManager.enqueue(request);
            } else {
                return "failed";
            }*/
            return strings[3];
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            ProgressBar progressBar = progressBarRef.get();
            progressBar.setIndeterminate(false);
            progressBar.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ProgressBar progressBar = progressBarRef.get();
            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setIndeterminate(true);
            downloads.remove(s);
            try {
                Context mContext = mContextRef.get();
                if(s.equals("failed")) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext,s +  " download Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, s + " downloaded", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }

        }
    }
}
