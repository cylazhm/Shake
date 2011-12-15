package yunleicheng.com;

import com.waps.AdView;
import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

import yunleicheng.com.consts.Consts;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Main extends Activity implements UpdatePointsNotifier {
	Spinner ampl = null;
	Spinner speed = null;
	Spinner direction = null;
	Spinner interval = null;
	ToggleButton wl = null;
	SharedPreferences settings = null;
	
	Button startService = null;
	Button stopService = null;
	
	Handler mHandler = new Handler();
	int myPoints;
	
	//Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,1,1,R.string.menu0);
		menu.add(0,2,2,R.string.menu1);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Create menu click listener
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int title = 0;
		int message = 0;
		int button = 0;
		if(item.getItemId()==1){
			title = R.string.uninstall_title;
			message = R.string.uninstall_message;
			button = R.string.uninstall_button;
			Dialog dlg = new AlertDialog.Builder(Main.this)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(button, new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					dialog.cancel();
				}
			}).create();
			dlg.show();
		}
		
		else{
			AppConnect.getInstance(this).showOffers(this);
		}

		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		AppConnect.getInstance(this);
		
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
		
		//Get accumulated points
		myPoints = settings.getInt("points",0);
		
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
				if(isChecked){
					Dialog dlg = new AlertDialog.Builder(Main.this)
					.setTitle(R.string.power_title)
					.setMessage(R.string.power_message)
					.setPositiveButton(R.string.power_button, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.cancel();
						}
					}).create();
					dlg.show();
				}
			}
		});

		startService.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Main.this.stopService(new Intent(Main.this, ScreenService.class));//Stop the service first
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
		AppConnect.getInstance(this).getPoints(this);//Get ads points
	}
	
	private void setSpinner(Spinner s, String[] items){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppConnect.getInstance(this).finalize();
	}

	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		if(arg1<Consts.POINTS){
			myPoints = arg1;
			mHandler.post(mUpdateResults);
		}
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("Failed points = "+myPoints);
	}
	
    Runnable mUpdateResults = new Runnable() {
        public void run() {
        	SharedPreferences.Editor pointsEditor = settings.edit();
        	pointsEditor.putInt("points", myPoints);
        	pointsEditor.commit();
        	System.out.println("points = "+myPoints);
        	if(myPoints<Consts.POINTS){
        		LinearLayout container =(LinearLayout)findViewById(R.id.adView);
        		new AdView(Main.this,container).DisplayAd(20);
        	}
        }
    };
}