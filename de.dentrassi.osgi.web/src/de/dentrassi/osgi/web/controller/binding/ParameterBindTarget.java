package de.dentrassi.osgi.web.controller.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import javax.validation.Valid;

public class ParameterBindTarget implements BindTarget
{

    private final Parameter parameter;

    private final Object[] args;

    private final int argumentIndex;

    public ParameterBindTarget ( final Parameter parameter, final Object[] args, final int argumentIndex )
    {
        this.parameter = parameter;
        this.args = args;
        this.argumentIndex = argumentIndex;
    }

    @Override
    public Class<?> getType ()
    {
        return this.parameter.getType ();
    }

    @Override
    public String getQualifier ()
    {
        // although we do have a parameter name, it is not reliable
        return null;
    }

    @Override
    public boolean isAnnotationPresent ( final Class<Valid> clazz )
    {
        return this.parameter.isAnnotationPresent ( clazz );
    }

    @Override
    public <T extends Annotation> T getAnnotation ( final Class<T> clazz )
    {
        return this.parameter.getAnnotation ( clazz );
    }

    @Override
    public void bind ( final Binding binding )
    {
        this.args[this.argumentIndex] = binding.getValue ();
    }

}
