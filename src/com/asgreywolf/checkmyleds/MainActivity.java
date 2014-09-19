package com.asgreywolf.checkmyleds;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.SeekBar;

/**
 * Main Window
 * @author AsGreyWolf
 */
public class MainActivity extends Activity {
	/**
	 * Captured data
	 */
	private int [] mVizData = new int[1];
	private final Handler mHandler = new Handler();
	private AudioCaptureMic mAudioCaptureMic;
	private AudioCapture mAudioCapture;
	AudioManager am;
	ProgressBar audioBar;
	boolean mic=false;
	int micSens=100;
	double micFilter=0.6;
	/**
	 * Audio capture thread
	 */
	private final Runnable audioGet = new Runnable() {
		public void run() {
			double data=0;
			if (mAudioCapture != null && !mic) {
				int volume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				mVizData =  mAudioCapture.getFormattedData(1,1);
				if(mVizData.length>0){ 
					data=(mVizData[0]*100.0f/volume); 	
				}
			}else if (mAudioCaptureMic != null && mic) {
				data =  mAudioCaptureMic.getFormattedData(micSens,micFilter);
			}
			for(int i=0;i<leds.length;i++)
				leds[i].capture((int) data,MainActivity.this); //update leds
			audioBar.setProgress((int) (data+255*2)); //update progressbar
			mHandler.postDelayed(audioGet, 1000 / 25);
		}
	};
	LED leds[];
	DataOutputStream os;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new AlertDialog.Builder(this)
		.setTitle("Attention!")
		.setMessage("Attention! The author is not liable for any damage caused by the program. Whatever you do, you do at your own risk.")
		.setPositiveButton(android.R.string.yes, null)
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				MainActivity.this.finish();
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show();
		Process p;	
		audioBar=(ProgressBar)findViewById(R.id.audioBar);
		audioBar.setMax(255*4);
		//check root status
		try {
			p = Runtime.getRuntime().exec("su");
			os=new DataOutputStream( p.getOutputStream() );
			os.writeBytes("mount -o remount,rw /system\n");
			os.writeBytes("touch /system/bin/CheckMyLEDs.txt\n");
			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
			}
			if(!new File("/system/bin/CheckMyLEDs.txt").exists()){
				((TextView)findViewById(R.id.rootstatus)).setText(getResources().getString(R.string.RootNO));	
			}
			p = Runtime.getRuntime().exec("su");
			os=new DataOutputStream( p.getOutputStream() );
			os.writeBytes("rm /system/bin/CheckMyLEDs.txt\n");
			os.writeBytes("mount -o remount,ro /system\n");
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
			((TextView)findViewById(R.id.rootstatus)).setText(getResources().getString(R.string.RootNO));
		}
		search(new File("/sys/class/leds"), 0);
		//search(new File("/sys/devices/platform"), 0);
		search(new File("/sys/class/backlight"), 0);
		((CheckBox)findViewById(R.id.mic)).setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked){
					if (mAudioCapture != null) {
						mAudioCapture.stop();
						mAudioCapture.release();
						mAudioCapture = null;
					}
					mAudioCaptureMic = new AudioCaptureMic();
					mAudioCaptureMic.start();
				}else{
					if (mAudioCaptureMic != null) {
						mAudioCaptureMic.stop();
						mAudioCaptureMic.release();
						mAudioCaptureMic = null;
					}
					mAudioCapture = new AudioCapture(AudioCapture.TYPE_FFT,1);
					mAudioCapture.start();
				}
				mic=isChecked;
			}});
		SeekBar sens=(SeekBar)findViewById(R.id.micSens);
		sens.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				micSens=progress+1;
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		sens.setProgress(micSens-1);
		SeekBar filter=(SeekBar)findViewById(R.id.micFilter);
		filter.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				micFilter=progress*1.0/100;
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		filter.setProgress((int) (micFilter*100));
	}
	ArrayList<LED> ar=new ArrayList<LED>();
	WakeLock wakeLock;
	@SuppressWarnings("deprecation")
	@Override
	public void onResume(){
		super.onResume();
		PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
		wakeLock.acquire();
		Process p;
		try {
			p = Runtime.getRuntime().exec("su");
			os=new DataOutputStream( p.getOutputStream() );
		} catch (IOException e) {
		}
		if(ar.size()>0){
			leds=new LED[ar.size()];
			String[] blankstr=new String[ar.size()];
			for(int l=0;l<ar.size();l++){
				blankstr[l]="Loading";
				leds[l]=(LED) ar.get(l);
				leds[l].load();
			}
			((ListView)findViewById(R.id.activity_main_lv)).setAdapter(new LEDAdapter(this, leds, blankstr));
		}
		if(mic){
			mAudioCaptureMic = new AudioCaptureMic();
			mAudioCaptureMic.start();
		}else{
			mAudioCapture = new AudioCapture(AudioCapture.TYPE_FFT,1);
			mAudioCapture.start();
		}
		am = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
		mHandler.postDelayed(audioGet, 1000 / 25);

	}
	@Override
	public void onPause(){
		super.onPause();
		wakeLock.release();
		for(int i=0;i<leds.length;i++){
			leds[i].close(this);
		}
		try {
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
		}
		mHandler.removeCallbacks(audioGet);
		if (mAudioCapture != null) {
			mAudioCapture.stop();
			mAudioCapture.release();
			mAudioCapture = null;
		}
		if (mAudioCaptureMic != null) {
			mAudioCaptureMic.stop();
			mAudioCaptureMic.release();
			mAudioCaptureMic = null;
		}
	}
	/**
	 * Searches for led descriptors in the directory
	 * @param path Directory
	 * @param step Recursive step
	 */
	void search(File path, int step){
		if(step>3) return;
		File[] list=path.listFiles();
		if(list==null) return;
		for(File f:list){
			if(f.isDirectory()){
				search(f,step+1);
			}else{
				boolean sovp = false;
				for(int i=0;i<ar.size();i++){
					if(new File(((LED)ar.get(i)).brightness).getParentFile().getName().compareTo(f.getParentFile().getName())==0){
						sovp=true;
						break;
					}
				}
				if(sovp) continue;
				if(f.getName().compareTo("brightness")==0){
					LED l=new LED();
					l.brightness=f.getAbsolutePath();
					l.name=f.getParentFile().getName();
					File b=searchBlink(f.getParentFile(),0);
					if(b==null)
						l.blink=null;
					else
						l.blink=b.getAbsolutePath();
					b=searchTrigger(f.getParentFile(),0);
					if(b==null)
						l.trigger=null;
					else
						l.trigger=b.getAbsolutePath();

					File rgb_start=new File(f.getParentFile(),"rgb_start");
					if(rgb_start.exists()){
						l.rgb_start=rgb_start.getAbsolutePath();
					}

					ar.add(l);
					Log.e("CheckMyLEDs", f.getAbsolutePath());
				}
			}
		}
	}
	/**
	 * Searches for led blink file descriptor in the directory
	 * @param path Directory
	 * @param step Recursive step
	 * @return
	 */
	File searchBlink(File path, int step){
		File s=null;
		if(step>2) return null;
		File[] list=path.listFiles();
		if(list==null) return null;
		for(File f:list){
			if(f.isDirectory()){
				s=searchBlink(f,step+1);
				if(s!=null) return s;
			}else{
				if(f.getName().compareTo("blink")==0){
					return f;
				}
			}
		}
		return null;
	}
	/**
	 * Searches for max brightness file descriptor in the directory
	 * @param path Directory
	 * @param step Recursive step
	 * @return
	 */
	File searchMax(File path, int step){
		File s=null;
		if(step>2) return null;
		File[] list=path.listFiles();
		if(list==null) return null;
		for(File f:list){
			if(f.isDirectory()){
				s=searchBlink(f,step+1);
				if(s!=null) return s;
			}else{
				if(f.getName().compareTo("max_brightness")==0){
					return f;
				}
			}
		}
		return null;
	}
	/**
	 * Searches for trigger file descriptor in the directory
	 * @param path Directory
	 * @param step Recursive step
	 * @return
	 */
	File searchTrigger(File path, int step){
		File s=null;
		if(step>1) return null;
		File[] list=path.listFiles();
		if(list==null) return null;
		for(File f:list){
			if(f.isDirectory()){
				s=searchTrigger(f,step+1);
				if(s!=null) return s;
			}else{
				if(f.getName().compareTo("trigger")==0){
					return f;
				}
			}
		}
		return null;
	}
	int idd;
	/**
	 * Trigger dialog
	 * @param id LED id
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		idd=id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder = new AlertDialog.Builder(this);
		docl d=new docl();
		d.id=idd;
		d.context=this;
		String []dsf = new String[ar.get(idd).triggers.size()];
		ar.get(idd).triggers.toArray(dsf);
		builder.setTitle(getResources().getString(R.string.select_trigger))
		.setCancelable(false)
		.setNeutralButton(getResources().getString(R.string.back),
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int id) {
				dialog.cancel();
			}
		})
		.setSingleChoiceItems(dsf, ar.get(idd).usedtrigger,d);
		return builder.create();
	}
	class docl implements DialogInterface.OnClickListener{
		int id;
		MainActivity context;
		@Override
		public void onClick(DialogInterface dialog, int which) {
			ar.get(id).setTrigger(which,context);
		}
	}
}
