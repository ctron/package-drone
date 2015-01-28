package de.dentrassi.osgi.web.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.TYPE )
@Repeatable ( ControllerInterceptors.class )
public @interface ControllerInterceptor
{
    public Class<? extends ControllerInterceptorProcessor> value ();
}
