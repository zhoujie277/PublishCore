package com.mysoft.core.exception;

import android.text.format.Formatter;

import com.mysoft.core.MApplication;

public class SdcardNoSpaceException extends Exception {

	private static final long serialVersionUID = 8474485224972281969L;

	public SdcardNoSpaceException(long size) {
		this("SDcard空间不足，剩余可用空间：" + Formatter.formatFileSize(MApplication.getApplication(), size));
	}

	public SdcardNoSpaceException(String msg) {
		super(msg);
	}

}
