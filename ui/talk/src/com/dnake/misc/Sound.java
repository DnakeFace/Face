package com.dnake.misc;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Sound {
	public static String ringing = "/dnake/bin/ringtone/ring1.wav";
	public static String ringback = "/dnake/bin/ringtone/ringback.wav";

	public static String modify_success = "/dnake/bin/prompt/modify_success.wav";
	public static String modify_failed = "/dnake/bin/prompt/modify_failed.wav";
	public static String passwd_err = "/dnake/bin/prompt/passwd_err.wav";
	public static String press = "/dnake/bin/prompt/press.wav";
	public static String call_err = "/dnake/bin/prompt/call_err.wav";
	public static String card_err = "/dnake/bin/prompt/card_err.wav";
	public static String unlock = "/dnake/bin/prompt/unlock.wav";

	public static String sdt_success = "/dnake/bin/prompt/sdt_success.wav";
	public static String sdt_failed = "/dnake/bin/prompt/sdt_failed.wav";
	public static String sdt_invalid = "/dnake/bin/prompt/sdt_invalid.wav";

	public static String[] key = new String[10];
	public static boolean key_0_9 = false;

	public static void load() {
		if (!Locale.getDefault().getCountry().equals("CN")) {
			modify_success = "/dnake/bin/prompt/en/modify_success.wav";
			modify_failed = "/dnake/bin/prompt/en/modify_failed.wav";
			passwd_err = "/dnake/bin/prompt/en/passwd_err.wav";
			unlock = "/dnake/bin/prompt/en/unlock.wav";
			for(int i=0; i<10; i++) {
				key[i] = "/dnake/bin/prompt/press.wav";
			}
		} else {
			modify_success = "/dnake/bin/prompt/modify_success.wav";
			modify_failed = "/dnake/bin/prompt/modify_failed.wav";
			passwd_err = "/dnake/bin/prompt/passwd_err.wav";
			unlock = "/dnake/bin/prompt/unlock.wav";
			for(int i=0; i<10; i++) {
				key[i] = "/dnake/bin/prompt/key/"+i+".wav";
			}
		}
	}

	public static MediaPlayer play(String url, Boolean looping) {
		return play(url, looping, null);
	}

	public static MediaPlayer play(String url, Boolean looping, OnCompletionListener listener) {
		MediaPlayer mp = new MediaPlayer();
		try {
			mp.setDataSource(url);
			mp.prepare();
			mp.setLooping(looping);
			mp.start();

			if (listener != null)
				mp.setOnCompletionListener(listener);
			else {
				mp.setOnCompletionListener(new OnCompletionListener() {
					public void onCompletion(MediaPlayer p) {
						p.reset();
						p.release();
					}
				});
			}
			return mp;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mp != null) {
			mp.stop();
			mp.release();
		}
		return null;
	}

	public static void stop(MediaPlayer mp) {
		if (mp != null) {
			mp.stop();
			mp.release();
		}
	}

	public static int volume() {
		AudioManager m = (AudioManager) SysTalk.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (m != null) {
			int max = m.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int val = m.getStreamVolume(AudioManager.STREAM_MUSIC);
			float v = ((float)val/(float)max)*5.0f;
			return (int)v;
		}
		return 5;
	}

	public static void volume(int val) {
		AudioManager m = (AudioManager) SysTalk.mContext.getSystemService(Context.AUDIO_SERVICE);
		if (m != null) {
			int max = m.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float vol = ((float)val/5.0f)*max;
			m.setStreamVolume(AudioManager.STREAM_SYSTEM, (int)vol, 0);
			m.setStreamVolume(AudioManager.STREAM_RING, (int)vol, 0);
			m.setStreamVolume(AudioManager.STREAM_ALARM, (int)vol, 0);
			m.setStreamVolume(AudioManager.STREAM_MUSIC, (int)vol, 0);
		}
	}
}
