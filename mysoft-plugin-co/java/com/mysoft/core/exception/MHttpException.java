package com.mysoft.core.exception;

/**
 * 自定义Exception
 * 
 * @author zhouj04
 */
public class MHttpException extends Exception {

	private static final long serialVersionUID = -7270805647048264710L;

	public MHttpException() {
		super();
	}

	public MHttpException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public MHttpException(String detailMessage) {
		super(detailMessage);
	}

	public MHttpException(Throwable throwable) {
		super(throwable);
	}

}
