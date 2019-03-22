package com.xsrt.camerademo.record;

import android.graphics.ImageFormat;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.HandlerThread;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class RecordThread extends HandlerThread {
    public RecordThread(String name) {
        super(name);
    }

    public RecordThread(String name, int priority) {
        super(name, priority);
    }

    private void test(StreamConfigurationMap map, ImageReader reade) {
        Image image = reade.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
            @Override
            public int compare(Size o1, Size o2) {
                return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
            }
        });
    }
}
