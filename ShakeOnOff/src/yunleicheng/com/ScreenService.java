package yunleicheng.com;

import yunleicheng.com.consts.Consts;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings.SettingNotFoundException;
import android.widget.Toast;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

public class ScreenService extends Service implements SensorEventListener{
	
	private SensorManager sensorMgr;
	private Sensor accSens;
	private PowerManager pm;
	private PowerManager.WakeLock powerWakeLock;
	private PowerManager.WakeLock keepRunning;
	
	private Point leftPoint;
	private Point rightPoint;
	private int defaultTimeout;
//	private static int pushads;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
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
        
        if(Consts.AWAKE){
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
		if(Consts.KILLSENSER){
			sensorMgr.unregisterListener(this, accSens);
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
		if(x>Consts.THRESHOLD||x<Consts.THRESHOLD){//check left point and right point to determine if we need to lock screen
			if(leftPoint.getX()>Consts.THRESHOLD&&rightPoint.getX()<-Consts.THRESHOLD){
				if(Consts.DIRECTION==0){
					if(rightPoint.getTimeStamp()>leftPoint.getTimeStamp())	{
						if(event.timestamp-Consts.TIMESTAMP>Consts.NOACTION*1000){
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
/*							if(pushads++>Consts.PUSHADS){
								AppConnect.getInstance(this).setPushIcon(R.drawable.icon);
								AppConnect.getInstance(this).getPushAd(); 
								pushads=0;
							}*/
						}
					}
				}
				else if(Consts.DIRECTION==1){
					if(rightPoint.getTimeStamp()<leftPoint.getTimeStamp()){
						if(event.timestamp-Consts.TIMESTAMP>Consts.NOACTION*1000)
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
				else if(Consts.DIRECTION==3){
					
				}
				else if(Consts.DIRECTION==4){
					
				}
			}
		}
		
		if(x>Consts.THRESHOLD){
			leftPoint.setX(x);
			leftPoint.setTimeStamp(time);
		}else if(x<-Consts.THRESHOLD){
			rightPoint.setX(x);
			rightPoint.setTimeStamp(time);
		}

	
		if((leftPoint.getX()>Consts.THRESHOLD)&&(event.timestamp-leftPoint.getTimeStamp())>Consts.INTERVAL){
			leftPoint.setTimeStamp(event.timestamp);
			leftPoint.setX(0);
		}
		
		if((rightPoint.getX()<-Consts.THRESHOLD)&&(event.timestamp-rightPoint.getTimeStamp())>Consts.INTERVAL){
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

}
