package de.dentrassi.osgi.web.controller.converter;

public class ConversionException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ConversionException ()
    {
        super ();
    }

    public ConversionException ( final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace )
    {
        super ( message, cause, enableSuppression, writableStackTrace );
    }

    public ConversionException ( final String message, final Throwable cause )
    {
        super ( message, cause );
    }

    public ConversionException ( final String message )
    {
        super ( message );
    }

    public ConversionException ( final Throwable cause )
    {
        super ( cause );
    }

}
