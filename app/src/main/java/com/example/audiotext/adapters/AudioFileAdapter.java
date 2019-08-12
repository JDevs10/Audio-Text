package com.example.audiotext.adapters;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.audiotext.R;
import com.example.audiotext.interfaces.FilesListener;

import java.io.File;
import java.io.IOException;

public class AudioFileAdapter extends RecyclerView.Adapter<AudioFileAdapter.Holder> {
    private final String TAG = AudioFileAdapter.class.getSimpleName();
    private Context mContext;
    //private static MediaPlayer audioPlayer;

    private File[] files;
    private FilesListener filesListener;

    public AudioFileAdapter(Context mContext, File[] files){
        this.mContext = mContext;
//        this.filesListener = filesListener;
        this.files = files;
    }

    public class Holder extends RecyclerView.ViewHolder {

        MediaPlayer audioPlayer;
        LinearLayout audioLayout_ll;
        TextView audioTitle_tv;
        ImageButton audioPlayPause_ib, audioStop_ib;

        public Holder(@NonNull View itemView) {
            super(itemView);
            audioLayout_ll = itemView.findViewById(R.id.custom_audio_layout);
            audioTitle_tv = itemView.findViewById(R.id.custom_audio_title);
            audioPlayPause_ib = itemView.findViewById(R.id.custom_audio_play_pause_ib);
            audioStop_ib = itemView.findViewById(R.id.custom_audio_stop_ib);
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
        holder.audioTitle_tv.setText(files[i].getName());

        holder.audioLayout_ll.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //set audio information pop up
                //filesListener.onAudioFileLongClick(files[i]);
                Dialog dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.dialog_audio_info);
                viewAudioInformation(files[i], dialog);
                dialog.setCancelable(false);
                dialog.show();
                return true;
            }
        });

        if (holder.audioPlayer != null && holder.audioPlayer.isPlaying()){
            stop(holder);
        }

        final Boolean[] audioPlayerChecker = {true};
        holder.audioPlayPause_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioPlayerChecker[0]) {
                    play(holder, i);
                    holder.audioPlayPause_ib.setBackgroundResource(R.drawable.ic_pause);
                    holder.audioTitle_tv.setText("Playing " + files[i].getName());
                    audioPlayerChecker[0] = false;
                } else {
                    pause(holder);
                    holder.audioPlayPause_ib.setBackgroundResource(R.drawable.ic_play_arrow);
                    holder.audioTitle_tv.setText("Paused " + files[i].getName());
                    audioPlayerChecker[0] = true;
                }
            }
        });
        holder.audioStop_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(holder);
                audioPlayerChecker[0] = true;
                holder.audioPlayPause_ib.setBackgroundResource(R.drawable.ic_play_arrow);
                holder.audioTitle_tv.setText(files[i].getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    private void play(final Holder holder, final int index){
        if (holder.audioPlayer == null){
            //audioPlayer = MediaPlayer.create(mContext, R.raw.solar_system);
            //audioPlayer = MediaPlayer.create(mContext, Uri.fromFile(files[index].getAbsoluteFile()));
            try {
                holder.audioPlayer = new MediaPlayer();
                holder.audioPlayer.setDataSource(files[index].getAbsolutePath());
                holder.audioPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlayer(holder);
                    holder.audioPlayPause_ib.setBackgroundResource(R.drawable.ic_play_arrow);
                    holder.audioTitle_tv.setText(files[index].getName());
                }
            });
        }
        holder.audioPlayer.start();
    }

    private void pause(Holder holder){
        if (holder.audioPlayer != null){
            holder.audioPlayer.pause();
        }
    }

    private void stop(Holder holder){
        stopPlayer(holder);
    }

    private void stopPlayer(Holder holder){
        if (holder.audioPlayer != null){
            holder.audioPlayer.release();
            holder.audioPlayer = null;
        }
    }

    private void viewAudioInformation(final File file, final Dialog dialog){
        ImageView close = dialog.findViewById(R.id.dialog_audio_info_close_btn);
        TextView tittle = dialog.findViewById(R.id.dialog_audio_info_tittle);
        TextView text = dialog.findViewById(R.id.dialog_audio_info_text_et);
        Button cancel = dialog.findViewById(R.id.dialog_audio_info_cancel_btn);
        Button delete = dialog.findViewById(R.id.dialog_audio_info_delete_btn);

        tittle.setText(file.getName());
        text.setText("You're about to delete '"+file.getName()+"' from your device.\nAre you sure ?");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = file.getName();
                file.delete();
                if (!file.exists()){
                    Toast.makeText(mContext, name+" deleted!", Toast.LENGTH_LONG).show();
                    notifyDataSetChanged();
                    dialog.dismiss();
                }else {
                    Toast.makeText(mContext, name+" not deleted!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
