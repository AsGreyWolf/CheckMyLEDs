package com.asgreywolf.checkmyleds;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

public class MainActivity extends Activity {
	DataOutputStream os;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Process p;	
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
		} catch (IOException e) {
		// Code to run in input/output exception
		((TextView)findViewById(R.id.rootstatus)).setText(getResources().getString(R.string.RootNO));
		}
		
		//int i=0;
		//leds[i]=new LED();
		//leds[i].name="White";
		//leds[i].blink="/sys/class/leds/white/device/blink";
		//leds[i].brightness="/sys/class/leds/white/brightness";
		//if(leds[i].load()) i++;
		
		//leds[i]=new LED();
		//leds[i].name="Screen";
		//leds[i].blink=null;
		//leds[i].brightness="/sys/class/leds/lcd-backlight/brightness";
		//if(leds[i].load()) i++;
		search(new File("/sys/class/leds"), 0);
		//search(new File("/sys/devices/platform"), 0);

		search(new File("/sys/class/backlight"), 0);
		if(ar.size()>0){
			LED leds[]=new LED[ar.size()];
			String[] blankstr=new String[ar.size()];
			for(int l=0;l<ar.size();l++){
				blankstr[l]="Loading";
				leds[l]=(LED) ar.get(l);
			}
			((ListView)findViewById(R.id.activity_main_lv)).setAdapter(new LEDAdapter(this, leds, blankstr));
		}
	}
	ArrayList<LED> ar=new ArrayList<LED>();
	protected void onDestroy(){

		try {
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}
	void search(File path, int step){

		//Log.e("CheckMyLEDs", "checking "+path.getAbsolutePath());
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
						l.load();
						ar.add(l);
						Log.e("CheckMyLEDs", f.getAbsolutePath());
					}
				}
			
		}
	}
	File searchBlink(File path, int step){
		//Log.e("CheckMyLEDs", "blink "+path.getAbsolutePath());
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
	File searchTrigger(File path, int step){
		File s=null;
		//Log.e("CheckMyLEDs", "trigger "+path.getAbsolutePath());
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
