package yunleicheng.com;

import yunleicheng.com.consts.Consts;
import net.youmi.android.AdManager;
import net.youmi.android.AdView;
import net.youmi.android.appoffers.YoumiOffersManager;
import net.youmi.android.appoffers.YoumiPointsManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.RelativeLayout.LayoutParams;
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
	
	boolean adsRemoved = false;
	
	Button startService = null;
	Button stopService = null;
	LinearLayout layout = null;
	
	static{
		AdManager.init(Consts.YOUMI_ID, Consts.YOUMI_PASS, 30, false);
	}
	
	//Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0,1,1,R.string.menu0);
		menu.add(0,2,2,R.string.menu1);
		menu.add(0,3,3,R.string.menu2);
		return super.onCreateOptionsMenu(menu);
	}
	
	//Create menu click listener
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		int title = 0;
		int message = 0;
		int button = 0;
		if(item.getItemId()==1){
			title = R.string.uninstall_title;
			message = R.string.uninstall_message;
			button = R.string.uninstall_button;
		}
		
		else if(item.getItemId()==2){
			title = R.string.power_title;
			message = R.string.power_message;
			button = R.string.power_button;
		}else{
			if(adsRemoved){
				title = R.string.adsRemovedTitle;
				message = R.string.adsRemoved;
				button = R.string.power_button;
			}else{
				try {
					// 查询积分示例
					int points = YoumiPointsManager
							.queryPoints(this);
					System.out.println("----------------------------"+points);
					if(points>=Consts.REMOVE_AD_POINTS){
						YoumiPointsManager.spendPoints(Main.this,
								Consts.REMOVE_AD_POINTS);
						SharedPreferences.Editor amplEditor = settings.edit();
						amplEditor.putBoolean("removeAds", true);
						amplEditor.commit();
						adsRemoved = true;
						if(null!=layout){
							layout.removeAllViews();
							
						}
					}else{
						title = R.string.pointsTitle;
						message = R.string.getMorePoint;
						button = R.string.power_button;
					}
	
				} catch (Exception e) {
					Toast.makeText(this, R.string.failGetPoints, Toast.LENGTH_SHORT).show();
				}
			}
		}
		
		Dialog dlg = new AlertDialog.Builder(Main.this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton(button, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.cancel();
				if(item.getItemId()==3&&!adsRemoved){
					YoumiOffersManager.showOffers(Main.this,
							YoumiOffersManager.TYPE_REWARD_OFFERS);
				}
			}
		}).create();
		dlg.show();
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		YoumiOffersManager.init(Main.this,Consts.YOUMI_ID,Consts.YOUMI_PASS);
		
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
		
		adsRemoved = settings.getBoolean("removeAds", false);
		
		if(!adsRemoved){
			//Add YOUMI ads on the top
			AdView adView = new AdView(this);
			layout = (LinearLayout)findViewById(R.id.adView);
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			layout.addView(adView, params);
		}
		
		
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