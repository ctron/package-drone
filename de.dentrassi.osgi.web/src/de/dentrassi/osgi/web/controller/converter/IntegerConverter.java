package de.dentrassi.osgi.web.controller.converter;

public class IntegerConverter implements Converter<Integer>
{
    public static final IntegerConverter INSTANCE = new IntegerConverter ();

    @Override
    public Class<Integer> getType ()
    {
        return Integer.class;
    }

    @Override
    public Integer convertTo ( final String value )
    {
        try
        {
            return Integer.parseInt ( value );
        }
        catch ( final NumberFormatException e )
        {
            throw new ConversionException ( e );
        }
    }
}
