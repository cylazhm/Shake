package yunleicheng.com;

import yunleicheng.com.consts.Consts;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

public class LockScreen extends DeviceAdminReceiver{
	//This is a comment

	public static class Controller extends Activity{

		DevicePolicyManager mDPM;
		ComponentName mDeviceAdminSample;
		PowerManager pm;
		PowerManager.WakeLock powerWakeLock;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			
			//LockScreen ¼Ì³Ð×Ô DeviceAdminReceiver
			mDeviceAdminSample = new ComponentName(Controller.this,
					LockScreen.class);
			boolean active = mDPM.isAdminActive(mDeviceAdminSample);
			if (!active)	getAdmin();
			else mDPM.lockNow();
			Controller.this.finish();
//			android.os.Process.killProcess(android.os.Process.myPid()); 
		}

        public void getAdmin() {
            // Launch the activity to have the user enable our admin.
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mDeviceAdminSample);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Thank you for using ShakeOnOff");
            startActivityForResult(intent, Consts.RESULT_ENABLE);
        }
		
	}
}
