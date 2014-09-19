package com.asgreywolf.checkmyleds;

import android.media.MediaRecorder;

public class AudioCaptureMic {
	private MediaRecorder mRecorder = null;
	private double mEMA = 0.0;
	public AudioCaptureMic() {

	}
	public void start() {
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile("/dev/null"); 
			try {
				mRecorder.prepare();
			} catch (Exception e) {
			}
			mRecorder.start();
			mEMA = 0.0;
		}
	}
	public void stop() {
		if (mRecorder != null) {
			mRecorder.stop();       
			mRecorder.release();
			mRecorder = null;
		}
	}
	public void release() {

	}
	public double getRawData(double delitel) {
		if (mRecorder != null)
            return  (mRecorder.getMaxAmplitude()/delitel);
		else
            return 0;
	}
	public double getFormattedData(double delitel,double filter) {
		double amp = getRawData(delitel);
        mEMA = filter * amp + (1.0 - filter) * mEMA;
        return mEMA;
	}
	private boolean captureData() {
		return true;
	}
}
