package act.muzikator.model;

/**
 * Created by huy.pham@robusttechhouse.com on 4/9/16.
 */
public class PitchingNote {
    private String noteName;
    private int    imageResId;
    private int    soundResId;
    private float  pitch;
    private float  rightBelowPitch;
    private float  rightAbovePitch;
    private float  lowerBoundPitch;
    private float  upperBoundPitch;

    public PitchingNote(String noteName, int imageResId, float pitch, float rightBelowPitch, float rightAbovePitch, int soundResId) {
        this.noteName = noteName;
        this.imageResId = imageResId;
        this.pitch = pitch;
        this.rightBelowPitch = rightBelowPitch;
        this.rightAbovePitch = rightAbovePitch;
        this.soundResId = soundResId;

        lowerBoundPitch = rightBelowPitch + (pitch - rightBelowPitch) / 2;
        upperBoundPitch = pitch + (rightAbovePitch - pitch) / 2;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRightBelowPitch() {
        return rightBelowPitch;
    }

    public void setRightBelowPitch(float rightBelowPitch) {
        this.rightBelowPitch = rightBelowPitch;
    }

    public float getRightAbovePitch() {
        return rightAbovePitch;
    }

    public void setRightAbovePitch(float rightAbovePitch) {
        this.rightAbovePitch = rightAbovePitch;
    }

    public float getLowerBoundPitch() {
        return lowerBoundPitch;
    }

    public void setLowerBoundPitch(float lowerBoundPitch) {
        this.lowerBoundPitch = lowerBoundPitch;
    }

    public float getUpperBoundPitch() {
        return upperBoundPitch;
    }

    public void setUpperBoundPitch(float upperBoundPitch) {
        this.upperBoundPitch = upperBoundPitch;
    }

    public int getSoundResId() {
        return soundResId;
    }

    public void setSoundResId(int soundResId) {
        this.soundResId = soundResId;
    }

    public int isFrequencyAtPitch(float freq) {
        if (pitch < 0) {
            return freq < 0 ? 0 : 1;
        } else {
            if (freq > lowerBoundPitch && freq < upperBoundPitch) {
                return 0;
            } else if (freq > upperBoundPitch) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
