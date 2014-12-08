package de.dentrassi.osgi.web.controller.binding;

import java.lang.annotation.Annotation;

import javax.validation.Valid;

public interface BindTarget
{
    public Class<?> getType ();

    public <T extends Annotation> T getAnnotation ( Class<T> clazz );

    public void bind ( Binding binding );

    public String getQualifier ();

    public boolean isAnnotationPresent ( Class<Valid> clazz );
}
