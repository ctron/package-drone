package de.dentrassi.osgi.web.controller.binding;

public interface Binding
{
    public Object getValue ();

    public BindingResult getBindingResult ();

    public default Object postProcess ( final Object result )
    {
        return result;
    }

    // static factory methods

    public static Binding simpleBinding ( final Object value )
    {
        return new SimpleBinding ( value );
    }

    public static Binding nullBinding ()
    {
        return new Binding () {

            @Override
            public Object getValue ()
            {
                return null;
            }

            @Override
            public BindingResult getBindingResult ()
            {
                return null;
            }
        };
    }
}
