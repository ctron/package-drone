package de.dentrassi.osgi.web.controller.binding;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.controller.converter.ConverterManager;

public class RequestParameterBinder implements Binder
{
    private final static Logger logger = LoggerFactory.getLogger ( RequestParameterBinder.class );

    private final HttpServletRequest request;

    public RequestParameterBinder ( final HttpServletRequest request )
    {
        this.request = request;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final RequestParameter rp = target.getAnnotation ( RequestParameter.class );
        if ( rp == null )
        {
            return null;
        }

        final Class<?> type = target.getType ();

        if ( type.isAssignableFrom ( Part.class ) )
        {
            try
            {
                final Part value = this.request.getPart ( rp.value () );
                return Binding.simpleBinding ( value );
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to get part '{}'", rp.value () );
                return null;
            }
        }
        else
        {
            final String valueString = this.request.getParameter ( rp.value () );

            if ( valueString == null )
            {
                if ( rp.required () )
                {
                    throw new IllegalStateException ( String.format ( "Request parameter '%s' is required but missing.", rp.value () ) );
                }

                return Binding.nullBinding ();
            }
            else
            {
                final Object value = converter.convertTo ( valueString, type );
                return Binding.simpleBinding ( value );
            }
        }
    }
}
