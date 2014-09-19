package com.asgreywolf.checkmyleds;


import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
public class LEDAdapter extends ArrayAdapter<String> {
	private final MainActivity context;
	LED[] leds;
	ViewHolder[] holders;
	public LEDAdapter(MainActivity context, LED[] leds,String[] str) {
		super(context, R.layout.ledlayout, str);
		this.context = context;
		this.leds= leds;
		holders=new ViewHolder[leds.length];
		clicked=new boolean[leds.length];
	}
	static class ViewHolder {
		public TextView name;
		public CheckBox blink;
		public SeekBar brightness;
		public Button trigger;
		public CheckBox capture;
		public SeekBar captureSize;
		public SeekBar captureSize2;
		public LinearLayout namespace;
	}

	boolean clicked[];
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.ledlayout, null, true);
			holder = new ViewHolder();
			holder.namespace=(LinearLayout) rowView.findViewById(R.id.namespace);
			holder.name = (TextView) rowView.findViewById(R.id.name);
			holder.brightness = (SeekBar) rowView.findViewById(R.id.brightness);
			holder.blink=(CheckBox) rowView.findViewById(R.id.blink);
			holder.capture=(CheckBox) rowView.findViewById(R.id.capture);
			holder.captureSize = (SeekBar) rowView.findViewById(R.id.captureSizeTo);
			holder.captureSize2 = (SeekBar) rowView.findViewById(R.id.captureSizeFrom);
			holder.trigger=(Button) rowView.findViewById(R.id.trigger);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		if(holders[position]==null){
			onclick Onclick=new onclick();
			Onclick.id=position;
			holder.namespace.setOnClickListener(Onclick);
			osbcl sbcl=new osbcl();
			sbcl.id=position;
			sbcl.context=context;
			holder.brightness.setMax(leds[position].maxbr);
			holder.brightness.setOnSeekBarChangeListener(sbcl);
			occl ccl=new occl();
			ccl.id=position;
			ccl.context=context;
			holder.blink.setOnCheckedChangeListener(ccl);
			occl2 ccl2=new occl2();
			ccl2.id=position;
			ccl2.context=context;
			holder.capture.setOnCheckedChangeListener(ccl2);
			osbcl2 sbcl2=new osbcl2();
			sbcl2.id=position;
			sbcl2.context=context;
			holder.captureSize.setMax(255*4);
			holder.captureSize.setOnSeekBarChangeListener(sbcl2);
			osbcl3 sbcl3=new osbcl3();
			sbcl3.id=position;
			sbcl3.context=context;
			holder.captureSize2.setMax(255*4);
			holder.captureSize2.setOnSeekBarChangeListener(sbcl3);
			if(leds[position].trigger!=null){
				ocl cl=new ocl();
				cl.context=context;
				cl.id=position;
			}
			holders[position]=holder;
		}
		update(position);
		return rowView;
	}
	class onclick implements OnClickListener{
		int id;
		@Override
		public void onClick(View v) {
			clicked[id]=!clicked[id];
			updateAll();
		}
	}
	public void updateAll(){
		notifyDataSetChanged();
	}
	public void update(int id){
		ViewHolder holder=holders[id];
		if(holder==null) return;
		holder.name.setText(leds[id].name);
		if(clicked[id]){
			holder.brightness.setProgress(leds[id].br);
			if(leds[id].blink!=null)
				holder.blink.setChecked(leds[id].bl);
			else
				holder.blink.setVisibility(View.GONE);
			holder.capture.setChecked(leds[id].usedMusic);
			holder.captureSize.setProgress(leds[id].musicValueTo+255*2);
			holder.captureSize2.setProgress(leds[id].musicValueFrom+255*2);

			holder.brightness.setVisibility(View.VISIBLE);
			holder.capture.setVisibility(View.VISIBLE);
			holder.captureSize.setVisibility(View.VISIBLE);
			holder.captureSize2.setVisibility(View.VISIBLE);
			if(leds[id].trigger==null)
				holder.trigger.setVisibility(View.INVISIBLE);
			else
				holder.trigger.setVisibility(View.VISIBLE);
		}else{
			holder.brightness.setVisibility(View.GONE);
			holder.capture.setVisibility(View.GONE);
			holder.captureSize.setVisibility(View.GONE);
			holder.captureSize2.setVisibility(View.GONE);
			holder.blink.setVisibility(View.GONE);
			holder.trigger.setVisibility(View.INVISIBLE);
		}
	}
	class osbcl implements OnSeekBarChangeListener{
		int id;
		MainActivity context;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			leds[id].setLight(seekBar.getProgress(), context);
			update(id);
		}
	}
	class occl implements OnCheckedChangeListener{
		int id;
		MainActivity context;
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			leds[id].setBlink(arg1, context);
			update(id);
		}
	}
	class occl2 implements OnCheckedChangeListener{
		int id;
		MainActivity context;
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			if(arg1)leds[id].startMusic();
			else leds[id].stopMusic(context);
			update(id);
		}
	}
	class osbcl2 implements OnSeekBarChangeListener{
		int id;
		MainActivity context;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			leds[id].musicValueTo=seekBar.getProgress()-255*2;
			update(id);
		}
	}
	class osbcl3 implements OnSeekBarChangeListener{
		int id;
		MainActivity context;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			leds[id].musicValueFrom=seekBar.getProgress()-255*2;
			update(id);
		}
	}
	class ocl implements OnClickListener{
		int id;
		MainActivity context;
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			context.idd=id;
			context.showDialog(id);
		}
	}
}


