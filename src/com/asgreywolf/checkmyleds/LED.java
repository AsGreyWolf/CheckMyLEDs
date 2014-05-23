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
	String max_brightness;
	String blink;
	String trigger;
	String rgb_start;
	static String lcd="lcd-backlight";
	static String echo="echo ";
	static String to=" > ";
	static String end="\n";
	ArrayList<String> triggers=new ArrayList<String>();
	int usedtrigger=0;
	int br=255;
	boolean bl=false;
	int maxbr=255;
	//boolean rgb;
	//boolean rgb_s;
	//String rgb_start;
	String name;
	
	boolean usedMusic=false;
	int musicValueTo=17;
	int musicValueFrom=0;
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
		   //bbr=bbr/250*100;
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
		  if(max_brightness!=null){
				f=new File(max_brightness);
				if(!f.canRead()) max_brightness=null;
					try {
						fis = new FileInputStream(f);
					} catch (FileNotFoundException e) {
						Log.e("CheckMyLEDs", "Error loading LED", e);
						max_brightness=null;
					}
				  isr = new InputStreamReader(fis);
				   bufferedReader = new BufferedReader(isr);
				   try {
					   line= bufferedReader.readLine();
					   bufferedReader.close();
				   } catch (IOException e) {
					   Log.e("CheckMyLEDs", "Error loading LED", e);
					   max_brightness=null;
				   }
				   maxbr=Integer.parseInt(line);
			   }
		  //startMusic();
		   return true;
	}
	void reset(MainActivity context){
		try {
			buffer.setLength(0);
			buffer.append(echo);
			buffer.append(0);
			buffer.append(to);
			buffer.append(brightness);
			buffer.append(end);
			context.os.writeBytes(buffer.toString());
			if(rgb_start!=null)
			{
				buffer.setLength(0);
				buffer.append(echo);
				buffer.append(0);
				buffer.append(to);
				buffer.append(rgb_start);
				buffer.append(end);
				context.os.writeBytes(buffer.toString());
			}
			//br=0;
			//Log.e("CheckMyLEDs", "Resetting LED "+name);
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error setting LED", e);
		}
	}
	void setLight(int val,MainActivity context){
		br=val;
		write(context);
	}
	void setBlink(boolean val,MainActivity context){
		bl=val;
		write(context);
	}
	public void setTrigger(int id, MainActivity context) {
		usedtrigger=id;
		write(context);
	}
	StringBuilder buffer=new StringBuilder();
	public void write(MainActivity context){
		try {
			if(!name.equals(lcd))
			reset(context);
			//if(usedtrigger!=0){
			//	br=maxbr;
			//	bl=false;
			//}
			if(blink!=null){
				buffer.setLength(0);
				buffer.append(echo);
				buffer.append(bl ? 1 : 0);
				buffer.append(to);
				buffer.append(blink);
				buffer.append(end);
				context.os.writeBytes(buffer.toString());
			}
			if(trigger!=null)
			{
				buffer.setLength(0);
				buffer.append(echo);
				buffer.append(triggers.get(usedtrigger));
				buffer.append(to);
				buffer.append(trigger);
				buffer.append(end);
				context.os.writeBytes(buffer.toString());
			}
			buffer.setLength(0);
			buffer.append(echo);
			buffer.append((int)br);
			buffer.append(to);
			buffer.append(brightness);
			buffer.append(end);
			context.os.writeBytes(buffer.toString());
			if(rgb_start!=null)
			{
				buffer.setLength(0);
				buffer.append(echo);
				buffer.append(1);
				buffer.append(to);
				buffer.append(rgb_start);
				buffer.append(end);
				context.os.writeBytes(buffer.toString());
			}
		} catch (IOException e) {
			Log.e("CheckMyLEDs", "Error updating LED", e);
		}
	}
	public void capture(int data, MainActivity context){
		if(usedMusic)
			if(musicValueTo>=musicValueFrom)
				setLight(data>musicValueTo ? maxbr : (data<musicValueFrom ? 0 : (int)((data-musicValueFrom)*1.0f/(musicValueTo-musicValueFrom)*maxbr)), context);
			else
				setLight(data>musicValueFrom ? 0 : (data<musicValueTo ? maxbr : (int)((data-musicValueFrom)*1.0f/(musicValueTo-musicValueFrom)*maxbr)), context);
	}
	int savedState;
	public void startMusic(){
		if(!usedMusic)
		savedState=br;
		usedMusic=true;
	}
	public void stopMusic(MainActivity context){
		if(usedMusic)
			br=savedState;
		usedMusic=false;
		write(context);
	}
	public void close(MainActivity context){
		stopMusic(context);
	}
}
