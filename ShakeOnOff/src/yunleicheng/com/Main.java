package yunleicheng.com;

import yunleicheng.com.consts.Consts;
import net.youmi.android.AdManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Main extends Activity {
	Spinner ampl = null;
	Spinner speed = null;
	Spinner direction = null;
	Spinner interval = null;
	ToggleButton wl = null;
	SharedPreferences settings = null;
	
	Button startService = null;
	Button stopService = null;
	
	static{
		AdManager.init("a8b65eb7305a6bbc", "4ef84ff66cd340f7", 30, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ampl = (Spinner)findViewById(R.id.amplitude);
		speed = (Spinner)findViewById(R.id.speed);
		direction = (Spinner)findViewById(R.id.direction);
		interval = (Spinner)findViewById(R.id.interval);
		wl = (ToggleButton)findViewById(R.id.wakelock);
		startService = (Button)findViewById(R.id.start);
		stopService = (Button)findViewById(R.id.stop);
		Consts.KILLSENSER = false;
		
		final String[] amplItems = getResources().getStringArray(R.array.shake_amp);
		setSpinner(ampl,amplItems);
		
		final String[] speedItems = getResources().getStringArray(R.array.shake_speed);
		setSpinner(speed,speedItems);
		
		final String[] directionItems = getResources().getStringArray(R.array.shake_direction);
		setSpinner(direction,directionItems);
		
		final String[] intervalItems = getResources().getStringArray(R.array.shake_interval);
		setSpinner(interval,intervalItems);
		
		settings = this.getPreferences(MODE_PRIVATE);
		
		ampl.setSelection(settings.getInt("ampl", 1), false);
		final int[] ThresholdMap = {12,8,4};//amplitude of shake
		Consts.THRESHOLD = ThresholdMap[settings.getInt("ampl",1)];
		
		speed.setSelection(settings.getInt("speed", 3), false);
		final int[] speedMap = {80000000,90000000,150000000,600000000};//speed of shake - 0.1,0.2,0.3,0.4 seconds
		Consts.INTERVAL = speedMap[settings.getInt("speed",3)];
		
		direction.setSelection(settings.getInt("direction", 0), false);
		Consts.DIRECTION = settings.getInt("direciton", 0);
		
		interval.setSelection(settings.getInt("interval", 0), false);
		final int[] intervalMap = {1000000,2000000,3000000,4000000};//speed of shake - 0.1,0.2,0.3,0.4 seconds
		Consts.NOACTION = intervalMap[settings.getInt("interval", 1)];
		
		wl.setChecked(settings.getBoolean("wl", false));
		Consts.AWAKE = settings.getBoolean("wl", false);
		
		
		ampl.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SharedPreferences.Editor amplEditor = settings.edit();
				amplEditor.putInt("ampl", arg2);
				amplEditor.commit();
				Consts.THRESHOLD = ThresholdMap[settings.getInt("ampl",1)];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		speed.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SharedPreferences.Editor speedEditor = settings.edit();
				speedEditor.putInt("speed", arg2);
				speedEditor.commit();
				Consts.INTERVAL = speedMap[settings.getInt("speed",3)];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		direction.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SharedPreferences.Editor directionEditor = settings.edit();
				directionEditor.putInt("direction", arg2);
				directionEditor.commit();
				Consts.DIRECTION = settings.getInt("direciton", 0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		interval.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SharedPreferences.Editor intervalEditor = settings.edit();
				intervalEditor.putInt("interval", arg2);
				intervalEditor.commit();
				Consts.NOACTION = intervalMap[settings.getInt("interval", 1)];
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		wl.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences.Editor wlEditor = settings.edit();
				wlEditor.putBoolean("wl", isChecked);
				wlEditor.commit();
				Consts.AWAKE = settings.getBoolean("wl", false);
			}
			
		});

		startService.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				startService(new Intent(Main.this, ScreenService.class));
				Toast.makeText(Main.this, R.string.start_hint, Toast.LENGTH_SHORT).show();
				Main.this.finish();
			}
		});
		stopService.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Consts.KILLSENSER = true;
				Main.this.stopService(new Intent(Main.this, ScreenService.class));
				Toast.makeText(Main.this, R.string.stop_hint, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	private void setSpinner(Spinner s, String[] items){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// stopService(new Intent(this, ScreenService.class));
		// if(null!=powerWakeLock) powerWakeLock.release();
	}
}