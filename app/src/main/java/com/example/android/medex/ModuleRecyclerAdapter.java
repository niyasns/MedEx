package com.example.android.medex;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.CheckedOutputStream;

public class ModuleRecyclerAdapter extends RecyclerView.Adapter<ModuleViewHolder> {

    private static final String TAG = "ModuleRecyclerAdapter";
    private ModuleFragment moduleFragment;
    private ArrayList<Module> moduleArrayList;
    private FirebaseStorage storage;
    private ProgressBar progressBar;

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
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(moduleFragment.getActivity());
        View view = layoutInflater.inflate(R.layout.module_item, parent, false);

        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ModuleViewHolder holder, int position) {

        holder.title.setText(moduleArrayList.get(position).getFileName());
        holder.sub_title.setText(moduleArrayList.get(position).getSubject());
        holder.type.setText(moduleArrayList.get(position).getType());
        Date date = moduleArrayList.get(position).getTime().toDate();
        holder.time.setText(date.toString());

        Typeface raleway_bold = Typeface.createFromAsset(moduleFragment.getActivity().getAssets(),"fonts/Raleway-Bold.ttf" );
        Typeface raleway_regular = Typeface.createFromAsset(moduleFragment.getActivity().getAssets(),"fonts/Raleway-Regular.ttf" );

        holder.title.setTypeface(raleway_bold);
        holder.sub_title.setTypeface(raleway_regular);
        holder.type.setTypeface(raleway_regular);
        holder.time.setTypeface(raleway_regular);
        holder.download.setTypeface(raleway_regular);

        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadFile(holder.getAdapterPosition());
            }
        });

    }

    /**
     * download file
     * @param position position of the file clicked for download.
     */
    private void downloadFile(final int position) {

        progressBar.setVisibility(View.VISIBLE);
        Log.i("Downloading File", moduleArrayList.get(position).getUrl());

        storage.getReference(moduleArrayList.get(position).getUrl()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                progressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public int getItemCount() {
        return moduleArrayList.size();
    }

    private static class DownloadFileFromURL extends AsyncTask<String, String, String> {

        private ProgressBar progressBar;
        private Context mContext;

        DownloadFileFromURL(Context context, ProgressBar progressBar){
            this.mContext = context;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            Uri uri = Uri.parse(strings[0]);

            long downloadReference;
            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(strings[1]);
            request.setDescription(strings[2]);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, strings[1] + ".pdf");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            if (downloadManager != null) {
                downloadManager.enqueue(request);
            } else {
                return "failed";
            }
            return "success";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            if(s.equals("failed")) {
                Toast.makeText(mContext, "Download Failed", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(mContext, "Downloading. Check Downloads", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
