package de.dentrassi.pm.storage.channel.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Disposing<T> implements AutoCloseable, InvocationHandler
{
    private final T target;

    private final T proxy;

    private boolean disposed;

    public Disposing ( final Class<T> clazz, final T target )
    {
        if ( target == null )
        {
            throw new NullPointerException ( "'target' must not be null" );
        }

        this.target = target;
        this.proxy = clazz.cast ( Proxy.newProxyInstance ( clazz.getClassLoader (), new Class<?>[] { clazz }, this ) );
    }

    public T getTarget ()
    {
        return this.proxy;
    }

    @Override
    public void close ()
    {
        this.disposed = true;
    }

    @Override
    public Object invoke ( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        if ( this.disposed )
        {
            throw new IllegalStateException ( "Object is already disposed" );
        }
        return method.invoke ( this.target, args );
    }

    public static <T> Disposing<T> proxy ( final Class<T> clazz, final T target )
    {
        return new Disposing<T> ( clazz, target );
    }
}
