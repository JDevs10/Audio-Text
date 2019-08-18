package com.example.audiotext.adapters;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.audiotext.R;
import com.example.audiotext.interfaces.FilesListener;
import com.example.audiotext.interfaces.ViewAudioInformation;
import com.example.audiotext.utility.Utility;

import java.io.File;
import java.util.ArrayList;

public class AudioFileAdapter extends RecyclerView.Adapter<AudioFileAdapter.Holder> {
    private final String TAG = AudioFileAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Holder> holderList;
    private ArrayList<File> fileList;
    private FilesListener filesListener;
    private ViewAudioInformation mViewAudioInformation;

    public AudioFileAdapter(Context mContext, FilesListener filesListener, ViewAudioInformation mViewAudioInformation, ArrayList<File> fileList){
        this.mContext = mContext;
        this.filesListener = filesListener;
        this.mViewAudioInformation = mViewAudioInformation;
        this.fileList = fileList;
        this.holderList = new ArrayList<>();
    }

    public class Holder extends RecyclerView.ViewHolder {

        LinearLayout audioLayout_ll;
        public ProgressBar progressBarIcon;
        TextView audioTitle_tv, audioDuration;

        public Holder(@NonNull View itemView) {
            super(itemView);
            progressBarIcon = itemView.findViewById(R.id.custom_audio_progress);
            audioLayout_ll = itemView.findViewById(R.id.custom_audio_layout);
            audioTitle_tv = itemView.findViewById(R.id.custom_audio_title);
            audioDuration = itemView.findViewById(R.id.custom_audio_duration);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.custom_audio_layout, viewGroup, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int i) {
        holderList.add(holder);
        holder.audioTitle_tv.setText(fileList.get(i).getName());

        holder.audioDuration.setText(new Utility().convertFileDuration(fileList.get(i)));
        holder.audioLayout_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filesListener.onAudioFileListener(i, fileList.get(i));
                filesListener.onAudioFileProgress(true, holder.progressBarIcon);
            }
        });
        holder.audioLayout_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //set audio information pop up
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_audio_info);
                mViewAudioInformation.onViewAudioInformation(fileList, dialog, i);
                dialog.setCancelable(false);
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (fileList != null){
            return fileList.size();
        }else {
            return 0;
        }
    }

    public ArrayList<Holder> getHolders(){
        return holderList;
    }


}
