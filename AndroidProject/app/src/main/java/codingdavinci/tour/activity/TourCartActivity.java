package codingdavinci.tour.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.UUID;

import codingdavinci.tour.R;
import codingdavinci.tour.listener.LoadTourListener;
import codingdavinci.tour.listener.SaveTourListener;
import codingdavinci.tour.listener.UuidListItemListener;
import codingdavinci.tour.manager.DataProvider;
import codingdavinci.tour.manager.NoteManager;
import codingdavinci.tour.model.DatasetObject;
import codingdavinci.tour.model.Tour;
import codingdavinci.tour.model.UuidListItem;
import codingdavinci.tour.util.ActivityHelper;
import codingdavinci.tour.util.TourHelper;
import codingdavinci.tour.util.ViewHelper;
import codingdavinci.tour.view.NavigationButton;
import codingdavinci.tour.view.RecyclerViewListAdapter;

public class TourCartActivity extends CdvActivity implements UuidListItemListener {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Tour tour;
    private NavigationButton btnApply;
    private NavigationButton btnAddToCart;
    private NavigationButton btnDraft;
    private NavigationButton btnDelete;
    private NavigationButton btnAudioRecord;
    private NavigationButton btnAudioPlay;
    private RecyclerView listDatasetObjects;
    private RecyclerViewListAdapter rowAdapter;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private boolean isAudioRecording = false;
    private boolean isAudioPlaying = false;
    private DatasetObject currentObject;

    private class SwipeCallback extends ItemTouchHelper.SimpleCallback {
        public SwipeCallback() {
            super(0, ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            tour.removeDatasetObject((DatasetObject) rowAdapter.getUuidListItem(viewHolder.getAdapterPosition()));
            updateBasketContent();

            rowAdapter = new RecyclerViewListAdapter(TourCartActivity.this, tour.getDatasetObjects(), R.layout.generic_list_item, TourCartActivity.this);
            listDatasetObjects.setAdapter(rowAdapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_cart);
        initUiObjects();

        UUID tourUuid = ActivityHelper.getTourUuid(getIntent());
        Log.i("cdv", "Tour UUID: " + tourUuid.toString());
        DataProvider.loadTour(tourUuid, new LoadTourListener() {
            @Override
            public void tourLoaded(@NonNull Tour tour) {
                TourCartActivity.this.tour = TourHelper.createCopy(tour, false);
                buildScreen();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        buildScreen();
    }

    @Override
    protected void initUiObjects() {
        buildActionBar();

        // Navigation buttons
        LinearLayout navigationBar = ViewHelper.createNavigationBar(this, R.id.main);
        btnApply = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_ok, R.string.nav_apply);
        btnAddToCart = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_add_to_cart, R.string.nav_add_more);
        btnDraft = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_draft, R.string.nav_save_as_draft);
        btnDelete = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_delete, R.string.nav_delete_tour);
        btnAudioRecord = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_up, R.string.nav_audio_record);
        btnAudioPlay = ViewHelper.addNavigationButton(this, navigationBar, R.drawable.icons8_right, R.string.nav_audio_play);
        btnAudioRecord.rootView.setVisibility(View.GONE);
        btnAudioPlay.rootView.setVisibility(View.GONE);
        listDatasetObjects = findViewById(R.id.listViewDatasetObjects);
        listDatasetObjects.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        listDatasetObjects.setHasFixedSize(true);
        listDatasetObjects.setLayoutManager(new LinearLayoutManager(this));

        new ItemTouchHelper(new SwipeCallback()).attachToRecyclerView(listDatasetObjects);
    }

    private void initUIAudio() {
        // Button audio record
        btnAudioRecord.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if permission was already granted by the user
                if (ContextCompat.checkSelfPermission(TourCartActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TourCartActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
                    return;
                }
                
                if (isAudioRecording) {
                    stopAudioRecording();
                } else {
                    if (currentObject == null) {
                        NoteManager.showError(TourCartActivity.this, R.string.select_object);
                    }
                    startAudioRecording();
                }
            }
        });

        // Button audio play
        btnAudioPlay.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAudioPlaying) {
                    stopAudioPlaying();
                } else {
                    startAudioPlaying();
                }
            }
        });
    }

    @Override
    protected void buildScreen() {
        initUIAudio();

        // Button apply
        btnApply.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTourAndContinue(true);
            }
        });

        // Button cart
        btnAddToCart.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTourAndBack();
            }
        });

        btnDraft.rootView.setOnClickListener(TourHelper.createSaveDraftListener(this, tour, true));
        btnDelete.rootView.setOnClickListener(TourHelper.createDeleteListener(this, MainActivity.class, tour));

        rowAdapter = new RecyclerViewListAdapter(TourCartActivity.this, tour.getDatasetObjects(), R.layout.generic_list_item, TourCartActivity.this);
        listDatasetObjects.setAdapter(rowAdapter);

        updateBasketContent();
    }

    private void updateBasketContent() {
        btnApply.textView.setText(getText(R.string.nav_apply) + "(" + tour.getDatasetObjects().size() + ")");
    }

    private void saveTourAndContinue(boolean showErrors) {
        DataProvider.saveTour(tour, true, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(TourCartActivity.this, CreateTourActivity.class, null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(TourCartActivity.this, R.string.internal_error);
            }
        });
    }

    private void saveTourAndBack() {
        DataProvider.saveTour(tour, true, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                ActivityHelper.startCdvActivity(TourCartActivity.this, SelectDatasetObjectsActivity.class, null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(TourCartActivity.this, R.string.internal_error);
            }
        });
    }

    @Override
    public void onClick(int position, UuidListItem uuidListItem) {
        currentObject = (DatasetObject)uuidListItem;
        updateAudio();
    }

    @Override
    protected void onPause() {
        stopAudioAll();
        super.onPause();
    }

    private void stopAudioAll() {
        if (isAudioPlaying) {
            stopAudioPlaying();
        }
        if (isAudioRecording) {
            startAudioRecording();
        }
    }

    private void updateAudio() {
        stopAudioAll();
    }

    @Override
    public void onBackPressed() {
        stopAudioAll();
        DataProvider.saveTour(tour, true, new SaveTourListener() {
            @Override
            public void tourSaved(Tour tour) {
                // as this activity could be started by different other activities choose the correct previous screen
                ActivityHelper.startCdvActivity(TourCartActivity.this, getPreviousActivityClass(), null, ActivityHelper.EXTRA_TOUR_UUID, tour.getTourUuid());
            }

            @Override
            public void errorOccured() {
                NoteManager.showError(TourCartActivity.this, R.string.internal_error);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionToRecordAccepted = false;
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                break;
        }
        if (!permissionToRecordAccepted) {
            return;
        }
        NoteManager.showSnack(findViewById(R.id.root), R.string.ready_to_record_audio);
    }

    private void startAudioPlaying() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getAudioFilename());
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnAudioPlay.imageButton.setImageResource(R.drawable.icons8_left);
            isAudioPlaying = true;
        } catch (IOException e) {
            Log.e("cdv", "prepare() MediaPlayer failed");
            NoteManager.showError(this, R.string.internal_error);
        }
    }

    private String getAudioFilename() {
        // Record to the external cache directory for visibility
        String filename = getExternalCacheDir().getAbsolutePath();
        filename += "/TourWizArt_tmp.3gp";
        return filename;
    }

    private void stopAudioPlaying() {
        mediaPlayer.release();
        mediaPlayer = null;
        btnAudioPlay.imageButton.setImageResource(R.drawable.icons8_right);
        isAudioPlaying = false;
    }

    private void startAudioRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getAudioFilename());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            btnAudioRecord.imageButton.setImageResource(R.drawable.icons8_down);
            isAudioRecording = true;
        } catch (IOException e) {
            Log.e("cdv", "prepare() MediaPlayer failed");
            NoteManager.showError(this, R.string.internal_error);
        }
    }

    private void stopAudioRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        btnAudioRecord.imageButton.setImageResource(R.drawable.icons8_up);
        isAudioRecording = false;
    }

}
