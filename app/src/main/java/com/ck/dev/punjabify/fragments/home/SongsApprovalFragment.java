package com.ck.dev.punjabify.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ck.dev.punjabify.R;
import com.ck.dev.punjabify.adapter.ApprovalSongsListAdapter;
import com.ck.dev.punjabify.interfaces.HomeToOnlineFragment;
import com.ck.dev.punjabify.interfaces.OnRecyclerItemClick;
import com.ck.dev.punjabify.interfaces.ViewPagerBackPressed;
import com.ck.dev.punjabify.model.ServerizedTrackData;
import com.ck.dev.punjabify.utils.Config;
import com.ck.dev.punjabify.utils.GenreConfig;
import com.ck.dev.punjabify.utils.ServerizedManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SongsApprovalFragment extends Fragment implements ViewPagerBackPressed, OnRecyclerItemClick {

    private RecyclerView localSongsList;
    private ApprovalSongsListAdapter approvalSongsListAdapter;

    private TextView    titleTxt;
    private TextView    artistTxt;
    private ImageButton deleteBtn;
    private ImageButton nextBtn;
    private Button      gediBtn;
    private Button      hipHopBtn;
    private Button      jattismBtn;
    private Button      legendBtn;
    private Button      longDriveBtn;
    private Button      mahfilBtn;
    private Button      originalBtn;
    private Button      parentalBtn;
    private Button      partyBtn;
    private Button      proBtn;
    private Button      rapBtn;
    private Button      romanceBtn;
    private Button      sadBtn;
    private Button      genderBtn;
    private Button      approveBtn;

    private Boolean gedi      = false;
    private Boolean hipHop    = false;
    private Boolean jattism   = false;
    private Boolean legend    = false;
    private Boolean longDrive = false;
    private Boolean mahfil    = false;
    private Boolean original  = false;
    private Boolean parental  = false;
    private Boolean party     = false;
    private Boolean pro       = false;
    private Boolean rap       = false;
    private Boolean romance   = false;
    private Boolean sad       = false;
    private int     gender    = 0;

    private ArrayList<ServerizedTrackData> serverizedTrackDataList = new ArrayList<>();

    private DatabaseReference databaseReference;

    private HomeToOnlineFragment homeToOnlineFragment;

    private ServerizedTrackData currentTrack;

    private ServerizedManager serverizedManager;

    public SongsApprovalFragment(HomeToOnlineFragment homeToOnlineFragment) {
        this.homeToOnlineFragment = homeToOnlineFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_approval_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        localSongsList = view.findViewById(R.id.songs_list);

        titleTxt     = view.findViewById(R.id.track_title);
        artistTxt    = view.findViewById(R.id.track_artist);
        gediBtn      = view.findViewById(R.id.gedi_btn);
        hipHopBtn    = view.findViewById(R.id.hip_hop_btn);
        jattismBtn   = view.findViewById(R.id.jattism_btn);
        legendBtn    = view.findViewById(R.id.legend_btn);
        longDriveBtn = view.findViewById(R.id.long_drive_btn);
        mahfilBtn    = view.findViewById(R.id.mahfil_btn);
        originalBtn  = view.findViewById(R.id.original_btn);
        parentalBtn  = view.findViewById(R.id.parental_btn);
        partyBtn     = view.findViewById(R.id.party_btn);
        proBtn       = view.findViewById(R.id.pro_btn);
        rapBtn       = view.findViewById(R.id.rap_btn);
        romanceBtn   = view.findViewById(R.id.romance_btn);
        sadBtn       = view.findViewById(R.id.sad_btn);
        genderBtn    = view.findViewById(R.id.gender_btn);
        approveBtn   = view.findViewById(R.id.approve_btn);

        nextBtn      = view.findViewById(R.id.next_btn);
        deleteBtn    = view.findViewById(R.id.delete_btn);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        approvalSongsListAdapter = new ApprovalSongsListAdapter(this, serverizedTrackDataList);
        localSongsList.setLayoutManager(new LinearLayoutManager(getContext()));
        localSongsList.setAdapter(approvalSongsListAdapter);
        localSongsList.setItemViewCacheSize(20);
        localSongsList.setDrawingCacheEnabled(true);

        serverizedManager = new ServerizedManager(getContext());

        fetchUnapprovedSongs();
        onClick();
    }

    private void onClick() {
        gediBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gedi = mutuallyExclusiveBtn(gedi, gediBtn, GenreConfig.GENRE_GEDI);
            }
        });

        hipHopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hipHop = mutuallyExclusiveBtn(hipHop, hipHopBtn, GenreConfig.GENRE_HIP_HOP);
            }
        });

        jattismBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jattism = mutuallyExclusiveBtn(jattism, jattismBtn, GenreConfig.GENRE_JATTISM);
            }
        });

        legendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                legend = mutuallyExclusiveBtn(legend, legendBtn, GenreConfig.GENRE_LEGEND);
            }
        });

        longDriveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longDrive = mutuallyExclusiveBtn(longDrive, longDriveBtn, GenreConfig.GENRE_LONG_DRIVE);
            }
        });

        mahfilBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mahfil = mutuallyExclusiveBtn(mahfil, mahfilBtn, GenreConfig.GENRE_MAHFIL);
            }
        });

        originalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                original = mutuallyExclusiveBtn(original, originalBtn, GenreConfig.GENRE_ORIGINAL);
            }
        });

        parentalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parental = mutuallyExclusiveBtn(parental, parentalBtn, GenreConfig.GENRE_PARENTAL);
            }
        });

        partyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                party = mutuallyExclusiveBtn(party, partyBtn, GenreConfig.GENRE_PARTY);
            }
        });

        proBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pro = mutuallyExclusiveBtn(pro, proBtn, GenreConfig.GENRE_PRO);
            }
        });

        rapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rap = mutuallyExclusiveBtn(rap, rapBtn, GenreConfig.GENRE_RAP);
            }
        });

        romanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                romance = mutuallyExclusiveBtn(romance, romanceBtn, GenreConfig.GENRE_ROMANTIC);
            }
        });

        sadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sad = mutuallyExclusiveBtn(sad, sadBtn, GenreConfig.GENRE_SAD);
            }
        });

        genderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (gender) {
                    case 0:
                        genderBtn.setText("D");
                        gender = 1;
                        break;
                    case 1:
                        gender = 2;
                        genderBtn.setText("F");
                        break;
                    case 2:
                        gender = 0;
                        genderBtn.setText("M");
                        break;
                }
            }
        });

        approveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update to server
                uploadTrackToServerizedTracks();
                serverizedTrackDataList.get(currentTrack.getIndex()).setOriginal(2);
                approvalSongsListAdapter.notifyItemChanged(currentTrack.getIndex());
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("unapproved").child(currentTrack.getArtist().replace(" ", "_")).child(currentTrack.getTitle()).removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Config.LOG(Config.TAG_ARTIST_FOLLOW, "Deleted", false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Config.LOG(Config.TAG_ARTIST_FOLLOW, "Error in Delete " + e, false);
                    }
                });
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = currentTrack.getIndex();
                index++;
                updateData(serverizedTrackDataList.get(index));
            }
        });
    }

    private void updateUI() {
        titleTxt.setText(currentTrack.getTitle());
        artistTxt.setText(currentTrack.getArtist());
    }

    private void fetchUnapprovedSongs() {
        databaseReference.child("unapproved").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serverizedTrackDataList.clear();
                int count = 0;
                for (DataSnapshot artistData: snapshot.getChildren()) {
                    for (DataSnapshot trackData: artistData.getChildren()) {
                        ServerizedTrackData onlineTrackData = new ServerizedTrackData(
                                count,
                                trackData.child("album").getValue(String.class),
                                trackData.child("artist").getValue(String.class),
                                0,
                                "M",
                                0,
                                0,
                                0,
                                trackData.child("link").getValue(String.class),
                                0,
                                0,
                                -1,
                                0,
                                0,
                                0,
                                0,
                                trackData.child("release").getValue(String.class),
                                0,
                                0,
                                trackData.child("track").getValue(String.class)
                        );
                        serverizedTrackDataList.add(onlineTrackData);
                        count++;
                    }
                    Config.LOG(Config.TAG_UNAPPROVED, "tracks " + count, false);
                }
                approvalSongsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadTrackToServerizedTracks() {
        String artist  = currentTrack.getArtist();
        String title   = currentTrack.getTitle();
        String album   = currentTrack.getAlbum();
        String link    = currentTrack.getLink();
        String release = currentTrack.getRelease();
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("album").setValue(album);
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("artist").setValue(artist);
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("gedi").setValue(booleanToInt(gedi));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("gender").setValue(genderBtn.getText().toString());
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("hip hop").setValue(booleanToInt(hipHop));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("jattism").setValue(booleanToInt(jattism));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("legend").setValue(booleanToInt(legend));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("link").setValue(link);
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("long drive").setValue(booleanToInt(longDrive));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("mahfil").setValue(booleanToInt(mahfil));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("original").setValue(booleanToInt(original));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("parental").setValue(booleanToInt(parental));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("party").setValue(booleanToInt(party));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("pro").setValue(booleanToInt(pro));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("rap").setValue(booleanToInt(rap));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("release").setValue(release);
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("romantic").setValue(booleanToInt(romance));
        databaseReference.child("approved").child(artist.replace(" ", "_")).child(title).child("sad").setValue(booleanToInt(sad));
    }

    private Boolean mutuallyExclusiveBtn(Boolean stat, Button btn, String genre) {
        if (stat) {
            btn.setBackgroundResource(R.drawable.rounded_border_btn_20);
            return false;
        }
        btn.setBackgroundResource(GenreConfig.getGenreResource(genre));
        return true;
    }

    private int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static Fragment init(HomeToOnlineFragment homeToOnlineFragment) { return  new SongsApprovalFragment(homeToOnlineFragment);}

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void updateData(ServerizedTrackData track) {
        currentTrack = track;
        updateUI();
        serverizedTrackDataList.get(track.getIndex()).setOriginal(1);
        approvalSongsListAdapter.notifyItemChanged(track.getIndex());
        homeToOnlineFragment.playOnlineTrack(track);
        serverizedManager.dropQueue();
        homeToOnlineFragment.updateOnlineQueue();
    }
}
