package com.niyas.android.medex;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ModuleRecyclerAdapter extends RecyclerView.Adapter<ModuleViewHolder> {

    private static final String TAG = "ModuleRecyclerAdapter";
    private ModuleFragment moduleFragment;
    private ArrayList<Module> moduleArrayList;
    private FirebaseStorage storage;
    private ProgressBar progressBar;

    private Context context;
    private static String extension = ".pdf";
    private static DownloadManager downloadManager;
    private static ArrayList<String> downloads;

    /**
     * ModuleRecyclerAdapter Constructor
     * @param moduleFragment fragment instance
     * @param moduleArrayList Array list of data
     * @param storage firebase storage instacne
     * @param progressBar progress bar instance
     */
    public ModuleRecyclerAdapter(ModuleFragment moduleFragment, ArrayList<Module> moduleArrayList, FirebaseStorage storage,
                                 ProgressBar progressBar) {
        this.moduleFragment = moduleFragment;
        this.moduleArrayList = moduleArrayList;
        this.storage = storage;
        this.progressBar = progressBar;
        this.context = moduleFragment.getActivity();
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(moduleFragment.getActivity());
        View view = layoutInflater.inflate(R.layout.module_item, parent, false);
        downloads = new ArrayList<>();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ModuleViewHolder holder, int position) {

        holder.title.setText(moduleArrayList.get(position).getFileName());
        holder.sub_title.setText(moduleArrayList.get(position).getSubject());
        holder.type.setText(checkYear(moduleArrayList.get(position).getType()));
        SimpleDateFormat simpleDateFormat  =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
        holder.time.setText(simpleDateFormat.format(moduleArrayList.get(position).getTime().toDate()));

        Typeface raleway_bold = Typeface.createFromAsset(moduleFragment.getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(moduleFragment.getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        holder.title.setTypeface(raleway_bold);
        holder.sub_title.setTypeface(raleway_regular);
        holder.type.setTypeface(raleway_regular);
        holder.time.setTypeface(raleway_regular);

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadFile(holder.getAdapterPosition());
            }
        });

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
        String fileName = moduleArrayList.get(position).getFileName();
        extension = filePath.substring(filePath.lastIndexOf("."));
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +
                moduleArrayList.get(position).getFileName() + extension);

        if(file.exists()) {
            Toast.makeText(moduleFragment.getActivity(), "Opening file from downloads", Toast.LENGTH_SHORT).show();
            openFile(file);
        } else if(downloads.contains(fileName)) {
            Toast.makeText(context, fileName + "already in download queue. Please check", Toast.LENGTH_SHORT).show();
        } else {
            downloads.add(fileName);
            Log.d(TAG, fileName + " added to queue");
            storage.getReference(moduleArrayList.get(position).getUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("Download Link", uri.toString());
                    DownloadFileFromURL downloadFileFromURL = new DownloadFileFromURL(moduleFragment.getActivity(), progressBar);
                    downloadFileFromURL.execute(uri.toString(), moduleArrayList.get(position).getFileName(), moduleArrayList.get(position).getSubject());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Fetching URL failed", moduleArrayList.get(position).getUrl());
                    Toast.makeText(moduleFragment.getActivity(), "Download Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return moduleArrayList.size();
    }

    private void openFile(File file) {
        Uri path = Uri.fromFile(file);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeType = "*/*";
        if (mimeTypeMap.hasExtension(MimeTypeMap.getFileExtensionFromUrl(path.toString()))){
            mimeType = MimeTypeMap.getFileExtensionFromUrl(path.toString());
        }
        Log.d(TAG, mimeType + " " + extension);
        Log.i(TAG, String.valueOf(path));
        Intent OpenIntent = new Intent(Intent.ACTION_VIEW);
        OpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri fileURI = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider", file);
        if(extension.equals(".pdf")) {
            OpenIntent.setDataAndType(fileURI, "application/" + mimeType);
        } else if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg")){
            OpenIntent.setDataAndType(fileURI, "image/" + mimeType);
        }
        OpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        progressBar.setVisibility(View.INVISIBLE);
        try {
            context.startActivity(OpenIntent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private static BroadcastReceiver onComplete = new BroadcastReceiver() {
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
    };

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
        }

        @Override
        protected String doInBackground(String... strings) {

            Uri uri = Uri.parse(strings[0]);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(strings[1]);
            request.setDescription(strings[2]);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, strings[1] + extension);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            if (downloadManager != null) {
                downloadManager.enqueue(request);
            } else {
                return "failed";
            }
            return strings[1];
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ProgressBar progressBar = progressBarRef.get();
            progressBar.setVisibility(View.INVISIBLE);
            Context mContext = mContextRef.get();
            if(s.equals("failed")) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(mContext,s +  " download Failed", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(mContext, "Downloading " + s, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
