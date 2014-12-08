package de.dentrassi.osgi.web.controller.converter;

public interface Converter<T>
{
    public T convertTo ( String value ) throws ConversionException;

    public Class<T> getType ();
}
