package de.dentrassi.osgi.web.controller.binding;

public class ExceptionError implements BindingError
{
    private final Exception ex;

    public ExceptionError ( final Exception ex )
    {
        this.ex = ex;
    }

    @Override
    public String getMessage ()
    {
        return this.ex.getMessage ();
    }

}
