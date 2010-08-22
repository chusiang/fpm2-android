package org.braiden.fpm2;

import java.util.Collections;
import java.util.List;

import org.braiden.fpm2.model.PasswordItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FpmApplication extends Application {

	public static final String ACTION_FPM_OPEN = "org.braiden.fpm2.FPM_OPEN";
	public static final String ACTION_FPM_CLOSE = "org.braiden.fpm2.FPM_CLOSE";
	public static final long FPM_AUTO_LOCK_MILLISECONDS = 10L * 1000L;
	
	private static final String TAG = "FpmApplication";
	private FpmCrypt fpmCrypt;

	@Override
	public void onCreate() {
		super.onCreate();
		fpmCrypt = new FpmCrypt();
		BroadcastReceiver autoCloseReceiver = new AutoCloseFpmBroadcastReceiver(this, fpmCrypt);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_FPM_CLOSE);
		filter.addAction(ACTION_FPM_OPEN);
		registerReceiver(autoCloseReceiver, filter);
	}
	
	public void openCrypt(final Activity activity) {
		if (!isCryptOpen()) {
			LayoutInflater factory = LayoutInflater.from(activity);
			final View textEntryView = factory.inflate(R.layout.passphrase_dialog, null);
			final EditText editText = (EditText) textEntryView.findViewById(R.id.password_edit);
			final Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			new AlertDialog.Builder(activity)
            	//.setIcon(R.drawable.passphrase_dialog_icon)
            	.setTitle(R.string.passphrase_dialog_title)
            	.setView(textEntryView)
            	.setCancelable(false)
            	.setPositiveButton(R.string.passphrase_dialog_ok, new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int whichButton) {
            			try {
							FpmApplication.this.fpmCrypt.open(
									activity.getAssets().open("fpm.xml"),
									editText.getText().toString());
							activity.sendBroadcast(new Intent(ACTION_FPM_OPEN));
						} catch (Exception e) {
							Log.w(TAG, "Failed to unlock FPM.", e);
							vib.vibrate(250L);
							openCrypt(activity);
						}
            		}
            	})
            	.setNegativeButton(R.string.passphrase_dialog_cancel, new DialogInterface.OnClickListener() {
            		@Override
            		public void onClick(DialogInterface dialog, int which) {
            			activity.finish();
            		}
            	})
            	.create()
            	.show();
		}		
	}
	
	public void closeCrypt() {
		fpmCrypt.close();
	}
	
	public boolean isCryptOpen() {
		return fpmCrypt.isOpen();
	}

	public List<PasswordItem> getPasswordItems() {
		if (isCryptOpen()) {
			return fpmCrypt.getFpmFile().getPasswordItems();
		} else {
			return Collections.emptyList();
		}
	}
	
	public PasswordItem getPasswordItemById(long id) {
		List<PasswordItem> items = getPasswordItems();
		if (id >= items.size()) {
			return null;
		} else {
			return items.get((int) id);
		}
	}
	
	public static class AutoCloseFpmBroadcastReceiver extends BroadcastReceiver {

		Thread autoCloseThread = null;
		FpmCrypt fpmCrypt;
		Context ctx;
		
		public AutoCloseFpmBroadcastReceiver(Context ctx, FpmCrypt crypt) {
			this.fpmCrypt = crypt;
			this.ctx = ctx;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (ACTION_FPM_OPEN.equals(intent.getAction())) {
				if (autoCloseThread == null || !autoCloseThread.isAlive()) {
					autoCloseThread = new Thread() {
						@Override
						public void run() {
							try {
								Thread.sleep(FPM_AUTO_LOCK_MILLISECONDS);
								fpmCrypt.close();
								Intent intent = new Intent(ACTION_FPM_CLOSE);
								ctx.sendBroadcast(intent);
							} catch (InterruptedException e) {
								
							}
						}
					};
					autoCloseThread.start();
				}
			} else if (ACTION_FPM_CLOSE.equals(intent.getAction())) {
				if (autoCloseThread != null && autoCloseThread.isAlive()) {
					autoCloseThread.interrupt();
					autoCloseThread = null;
				}
			}
		}
		
	}
	
}