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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class MainActivity extends Activity {
	private int [] mVizData = new int[1];
	private final Handler mHandler = new Handler();
	private AudioCapture mAudioCapture;
	//float maxWave=0;
	//float minWave=0;
	AudioManager am;
	ProgressBar audioBar;
	private final Runnable audioGet = new Runnable() {
        public void run() {
        	int data=0;
        	if (mAudioCapture != null) {
        		 int volume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mVizData = mAudioCapture.getFormattedData(1, 1);
                if(mVizData.length>0){ 
                	data=(int) (mVizData[0]*100.0f/volume); 	
                }
            }
        	for(int i=0;i<leds.length;i++)
        		leds[i].capture(data,MainActivity.this);
        	audioBar.setProgress(data+255*2);
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
		try {
			// Preform su to get root privledges
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
		mAudioCapture = new AudioCapture(AudioCapture.TYPE_FFT, 1);
		mAudioCapture.start();
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
	}
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
