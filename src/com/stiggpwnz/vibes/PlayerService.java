package com.stiggpwnz.vibes;

import java.util.LinkedList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.jakewharton.notificationcompat2.NotificationCompat2;
import com.stiggpwnz.vibes.restapi.Song;

public class PlayerService extends Service {

	private static final String SONG = "song";
	private static final int NOTIFICATION = 49;
	private static final long IDLE_TIME = 10 * 60 * 1000;

	private final IBinder binder = new ServiceBinder();
	private final Handler handler = new Handler();
	private final Runnable serviceKiller = new Runnable() {

		@Override
		public void run() {
			Log.d(VibesApplication.VIBES, "killing service");
			stopSelf();
		}
	};

	private NotificationManager notificationManager;
	private Player player;
	private VibesApplication app;
	private List<Integer> downloadQueue;
	private WifiManager.WifiLock wifiLock;
	private PendingIntent intent;

	public class ServiceBinder extends Binder {

		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(VibesApplication.VIBES, "service created");
		app = (VibesApplication) getApplication();
		player = new Player(this, getHandler());
		wifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(SONG);
		wifiLock.acquire();
		app.setServiceRunning(true);
		intent = PendingIntent.getActivity(this, 0, new Intent(this, PlayerActivity.class), 0);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		app.setServiceRunning(false);
		cancelSongNotification();
		wifiLock.release();
		player.release();
		notificationManager = null;
		Log.d(VibesApplication.VIBES, "service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public Player getPlayer() {
		return player;
	}

	public NotificationManager getNotificationManager() {
		if (notificationManager == null)
			notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		return notificationManager;
	}

	public List<Integer> getDownloadQueue() {
		if (downloadQueue == null)
			downloadQueue = new LinkedList<Integer>();
		return downloadQueue;
	}

	public void download(Song song) {
		try {
			Settings settings = app.getSettings();
			new Downloader(this, app.getVkontakte(), getDownloadQueue(), settings.getDirectoryPath(), settings.getFinishedNotification()).download(song);
		} catch (Exception e) {
			onDownloadException(e.getLocalizedMessage());
		}
	}

	public void onDownloadException(String message) {
		if (message != null) {
			Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP, 0, -50);
			toast.show();
		}
	}

	public void showSongNotification() {
		if (player.getCurrentSong() != null) {
			CharSequence contentTitle = player.getCurrentSong().title;
			CharSequence contentText = player.getCurrentSong().performer;
			NotificationCompat2.Builder builder = new NotificationCompat2.Builder(this).setContentTitle(contentTitle).setContentText(contentText)
					.setSmallIcon(R.drawable.notification_icon).setContentIntent(intent);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				builder.setProgress(player.getSongDuration(), player.getCurrentPosition(), false);
			Notification notification = builder.build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			getNotificationManager().notify(SONG, NOTIFICATION, notification);
		}
	}

	public void cancelSongNotification() {
		getNotificationManager().cancel(SONG, NOTIFICATION);
	}

	public void startWaiter() {
		getHandler().removeCallbacks(serviceKiller);
		Log.d(VibesApplication.VIBES, "starting waiter");
		getHandler().postDelayed(serviceKiller, IDLE_TIME);
	}

	public void stopWaiter() {
		Log.d(VibesApplication.VIBES, "stopping waiter");
		getHandler().removeCallbacks(serviceKiller);
	}

	public Handler getHandler() {
		return handler;
	}

}
