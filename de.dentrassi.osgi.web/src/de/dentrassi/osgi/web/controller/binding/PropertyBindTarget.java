package de.dentrassi.osgi.web.controller.binding;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyBindTarget implements BindTarget
{
    private final static Logger logger = LoggerFactory.getLogger ( PropertyBindTarget.class );

    private final PropertyDescriptor propertyDescriptor;

    private final Class<?> objectClass;

    private final Object target;

    public PropertyBindTarget ( final Object target, final PropertyDescriptor propertyDescriptor )
    {
        this.propertyDescriptor = propertyDescriptor;
        this.target = target;
        this.objectClass = target.getClass ();
    }

    @Override
    public Class<?> getType ()
    {
        return this.propertyDescriptor.getPropertyType ();
    }

    @Override
    public String getQualifier ()
    {
        return this.propertyDescriptor.getName ();
    }

    @Override
    public boolean isAnnotationPresent ( final Class<Valid> clazz )
    {
        return getAnnotation ( clazz ) != null;
    }

    @Override
    public <T extends Annotation> T getAnnotation ( final Class<T> clazz )
    {
        final T a = this.propertyDescriptor.getWriteMethod ().getAnnotation ( clazz );
        if ( a != null )
        {
            return a;
        }

        Field field;
        try
        {
            field = this.objectClass.getField ( this.propertyDescriptor.getName () );
        }
        catch ( final Exception e )
        {
            return null;
        }

        return field.getAnnotation ( clazz );
    }

    @Override
    public void bind ( final Binding binding )
    {
        try
        {
            this.propertyDescriptor.getWriteMethod ().invoke ( this.target, binding.getValue () );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to apply property", e );
            binding.getBindingResult ().addError ( new ExceptionError ( e ) );
        }
    }
}
