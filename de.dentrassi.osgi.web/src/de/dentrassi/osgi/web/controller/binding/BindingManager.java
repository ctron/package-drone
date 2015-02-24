/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.controller.binding;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dentrassi.osgi.converter.ConverterManager;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.controller.validator.ValidationResult;
import de.dentrassi.osgi.web.controller.validator.Validator;

public class BindingManager
{
    public static class Result extends SimpleBindingResult
    {
    }

    public interface Call
    {
        public Object invoke () throws Exception;
    }

    private final ConverterManager converter;

    public BindingManager ()
    {
        this.converter = ConverterManager.create ();
    }

    public static final BindingManager create ( final Map<String, Object> data )
    {
        final BindingManager result = new BindingManager ();

        result.addBinder ( new MapBinder ( data ) );
        result.addBinder ( new BindingManagerBinder () );

        return result;
    }

    protected Binding bindValue ( final BindTarget target, final ConverterManager converter )
    {
        for ( final Binder binder : this.binders )
        {
            final Binding binding = binder.performBind ( target, converter, this );
            if ( binding != null )
            {
                return binding;
            }
        }
        return null;
    }

    private BindTarget createParameterTarget ( final Parameter parameter, final Object[] args, final int argumentIndex )
    {
        return new ParameterBindTarget ( parameter, args, argumentIndex );
    }

    protected BindTarget createPropertyTarget ( final Object object, final PropertyDescriptor pd )
    {
        return new PropertyBindTarget ( object, pd );
    }

    public Call bind ( final Method method, final Object targetObject )
    {
        final Parameter[] p = method.getParameters ();

        final Binding[] bindings = new Binding[p.length];
        final Object[] args = new Object[p.length];

        for ( int i = 0; i < p.length; i++ )
        {
            final BindTarget target = createParameterTarget ( p[i], args, i );
            final Binding binding = bindValue ( target, this.converter );

            if ( binding != null )
            {
                bindings[i] = binding;
                target.bind ( binding );
            }
            else
            {
                throw new IllegalStateException ( String.format ( "Unable to bind parameter '%s' (%s)", p[i].getName (), p[i].getType () ) );
            }
        }

        return new Call () {

            @Override
            public Object invoke () throws Exception
            {
                Object result = method.invoke ( targetObject, args );
                for ( final Binding binding : bindings )
                {
                    result = binding.postProcess ( result );
                }
                result = postProcess ( result );
                return result;
            }
        };
    }

    protected Object postProcess ( final Object result )
    {
        if ( result instanceof ModelAndView )
        {
            ( (ModelAndView)result ).put ( BindingResult.ATTRIBUTE_NAME, this.result );
        }
        else if ( result instanceof String )
        {
            final ModelAndView mav = new ModelAndView ( (String)result );
            mav.put ( BindingResult.ATTRIBUTE_NAME, this.result );
            return mav;
        }
        return result;
    }

    private final Collection<Binder> binders = new LinkedList<> ();

    private Validator validator;

    private final Result result = new Result ();

    public void addBinder ( final Binder binder )
    {
        this.binders.add ( binder );
    }

    public void bindProperties ( final Object o ) throws Exception
    {
        if ( o == null )
        {
            return;
        }

        final Class<?> objectClass = o.getClass ();
        final BeanInfo bi = Introspector.getBeanInfo ( objectClass );

        for ( final PropertyDescriptor pd : bi.getPropertyDescriptors () )
        {
            if ( pd.getWriteMethod () != null )
            {
                final BindTarget target = createPropertyTarget ( o, pd );
                final Binding binding = bindValue ( target, this.converter );
                if ( binding != null )
                {
                    target.bind ( binding );
                    this.result.addChild ( pd.getName (), binding.getBindingResult () );
                }
            }
        }

        validate ( o );
    }

    protected void validate ( final Object o )
    {
        if ( this.validator == null )
        {
            return;
        }

        final ValidationResult vr = this.validator.validate ( o );

        for ( final Map.Entry<String, List<BindingError>> entry : vr.getErrors ().entrySet () )
        {
            this.result.addErrors ( entry.getKey (), entry.getValue () );
        }

        this.result.addMarkers ( vr.getMarkers () );
    }

    public BindingResult getResult ()
    {
        return this.result;
    }

    public void setValidator ( final Validator validator )
    {
        this.validator = validator;
    }
}
