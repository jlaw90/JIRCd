package com.sqrt4.jircd.pool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SimplePool<T> extends ObjectPool<T> {
    private Constructor cons;

    public SimplePool(Class<T> clazz) throws Exception {
        Constructor[] cons = clazz.getConstructors();
        for(Constructor c: cons) {
            if(c.getParameterTypes().length == 0) {
                this.cons = c;
                break;
            }
        }
        if(this.cons == null)
            throw new Exception("No default constructor available!");
    }

    public T create() {
        try {
            return (T) cons.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}