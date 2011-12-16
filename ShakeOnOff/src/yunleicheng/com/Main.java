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
import android.preference.PreferenceManager;
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
		menu.add(0,3,3,R.string.menu2);
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
		else if(item.getItemId()==3){//remove Ads
			if(!settings.getBoolean(Consts.SHOWADS, true)){
				Toast.makeText(this, R.string.adRemovedAlready, Toast.LENGTH_SHORT).show();
			}else{
				int points = settings.getInt(Consts.POINTSAVED, 0);
				System.out.println("points = "+points);
				if(points<Consts.ADSPOINTS){
					Dialog dlg = new AlertDialog.Builder(Main.this)
					.setMessage("您现有"+points+"积分,去除广告需要"+Consts.ADSPOINTS+"积分。请下载安装推荐应用获得更多积分。")
					.setPositiveButton("确定", new DialogInterface.OnClickListener(){
	
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dialog.cancel();
							AppConnect.getInstance(Main.this).showOffers(Main.this);
						}
					}).create();
					dlg.show();
				}else{
					AppConnect.getInstance(this).spendPoints(Consts.ADSPOINTS, this);
					SharedPreferences.Editor Editor = settings.edit();
					Editor.putInt(Consts.POINTSAVED, points-Consts.ADSPOINTS);
					Editor.putBoolean(Consts.SHOWADS, false);
					Editor.commit();
					Toast.makeText(this, R.string.adRemoved, Toast.LENGTH_SHORT).show();
				}
			}
		}else{
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
		
		final String[] amplItems = getResources().getStringArray(R.array.shake_amp);
		setSpinner(ampl,amplItems);
		
		final String[] speedItems = getResources().getStringArray(R.array.shake_speed);
		setSpinner(speed,speedItems);
		
		final String[] directionItems = getResources().getStringArray(R.array.shake_direction);
		setSpinner(direction,directionItems);
		
		final String[] intervalItems = getResources().getStringArray(R.array.shake_interval);
		setSpinner(interval,intervalItems);
		
		//Load parameters from preferences
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		ampl.setSelection(settings.getInt(Consts.AMPL, 1), false);
		
		
		speed.setSelection(settings.getInt(Consts.SPEED, 3), false);
		
		direction.setSelection(settings.getInt(Consts.DIRECT, 0), false);
		
		interval.setSelection(settings.getInt(Consts.INTERVAL, 0), false);
		
		wl.setChecked(settings.getBoolean(Consts.WL, false));
		
		//Get accumulated points
		myPoints = settings.getInt(Consts.POINTSAVED,0);
		
		ampl.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				SharedPreferences.Editor amplEditor = settings.edit();
				amplEditor.putInt(Consts.AMPL, arg2);
				amplEditor.commit();
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
				speedEditor.putInt(Consts.SPEED, arg2);
				speedEditor.commit();
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
				directionEditor.putInt(Consts.DIRECT, arg2);
				directionEditor.commit();
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
				intervalEditor.putInt(Consts.INTERVAL, arg2);
				intervalEditor.commit();
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
				wlEditor.putBoolean(Consts.WL, isChecked);
				wlEditor.commit();
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
				SharedPreferences.Editor wlEditor = settings.edit();
				wlEditor.putBoolean(Consts.KILLSENSOR, true);
				wlEditor.commit();
				Main.this.stopService(new Intent(Main.this, ScreenService.class));
				Toast.makeText(Main.this, R.string.stop_hint, Toast.LENGTH_SHORT).show();
			}
		});
		AppConnect.getInstance(this).getPoints(this);//Get ads points
		
		if(settings.getBoolean(Consts.SHOWADS, true)){
			LinearLayout container =(LinearLayout)findViewById(R.id.adView);
			new AdView(Main.this,container).DisplayAd(20);
		}
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
		myPoints = arg1;
		mHandler.post(mUpdateResults);
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub
		System.out.println("Failed points = "+myPoints);
	}
	
    Runnable mUpdateResults = new Runnable() {
        public void run() {
        	SharedPreferences.Editor pointsEditor = settings.edit();
        	pointsEditor.putInt(Consts.POINTSAVED, myPoints);
        	pointsEditor.commit();
        }
    };
}