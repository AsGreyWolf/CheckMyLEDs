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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
public class LEDAdapter extends ArrayAdapter<String> {
    private final MainActivity context;
    LED[] leds;
    public LEDAdapter(MainActivity context, LED[] leds,String[] str) {
        super(context, R.layout.ledlayout, str);
        this.context = context;
        this.leds= leds;
    }

    // Класс для сохранения во внешний класс и для ограничения доступа
    // из потомков класса
    static class ViewHolder {
        public TextView name;
        public CheckBox blink;
        public SeekBar brightness;

        public Button trigger;
        //public LinearLayout layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ViewHolder буферизирует оценку различных полей шаблона элемента
    	
        ViewHolder holder;
        // Очищает сущетсвующий шаблон, если параметр задан
        // Работает только если базовый шаблон для всех классов один и тот же
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.ledlayout, null, true);
            holder = new ViewHolder();
            holder.name = (TextView) rowView.findViewById(R.id.name);
            holder.brightness = (SeekBar) rowView.findViewById(R.id.brightness);
        	// TODO Auto-generated method stub
			osbcl sbcl=new osbcl();
            sbcl.led=leds[position];
            sbcl.context=context;
            holder.brightness.setOnSeekBarChangeListener(sbcl);
            holder.blink=(CheckBox) rowView.findViewById(R.id.blink);
            occl ccl=new occl();
            ccl.led=leds[position];
            ccl.context=context;
            holder.blink.setOnCheckedChangeListener(ccl);

            holder.trigger=(Button) rowView.findViewById(R.id.trigger);
            if(leds[position].trigger!=null){
            ocl cl=new ocl();
            cl.context=context;
            cl.id=position;
            holder.trigger.setOnClickListener(cl);
            }else{
            holder.trigger.setVisibility(View.INVISIBLE);	
            }
            
            
           rowView.setTag(holder);
            
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        leds[position].bl=holder.blink.isChecked();
        
        
        holder.name.setText(leds[position].name);
        holder.brightness.setProgress(leds[position].br);
        if(leds[position].blink!=null)
        	holder.blink.setChecked(leds[position].bl);
        else
        	holder.blink.setVisibility(View.INVISIBLE);
        return rowView;
    }
}
class osbcl implements OnSeekBarChangeListener{
	LED led;
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
		led.setLight(seekBar.getProgress(), context);
	}
}
class occl implements OnCheckedChangeListener{
	LED led;
	MainActivity context;
	@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			led.setBlink(arg1, context);
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
