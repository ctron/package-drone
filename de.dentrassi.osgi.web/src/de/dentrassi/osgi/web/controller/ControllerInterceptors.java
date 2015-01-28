package de.dentrassi.osgi.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.TYPE )
public @interface ControllerInterceptors
{
    ControllerInterceptor[] value ();
}
