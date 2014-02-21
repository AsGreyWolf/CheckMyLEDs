package com.asgreywolf.checkmyleds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

public class LED {
	String brightness;
	String blink;
	String trigger;
	
	ArrayList<String> triggers=new ArrayList<String>();
	int usedtrigger;
	
	int br;
	boolean bl;
	boolean rgb;
	boolean rgb_s;
	String rgb_start;
	String name;
	boolean load(){
		
		File f=new File(brightness);
		if(!f.canRead()) return false;
			FileInputStream fis;
			try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				Log.e("CheckMyLEDs", "Error loading LED", e);
				return false;
			}
		   InputStreamReader isr = new InputStreamReader(fis);
		   BufferedReader bufferedReader = new BufferedReader(isr);
		   String line;
		   try {
			   line= bufferedReader.readLine();
			   bufferedReader.close();
		   } catch (IOException e) {
			   Log.e("CheckMyLEDs", "Error loading LED", e);
				return false;
		   }
		   float bbr=Integer.parseInt(line);
		   bbr=bbr/250*100;
		   br=(int) bbr;
		   if(blink!=null){
			f=new File(blink);
			if(!f.canRead()) blink=null;
				try {
					fis = new FileInputStream(f);
				} catch (FileNotFoundException e) {
					Log.e("CheckMyLEDs", "Error loading LED", e);
					blink=null;
				}
			  isr = new InputStreamReader(fis);
			   bufferedReader = new BufferedReader(isr);
			   try {
				   line= bufferedReader.readLine();
				   bufferedReader.close();
			   } catch (IOException e) {
				   Log.e("CheckMyLEDs", "Error loading LED", e);
				   blink=null;
			   }
			   bl=Integer.parseInt(line)>=1 ? true : false;
		   }
		  if(trigger!=null){
			   StringBuilder sb=new StringBuilder();
			   	f=new File(trigger);
				if(!f.canRead()) trigger=null;
					try {
						fis = new FileInputStream(f);
					} catch (FileNotFoundException e) {
						Log.e("CheckMyLEDs", "Error loading LED", e);
						trigger=null;
					}
				  isr = new InputStreamReader(fis);
				   bufferedReader = new BufferedReader(isr);
				   
				   char c[]=new char[1];
				   boolean started = false;
				   int pos=0;
				   try {
					while(bufferedReader.read(c,0,1)!=-1){
						if(c[0]==' ' || c[0]=='\n'){
							if(started){
								pos++;
								triggers.add(sb.toString());
								sb.setLength(0);
								started=false;
							}
						}
						else if(c[0]=='['){
							usedtrigger=pos;
						}
						else if(c[0]==']'){
						}
						else{
							sb.append(c[0]);
							started=true;
						}
					}
					if(started)
						triggers.add(sb.toString());
				} catch (IOException e) {
						Log.e("CheckMyLEDs", "Error loading LED", e);
					   trigger=null;
				}
				   try {
					   bufferedReader.close();
				   } catch (IOException e) {
					   Log.e("CheckMyLEDs", "Error loading LED", e);
					   trigger=null;
				   }
		   }
		   return true;
	}
	void reset(MainActivity context){
		try {
			context.os.writeBytes("echo 0 > "+brightness+"\n");
			br=0;
			Log.e("CheckMyLEDs", "Resetting LED "+brightness);
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error setting LED", e);
		}
	}
	void setLight(int val,MainActivity context){
		if(!name.equals("lcd-backlight") && !bl)
			reset(context);
		try {
			float bbr=val;
			bbr=bbr/100*250;
			context.os.writeBytes("echo "+(int)bbr+" > "+brightness+"\n");
			br=val;
			
			Log.e("CheckMyLEDs", "Setting LED "+brightness+" to "+(int)bbr);
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error setting LED", e);
		}
	}
	void setBlink(boolean val,MainActivity context){
		try {
			context.os.writeBytes("echo "+(val ? 1 : 0)+" > "+blink+"\n");
			bl=val;
			Log.e("CheckMyLEDs", "Setting LED "+blink+" to "+val);
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error setting LED", e);
		}
		setLight(br,context);
	}
	public void setTrigger(int id, MainActivity context) {
		reset(context);
		try {
			context.os.writeBytes("echo "+triggers.get(id)+" > "+trigger+"\n");
			usedtrigger=id;
			Log.e("CheckMyLEDs", "Setting LED "+trigger+" to "+id);
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error setting LED", e);
		}
		//setLight(br,context);
	}
}
