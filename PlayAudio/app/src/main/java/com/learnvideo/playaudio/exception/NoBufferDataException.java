package com.learnvideo.playaudio.exception;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by Chenwei on 2017/9/9.
 */

public class NoBufferDataException extends RuntimeException {
    public NoBufferDataException() {
    }

    public NoBufferDataException(String message) {
        super(message);
    }

    public NoBufferDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoBufferDataException(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public NoBufferDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
