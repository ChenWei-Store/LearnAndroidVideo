package com.learnvideo.playaudio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Chenwei on 2017/9/12.
 */

public class Utils {

    public static short[] byte2Short(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }
    public static byte[] short2Byte(short[] values) {
        byte[] bytes = new byte[values.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(values);
        return bytes;
    }
}
