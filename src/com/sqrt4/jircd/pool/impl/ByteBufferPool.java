package com.sqrt4.jircd.pool.impl;

import com.sqrt4.jircd.pool.ObjectPool;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ByteBufferPool extends ObjectPool<ByteBuffer> {
    private static Map<Integer, ByteBufferPool> pools = new HashMap<Integer, ByteBufferPool>();

    private int size;

    public ByteBufferPool(int size) {
        this.size = size;
    }

    public ByteBuffer create() {
        return ByteBuffer.allocateDirect(size);
    }

    @Override
    public void reset(ByteBuffer byteBuffer) {
        byteBuffer.clear();
    }

    public static ByteBuffer acquire(int size) {
        int rem = size %4096;
        if(rem != 0)
            size += 4096 - rem;

        if(!pools.containsKey(size))
            pools.put(size, new ByteBufferPool(size));

        return pools.get(size).acquire();
    }
}