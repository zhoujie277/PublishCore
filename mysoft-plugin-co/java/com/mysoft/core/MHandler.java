package com.mysoft.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 
 * <br/>
 * 2015-6-11
 * 
 * @author Jay
 */
public class MHandler extends Handler {
	private WeakReference<MHandlerListener> mHandlerListenerRef = null;

	@Override
	public void handleMessage(Message msg) {
		if (mHandlerListenerRef != null && mHandlerListenerRef.get() != null) {
			mHandlerListenerRef.get().handleMessage(msg);
		}
	}

	public MHandler(MHandlerListener listener) {
		this(Looper.getMainLooper(), listener);
	}

	public MHandler(Looper looper, MHandlerListener listener) {
		super(looper);
		mHandlerListenerRef = new WeakReference<MHandlerListener>(listener);
	}

	public void setOnHandlerListener(MHandlerListener listener) {
		if (mHandlerListenerRef != null) {
			mHandlerListenerRef.clear();
		}
		mHandlerListenerRef = new WeakReference<MHandlerListener>(listener);
	}

	public static interface MHandlerListener {
		void handleMessage(Message msg);
	}
}
