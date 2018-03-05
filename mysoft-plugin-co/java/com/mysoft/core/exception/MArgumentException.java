package com.mysoft.core.exception;

/**
 * Created by zhouj04 on 2016/4/22.
 */
public class MArgumentException extends Exception {

    public MArgumentException(Throwable throwable) {
        super(throwable);
    }

    public MArgumentException(String detailMessage, Throwable throwable) {

        super(detailMessage, throwable);
    }

    public MArgumentException(String detailMessage) {

        super(detailMessage);
    }

    public MArgumentException() {
        super();
    }
}
