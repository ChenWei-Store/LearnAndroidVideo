package com.learnvideo.playaudio.exception;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by Chenwei on 2017/9/5.
 *
 */

public class InitializeFailException extends RuntimeException {
    public InitializeFailException() {
        super();
    }

    public InitializeFailException(String message) {
        super(message);
    }

    public InitializeFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeFailException(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public InitializeFailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
