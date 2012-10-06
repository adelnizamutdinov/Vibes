package com.stiggpwnz.vibes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.stiggpwnz.vibes.imageloader.ImageLoader;
import com.stiggpwnz.vibes.restapi.Album;
import com.stiggpwnz.vibes.restapi.LastFM;
import com.stiggpwnz.vibes.restapi.Playlist;
import com.stiggpwnz.vibes.restapi.Playlist.Type;
import com.stiggpwnz.vibes.restapi.Song;
import com.stiggpwnz.vibes.restapi.Unit;
import com.stiggpwnz.vibes.restapi.VKontakte;
import com.stiggpwnz.vibes.restapi.VKontakteException;

public class VibesApplication extends Application implements Settings.Listener {

	public static final String VIBES = "vibes";

	public static final int TIMEOUT_CONNECTION = 3000;
	public static final int TIMEOUT_SOCKET = 5000;

	// common system stuff
	private Settings settings;
	private AbstractHttpClient httpClient;
	private ImageLoader imageLoader;
	private boolean serviceRunning;

	// general gui objects
	private View loadingFooter;
	private Typeface typeface;
	private Animation shake;

	// web services
	private VKontakte vkontakte;
	private LastFM lastfm;

	// general player data
	private Playlist playlist;
	private Playlist selectedPlaylist;
	private ArrayList<Song> songs;

	// cached units
	private ArrayList<Unit> friends;
	private ArrayList<Unit> groups;
	private Unit self;

	// other cached stuff
	private Map<Playlist, ArrayList<Song>> playlistsCache;
	private Map<Song, String> albumImagesCache;

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		settings = null;
		httpClient = null;
		imageLoader = null;

		vkontakte = null;
		lastfm = null;

		playlist = null;
		selectedPlaylist = null;
		songs = null;

		friends = null;
		groups = null;
		self = null;

		playlistsCache = null;
		albumImagesCache = null;

		loadingFooter = null;
		typeface = null;
		shake = null;
	}

	public ArrayList<Song> loadSongs(Playlist playlist) throws IOException, VKontakteException {
		int ownerId = playlist.unit != null ? playlist.unit.id : 0;
		int albumId = playlist.album != null ? playlist.album.id : 0;

		ArrayList<Song> songs = null;
		switch (playlist.type) {
		case AUDIOS:
			songs = getVkontakte().getAudios(ownerId, albumId, 0);
			break;

		case WALL:
			songs = getVkontakte().getWallAudios(ownerId, 0, false);
			break;

		case NEWSFEED:
			songs = getVkontakte().getNewsFeedAudios(0, 0);
			break;

		case SEARCH:
			songs = getVkontakte().search(playlist.query, 0);
			break;
		}

		getPlaylistsCache().put(playlist, songs);
		return songs;
	}

	public ArrayList<Album> loadAlbums(int id) throws ClientProtocolException, IOException, VKontakteException {
		return getVkontakte().getAlbums(id, 0);
	}

	public ArrayList<Unit> loadFriends() throws ClientProtocolException, IOException, VKontakteException {
		friends = getVkontakte().getFriends(false);
		return friends;
	}

	public ArrayList<Unit> loadGroups() throws ClientProtocolException, IOException, VKontakteException {
		groups = getVkontakte().getGroups();
		return groups;
	}

	@Override
	public void onLastFmSessionChanged(String session) {
		getLastFM().setSession(session);
	}

	@Override
	public void onVkontakteAccessTokenChanged(int userId, String accessToken) {
		getVkontakte().setUserId(userId);
		getVkontakte().setAccessToken(accessToken);
		
		self = null;
		setPlaylist(new Playlist(Type.NEWSFEED, null, getSelf()));
		setSelectedPlaylist(getPlaylist());
		getPlaylistsCache().clear();
		getAlbumImagesCache().clear();
		songs = null;
	}

	@Override
	public void onVkontakteMaxAudiosChanged(int maxAudios) {
		getVkontakte().maxAudios = maxAudios;
	}

	@Override
	public void onVkontakteMaxNewsChanged(int maxNews) {
		getVkontakte().maxNews = maxNews;
	}

	public Unit getSelf() {
		if (self == null) {
			self = new Unit(0, null, null);
			new Thread("Loading self") {

				@Override
				public void run() {
					try {
						self.initWith(getVkontakte().getSelf());
					} catch (Exception e) {

					}
				}
			}.start();
		}
		return self;
	}

	private static AbstractHttpClient createNewThreadSafeHttpClient() {
		// create a new simple client
		AbstractHttpClient defaultHttpClient = new DefaultHttpClient();

		// set timeout
		HttpParams params = defaultHttpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);

		// create a threadsafe manager
		SchemeRegistry registry = defaultHttpClient.getConnectionManager().getSchemeRegistry();
		ClientConnectionManager manager = new ThreadSafeClientConnManager(params, registry);

		// create a new threadsafe client
		return new DefaultHttpClient(manager, params);
	}

	public ImageLoader getImageLoader() {
		if (imageLoader == null)
			imageLoader = new ImageLoader(this, R.drawable.music);
		return imageLoader;
	}

	public ArrayList<Unit> getFriends() {
		return friends;
	}

	public ArrayList<Unit> getGroups() {
		return groups;
	}

	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	public LastFM getLastFM() {
		if (lastfm == null) {
			int density = getResources().getDisplayMetrics().densityDpi;
			lastfm = new LastFM(getHttpClient(), getSettings().getSession(), density);
		}
		return lastfm;
	}

	public Map<Playlist, ArrayList<Song>> getPlaylistsCache() {
		if (playlistsCache == null)
			playlistsCache = new HashMap<Playlist, ArrayList<Song>>();
		return playlistsCache;
	}

	public ArrayList<Song> getSongs() {
		return songs;
	}

	public Playlist getPlaylist() {
		if (playlist == null)
			playlist = new Playlist(Type.NEWSFEED, null, getSelf());
		return playlist;
	}

	public void setPlaylist(Playlist playlist) {
		this.playlist = playlist;
		songs = getPlaylistsCache().get(playlist);
	}

	public Playlist getSelectedPlaylist() {
		if (selectedPlaylist == null)
			selectedPlaylist = getPlaylist();
		return selectedPlaylist;
	}

	public void setSelectedPlaylist(Playlist selected) {
		this.selectedPlaylist = selected;
	}

	public Map<Song, String> getAlbumImagesCache() {
		if (albumImagesCache == null)
			albumImagesCache = new HashMap<Song, String>();
		return albumImagesCache;
	}

	private AbstractHttpClient getHttpClient() {
		if (httpClient == null)
			httpClient = createNewThreadSafeHttpClient();
		return httpClient;
	}

	public Settings getSettings() {
		if (settings == null)
			settings = new Settings(this, this);
		return settings;
	}

	public VKontakte getVkontakte() {
		if (vkontakte == null) {
			Settings settings = getSettings();
			vkontakte = new VKontakte(settings.getAccessToken(), getHttpClient(), settings.getUserID(), settings.getMaxNews(), settings.getMaxAudio());
		}
		return vkontakte;
	}

	public Typeface getTypeface() {
		if (typeface == null)
			typeface = Typeface.createFromAsset(getAssets(), "SegoeWP-Semilight.ttf");
		return typeface;
	}

	public View getFooter() {
		if (loadingFooter == null)
			loadingFooter = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer, null);
		return loadingFooter;
	}

	public Animation getShakeAnimation() {
		if (shake == null)
			shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		return shake;
	}

}
