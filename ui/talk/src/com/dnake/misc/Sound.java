package com.dnake.misc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;

@SuppressLint("UseSparseArrays")
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

	public static String sid_success = "/dnake/bin/prompt/sdt_success.wav";
	public static String sid_failed = "/dnake/bin/prompt/sdt_failed.wav";
	public static String sid_invalid = "/dnake/bin/prompt/sdt_invalid.wav";

	public static String face_success = "/dnake/bin/prompt/face_success.wav";

	public static String[] key = new String[10];
	public static boolean key_0_9 = false;

	public static void load() {
		if (!Locale.getDefault().getCountry().equals("CN")) {
			modify_success = "/dnake/bin/prompt/en/modify_success.wav";
			modify_failed = "/dnake/bin/prompt/en/modify_failed.wav";
			passwd_err = "/dnake/bin/prompt/en/passwd_err.wav";
			unlock = "/dnake/bin/prompt/en/unlock.wav";
			face_success = "/dnake/bin/prompt/en/unlock.wav";
			for(int i=0; i<10; i++) {
				key[i] = "/dnake/bin/prompt/press.wav";
			}
		} else {
			modify_success = "/dnake/bin/prompt/modify_success.wav";
			modify_failed = "/dnake/bin/prompt/modify_failed.wav";
			passwd_err = "/dnake/bin/prompt/passwd_err.wav";
			unlock = "/dnake/bin/prompt/unlock.wav";
			face_success = "/dnake/bin/prompt/face_success.wav";
			for(int i=0; i<10; i++) {
				key[i] = "/dnake/bin/prompt/key/"+i+".wav";
			}
		}

		mSoundPoolMap.clear();
		for (int i = 0; i < 10; i++) {
			mSoundPoolMap.put(i, mSoundPool.load(key[i], i));
		}
		mSoundPoolMap.put(OrderPress, mSoundPool.load(press, OrderPress));
		mSoundPoolMap.put(OrderUnlock, mSoundPool.load(unlock, OrderUnlock));
		mSoundPoolMap.put(OrderCardFailed, mSoundPool.load(card_err, OrderCardFailed));
		mSoundPoolMap.put(OrderSidOK, mSoundPool.load(sid_success, OrderSidOK));
		mSoundPoolMap.put(OrderSidFailed, mSoundPool.load(sid_failed, OrderSidFailed));
		mSoundPoolMap.put(OrderSidInvalid, mSoundPool.load(sid_invalid, OrderSidInvalid));
		mSoundPoolMap.put(OrderPasswdErr, mSoundPool.load(passwd_err, OrderPasswdErr));
		mSoundPoolMap.put(OrderFaceOK, mSoundPool.load(face_success, OrderFaceOK));
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
						p.stop();
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
			mp.reset();
			mp.release();
		}
		return null;
	}

	public static void stop(MediaPlayer mp) {
		if (mp != null) {
			mp.stop();
			mp.reset();
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

	public static int OrderPress = 100;
	public static int OrderUnlock = 101;
	public static int OrderCardFailed = 102;
	public static int OrderSidOK = 103;
	public static int OrderSidFailed = 104;
	public static int OrderSidInvalid = 105;
	public static int OrderPasswdErr = 106;
	public static int OrderFaceOK = 107;

	private static SoundPool mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
	private static HashMap<Integer, Integer> mSoundPoolMap = new HashMap<Integer, Integer>();

	public static void play(int order) {
		AudioManager m = (AudioManager) SysTalk.mContext.getSystemService(Context.AUDIO_SERVICE);
		int max = m.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int val = m.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volume = (float)val/max;
		mSoundPool.play(mSoundPoolMap.get(order), volume, volume, order, 0, 1);
	}
}
