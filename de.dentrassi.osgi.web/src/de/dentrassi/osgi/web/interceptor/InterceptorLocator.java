package de.dentrassi.osgi.web.interceptor;

import de.dentrassi.osgi.web.Interceptor;

public interface InterceptorLocator
{

    public void close ();

    public Interceptor[] getInterceptors ();

}
