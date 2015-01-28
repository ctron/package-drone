package de.dentrassi.osgi.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.ANNOTATION_TYPE )
public @interface Processed
{
    public Class<? extends AnnotationProcessor> value ();
}
