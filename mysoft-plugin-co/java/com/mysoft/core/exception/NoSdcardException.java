package com.mysoft.core.exception;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.mysoft.core.MHandler;
import com.mysoft.core.MHandler.MHandlerListener;

public class NoSdcardException extends Exception implements MHandlerListener {

	private static final long serialVersionUID = 8474485224972281969L;
	private MHandler mHandler = new MHandler(Looper.getMainLooper(), this);
	private Context mContext;

	public NoSdcardException(Context context) {
		super("未找到可用的SD卡，请先安装SD卡");
		this.mContext = context;
	}

	public void toast() {
		mHandler.sendEmptyMessage(0);
	}

	@Override
	public void handleMessage(Message msg) {
		Toast.makeText(mContext, getMessage(), Toast.LENGTH_LONG).show();
	}
}
