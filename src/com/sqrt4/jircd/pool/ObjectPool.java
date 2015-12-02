package com.sqrt4.jircd.pool;

import com.sqrt4.jircd.sched.Task;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class ObjectPool<T> {
    private static Map<Class<?>, ObjectPool<?>> auto = new HashMap<Class<?>, ObjectPool<?>>();

    private final LinkedList<SoftReference<T>> available = new LinkedList<SoftReference<T>>();
    private final LinkedList<SoftReference<T>> loaned = new LinkedList<SoftReference<T>>();
    private final ReferenceQueue<T> released = new ReferenceQueue<T>();

    public ObjectPool() {
        new ReferenceReclamationTask().scheduleWithFixedDelay(0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns an object from this pool
     * @return an object from this pool
     */
    public final T acquire() {
        T t = null;

        // Do we have one available?
        synchronized (available) {
            Iterator<SoftReference<T>> it = available.iterator();
            while (it.hasNext()) {
                SoftReference<T> sr = it.next();
                T t1 = sr.get();
                it.remove();
                if (t1 != null) {
                    sr.clear();
                    sr = null;
                    t = t1;
                    break;
                }
            }
        }

        // If not, create one
        if (t == null)
            t = create();

        synchronized (loaned) {
            loaned.add(new SoftReference<T>(t, released));
        }
        return t;
    }

    /**
     * Overriden in subclasses to create new instances when the pool is dry
     * @return a new object
     */
    public abstract T create();

    /**
     * Called when an object is released and made available to reset it to its default state
     * @param t the object to return to its default state
     */
    public void reset(T t) {

    }

    public static <T> ObjectPool<T> poolFor(Class<T> c) throws Exception {
        if (!auto.containsKey(c))
            auto.put(c, new SimplePool<T>(c));
        return (ObjectPool<T>) auto.get(c);
    }

    private class ReferenceReclamationTask extends Task {
        public Object call() throws Exception {
            Reference<? extends T> r;
            while ((r = released.poll()) != null) {
                synchronized (loaned) {
                    loaned.remove(r); // Remove the reference to the softreference...
                }
                T t = r.get();
                if (t == null)
                    continue; // Already been garbage collected
                System.out.println("Object released!");
                reset(t);
                synchronized (available) { // Make available for lending
                    available.add(new SoftReference<T>(t));
                }
            }
            return null;
        }
    }
}