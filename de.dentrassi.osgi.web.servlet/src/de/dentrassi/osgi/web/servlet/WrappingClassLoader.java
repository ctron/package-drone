package de.dentrassi.osgi.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class WrappingClassLoader extends ClassLoader
{

    public WrappingClassLoader ( final ClassLoader parentClassLoader )
    {
        super ( parentClassLoader );
    }

    @Override
    public URL getResource ( final String name )
    {
        System.out.println ( "getResource - " + name );
        return super.getResource ( name );
    }

    @Override
    public InputStream getResourceAsStream ( final String name )
    {
        System.out.println ( "getResourceAsStream - " + name );
        return super.getResourceAsStream ( name );
    }

    @Override
    public Enumeration<URL> getResources ( final String name ) throws IOException
    {
        System.out.println ( "getResources - " + name );
        return super.getResources ( name );
    }

    @Override
    protected URL findResource ( final String name )
    {
        System.out.println ( "findResource - " + name );
        return super.findResource ( name );
    }

    @Override
    protected Enumeration<URL> findResources ( final String name ) throws IOException
    {
        System.out.println ( "findResources - " + name );
        return super.findResources ( name );
    }

    @Override
    protected Class<?> findClass ( final String name ) throws ClassNotFoundException
    {
        System.out.println ( "findClass - " + name );
        return super.findClass ( name );
    }

    @Override
    public Class<?> loadClass ( final String name ) throws ClassNotFoundException
    {
        System.out.println ( "loadClass - " + name );
        return super.loadClass ( name );
    }

    @Override
    protected Class<?> loadClass ( final String name, final boolean resolve ) throws ClassNotFoundException
    {
        System.out.println ( "loadClass - " + name + " - " + resolve );
        return super.loadClass ( name, resolve );
    }
}
