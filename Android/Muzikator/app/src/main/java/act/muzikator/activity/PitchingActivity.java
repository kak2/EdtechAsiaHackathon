package act.muzikator.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import act.muzikator.R;
import act.muzikator.customView.PitchStaffView;
import act.muzikator.model.PitchingNote;
import act.muzikator.utils.PitchingUtils;
import act.muzikator.utils.SoundManager;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import butterknife.Bind;
import butterknife.OnClick;

public class PitchingActivity extends BaseActivity {
    private static final int SAMPLE_RATE     = 22050;
    private static final int BUFFER_SIZE     = 1024;
    private static final int OVERLAP         = 0;
    private static final int NOTE_DURATION   = 1000;
    private static final int PITCH_THREAHOLD = 500;

    @Bind(R.id.pitchingStaff) PitchStaffView pitchingStaff;
    @Bind(R.id.passCount)     TextView       passCount;
    @Bind(R.id.comment)       ImageView      comment;

    private AudioDispatcher    dispatcher;
    private SoundManager       soundManager;
    private int                originalRoundCount;
    private long               lastMarkedTime;
    private int                lastNoteIndex;
    private int                currentNoteIndex;
    private int                accuTime;
    private int                lowAccuTime;
    private int                highAccuTime;
    private int                rightNoteCount;
    private List<PitchingNote> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitching);

        PitchingUtils.prepare();
        Map<String, PitchingNote> noteMap = PitchingUtils.getNoteMap();
        notes = new ArrayList<>();
        notes.add(noteMap.get("o0"));
        notes.add(noteMap.get("o0"));
        notes.add(noteMap.get("C4"));
        notes.add(noteMap.get("D4"));
        notes.add(noteMap.get("E4"));
        notes.add(noteMap.get("F4"));
        notes.add(noteMap.get("G4"));
        notes.add(noteMap.get("A4"));
        pitchingStaff.setNotes(notes);
        originalRoundCount = notes.size() - 1;
        lastNoteIndex = 0;
        currentNoteIndex = 0;

        pitchingStaff.getLayoutParams().width = notes.size() * getResources().getDimensionPixelSize(R.dimen.pitchNoteSide);
        passCount.setText(String.format("0/%d", notes.size()));


        soundManager = new SoundManager();
        soundManager.initSounds(this);
        soundManager.addSoundResource(R.raw.c4);
        soundManager.addSoundResource(R.raw.d4);
        soundManager.addSoundResource(R.raw.e4);
        soundManager.addSoundResource(R.raw.f4);
        soundManager.addSoundResource(R.raw.g4);
        soundManager.addSoundResource(R.raw.a4);
        soundManager.addSoundResource(R.raw.b4);
        soundManager.addSoundResource(R.raw.c5);
        soundManager.addSoundResource(R.raw.d5);
        soundManager.addSoundResource(R.raw.e5);
        soundManager.addSoundResource(R.raw.f5);
        soundManager.addSoundResource(R.raw.g5);
    }

    private void startRecording() {
        if (dispatcher != null) {
            dispatcher.stop();
        }
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, OVERLAP);
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long currentMarkedTime = Calendar.getInstance().getTimeInMillis();

                        if (currentNoteIndex == lastNoteIndex) {
                            if (currentNoteIndex >= notes.size()) return;

                            int frequencyAtPitch = notes.get(currentNoteIndex).isFrequencyAtPitch(pitchInHz);
                            if (frequencyAtPitch == 0) {
                                accuTime += (currentMarkedTime - lastMarkedTime);
                                comment.setImageResource(R.drawable.ic_perfect);
                            } else if (frequencyAtPitch > 0) {
                                highAccuTime += (currentMarkedTime - lastMarkedTime);
                                comment.setImageResource(R.drawable.ic_too_high);
                            } else {
                                lowAccuTime += (currentMarkedTime - lastMarkedTime);
                                comment.setImageResource(R.drawable.ic_too_low);
                            }
                            lastMarkedTime = currentMarkedTime;
                        } else {
                            pitchingStaff.markNoteResult(lastNoteIndex, accuTime >= PITCH_THREAHOLD);
                            if (accuTime >= PITCH_THREAHOLD) {
                                rightNoteCount++;
                                passCount.setText(String.format("%d/%d", rightNoteCount, notes.size()));

                                comment.setImageResource(R.drawable.ic_perfect);
                            } else {
                                if (highAccuTime > lowAccuTime) {
                                    comment.setImageResource(R.drawable.ic_too_high);
                                } else {
                                    comment.setImageResource(R.drawable.ic_too_low);
                                }
                            }
                            lastNoteIndex = currentNoteIndex;
                            accuTime = 0;

                            if (currentNoteIndex >= notes.size()) return;

                            int frequencyAtPitch = notes.get(currentNoteIndex).isFrequencyAtPitch(pitchInHz);
                            if (frequencyAtPitch == 0) {
                                accuTime += (currentMarkedTime - lastMarkedTime);
                            } else if (frequencyAtPitch > 0) {
                                highAccuTime += (currentMarkedTime - lastMarkedTime);
                            } else {
                                lowAccuTime += (currentMarkedTime - lastMarkedTime);
                            }
                            lastMarkedTime = currentMarkedTime;
                        }
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, SAMPLE_RATE, BUFFER_SIZE, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    private void stopRecording() {
        dispatcher.stop();
    }

//    @Override
//    protected void onDestroy() {
//        if (dispatcher != null) {
//            dispatcher.stop();
//        }
//
//        super.onDestroy();
//    }

    @OnClick(R.id.playButtton)
    public void playPitching() {
        playNoteAtIndex(0);
    }

    private void playNoteAtIndex(final int noteAtIndex) {
        if (noteAtIndex < notes.size()) {
            PitchingNote note = notes.get(noteAtIndex);
            if (note.getImageResId() != R.drawable.o0) {
                soundManager.playSoundResource(note.getSoundResId());
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playNoteAtIndex(noteAtIndex + 1);
                }
            }, 1000);
        }
    }

    @OnClick(R.id.recordButton)
    public void recordPitching() {
        final int noteWidth = getResources().getDimensionPixelSize(R.dimen.pitchNoteSide);
        startRecording();
        lastMarkedTime = Calendar.getInstance().getTimeInMillis();
        lastNoteIndex = 0;
        accuTime = 0;

        animateStaff(noteWidth);
    }

    private void animateStaff(final int noteWidth) {
        pitchingStaff.animate().setInterpolator(new LinearInterpolator()).translationX(-noteWidth * (currentNoteIndex + 1)).setDuration(NOTE_DURATION).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentNoteIndex < notes.size()) {
                    currentNoteIndex++;
                    animateStaff(noteWidth);
                } else {
                    stopRecording();
                }
            }
        });
    }
}
