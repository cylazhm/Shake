package yunleicheng.com.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;

public class AdminReceiver extends DeviceAdminReceiver {
	public static Intent getAdmin(ComponentName admin) {
        // Launch the activity to have the user enable our admin.
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
        		admin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"Thanks for using shakeOff.");
        return intent;
    }
}
