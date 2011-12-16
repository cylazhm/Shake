package yunleicheng.com;

import com.waps.AppConnect;
import com.waps.UpdatePointsNotifier;

import yunleicheng.com.consts.Consts;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.widget.Toast;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class ScreenService extends Service implements SensorEventListener, UpdatePointsNotifier{
	
	private SensorManager sensorMgr;
	private Sensor accSens;
	private PowerManager pm;
	private PowerManager.WakeLock powerWakeLock;
	private PowerManager.WakeLock keepRunning;
	Handler mHandler = new Handler();
	
	private static Point leftPoint;
	private static Point rightPoint;
	private int defaultTimeout;
	private static int pushads;
	private static int totalPoints;
	
	//Preference parameters
	private SharedPreferences settings;
	private boolean awake;
	private int threshold;
	private int direction;
	private int noaction;
	private int speed;
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		//obtain the preference settings
		//Preference parameters
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		final int[] intervalMap = {1000000,2000000,3000000,4000000};//No actions - 0.1,0.2,0.3,0.4 seconds
		final int[] ThresholdMap = {12,8,4};//amplitude of shake
		final int[] speedMap = {80000000,90000000,150000000,600000000};//speed of shake - 0.1,0.2,0.3,0.4 seconds
		
		awake = settings.getBoolean(Consts.WL, false);
		threshold = ThresholdMap[settings.getInt(Consts.AMPL, 1)];
		direction = settings.getInt(Consts.DIRECT, 0);
		noaction = intervalMap[settings.getInt(Consts.INTERVAL, 0)];
		speed = speedMap[settings.getInt(Consts.SPEED, 3)];
		
		AppConnect.getInstance(this).getPoints(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		try {
			defaultTimeout = android.provider.Settings.System.getInt(getContentResolver(),SCREEN_OFF_TIMEOUT);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		
		leftPoint = new Point();
        rightPoint = new Point();
        
        sensorMgr = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSens = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        boolean support = sensorMgr.registerListener(this, accSens, SensorManager.SENSOR_DELAY_UI);
        if(!support)	Toast.makeText(ScreenService.this, this.getResources().getText(R.string.notSupport), Toast.LENGTH_SHORT).show();
        
        if(awake){
	        keepRunning = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Screen OnOff");
	        keepRunning.acquire();
	        Notification note=new Notification(R.drawable.icon,
                    "ShakeOnOff running",
                    System.currentTimeMillis());
	        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, 
	                new Intent(getApplicationContext(), Main.class), 
	                PendingIntent.FLAG_UPDATE_CURRENT); 
	        note.setLatestEventInfo(getApplicationContext(), "ShakeOnOff", 
	                this.getResources().getString(R.string.serviceHint), pi);
	        startForeground(1080, note);
        }
	}
	
	@Override
	public int onStartCommand(Intent intent,
	 int flags, int startId) {
	return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(null!=powerWakeLock&&defaultTimeout<=0) powerWakeLock.release();
		if(null!=keepRunning)	keepRunning.release();
		sensorMgr.unregisterListener(this, accSens);
		
		boolean k = settings.getBoolean(Consts.KILLSENSOR, false);
		
		if(k){
			this.stopForeground(true);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float x = event.values[0];
		float time = event.timestamp;
		if(x>threshold||x<-threshold){//check left point and right point to determine if we need to lock screen
			if(leftPoint.getX()>threshold&&rightPoint.getX()<-threshold){
				if(direction==0){
					if(rightPoint.getTimeStamp()>leftPoint.getTimeStamp())	{
						if(event.timestamp-Consts.TIMESTAMP>noaction*1000){
							if(pm.isScreenOn()){
									Intent callIntent = new Intent(Intent.ACTION_CALL);  
									callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
									callIntent.setClass(ScreenService.this,LockScreen.Controller.class);
									Consts.TIMESTAMP = event.timestamp;
									startActivity(callIntent);
							}else{
								powerWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "ScreenOn");
								if(defaultTimeout>0){
									powerWakeLock.acquire(defaultTimeout);
								}else{
									powerWakeLock.acquire();
								}
								Consts.TIMESTAMP = event.timestamp;
							}
							if(pushads++>Consts.PUSHADS&&settings.getBoolean(Consts.SHOWADS, true)){//check if total points earned
								AppConnect.getInstance(this).setPushIcon(R.drawable.icon);
								AppConnect.getInstance(this).getPushAd(); 
								pushads=0;
							}
						}
					}
				}
				else if(direction==1){
					if(rightPoint.getTimeStamp()<leftPoint.getTimeStamp()){
						if(event.timestamp-Consts.TIMESTAMP>noaction*1000)
							if(pm.isScreenOn()){
									Intent callIntent = new Intent(Intent.ACTION_CALL);  
									callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
									callIntent.setClass(ScreenService.this,LockScreen.Controller.class);
									Consts.TIMESTAMP = event.timestamp;
									startActivity(callIntent);
							}else{
								powerWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "ScreenOn");
								if(defaultTimeout>0){
									powerWakeLock.acquire(defaultTimeout);
								}else{
									powerWakeLock.acquire();
								}
								Consts.TIMESTAMP = event.timestamp;
							}
					}
				}
				else if(direction==3){
					
				}
				else if(direction==4){
					
				}
			}
		}
		
		if(x>threshold){
			leftPoint.setX(x);
			leftPoint.setTimeStamp(time);
		}else if(x<-threshold){
			rightPoint.setX(x);
			rightPoint.setTimeStamp(time);
		}
	
		if((leftPoint.getX()>threshold)&&(event.timestamp-leftPoint.getTimeStamp())>speed){
			leftPoint.setTimeStamp(event.timestamp);
			leftPoint.setX(0);
		}
		
		if((rightPoint.getX()<-threshold)&&(event.timestamp-rightPoint.getTimeStamp())>speed){
			rightPoint.setTimeStamp(event.timestamp);
			rightPoint.setX(0);
		}
	}
	class Point{
		private float x;
		private float timestamp;
		public void setX(float x){
			this.x = x;
		}
		public float getX(){
			return this.x;
		}
		public void setTimeStamp(float x){
			this.timestamp = x;
		}
		public float getTimeStamp(){
			return this.timestamp;
		}
	}
	@Override
	public void getUpdatePoints(String arg0, int arg1) {
		// TODO Auto-generated method stub
		ScreenService.totalPoints = arg1;
		mHandler.post(mUpdateResults);
	}

	@Override
	public void getUpdatePointsFailed(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
	Runnable mUpdateResults = new Runnable() {
        public void run() {
        	SharedPreferences.Editor pointsEditor = settings.edit();
        	pointsEditor.putInt(Consts.POINTSAVED, ScreenService.totalPoints);
        	pointsEditor.commit();
        }
    };

}
