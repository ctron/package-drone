/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.controller.form;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import de.dentrassi.osgi.converter.ConverterManager;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.controller.binding.BindTarget;
import de.dentrassi.osgi.web.controller.binding.Binder;
import de.dentrassi.osgi.web.controller.binding.Binding;
import de.dentrassi.osgi.web.controller.binding.BindingManager;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.MapBinder;
import de.dentrassi.osgi.web.controller.binding.ModelAndViewAwareBinding;
import de.dentrassi.osgi.web.controller.validator.CompositeValidator;
import de.dentrassi.osgi.web.controller.validator.ControllerValidatorProcessor;
import de.dentrassi.osgi.web.controller.validator.FormDataValidator;
import de.dentrassi.osgi.web.controller.validator.JavaValidator;
import de.dentrassi.osgi.web.controller.validator.Validator;

public class FormDataBinder implements Binder
{
    private final HttpServletRequest request;

    private final Object controller;

    public FormDataBinder ( final HttpServletRequest request )
    {
        this.request = request;
        this.controller = null;
    }

    public FormDataBinder ( final HttpServletRequest request, final Object controller )
    {
        this.request = request;
        this.controller = controller;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final FormData fd = target.getAnnotation ( FormData.class );
        if ( fd == null )
        {
            return null;
        }

        final Class<?> clazz = target.getType ();
        final String name = fd.value ();

        final ConstructionResult cr;
        try
        {
            cr = contruct ( clazz, bindingManager, target );
        }
        catch ( final Exception e )
        {
            throw new IllegalStateException ( String.format ( "Failed to create model object of type %s", clazz.getName () ), e );
        }

        if ( name != null )
        {
            bindingManager.getResult ().addChild ( name, cr.bindingResult );
        }

        return new ModelAndViewAwareBinding ( cr.object, cr.bindingResult ) {
            @Override
            public void postProcessModelAndView ( final ModelAndView mav )
            {
                if ( !name.isEmpty () )
                {
                    mav.put ( name, getValue () );
                }
            }
        };
    }

    static class ConstructionResult
    {
        Object object;

        BindingResult bindingResult;

        public ConstructionResult ( final Object object, final BindingResult bindingResult )
        {
            this.object = object;
            this.bindingResult = bindingResult;
        }
    }

    private ConstructionResult contruct ( final Class<?> clazz, final BindingManager bindingManager, final BindTarget target ) throws Exception
    {
        final Object o = clazz.newInstance ();

        final Map<String, Object> objects = new HashMap<> ();

        final Enumeration<String> en = this.request.getParameterNames ();
        while ( en.hasMoreElements () )
        {
            final String key = en.nextElement ();
            final String[] value = this.request.getParameterValues ( key );

            if ( value.length == 1 )
            {
                objects.put ( key, value[0] );
            }
            else
            {
                objects.put ( key, value );
            }
        }

        final BindingManager bm = new BindingManager ();

        if ( target.isAnnotationPresent ( Valid.class ) )
        {
            final List<Validator> validators = new LinkedList<> ();
            validators.add ( new JavaValidator () );
            validators.add ( new FormDataValidator () );

            if ( this.controller != null )
            {
                validators.add ( new ControllerValidatorProcessor ( this.controller ) );
            }

            bm.setValidator ( new CompositeValidator ( validators ) );
        }

        bm.addBinder ( new MapBinder ( objects ) );
        bm.bindProperties ( o );

        return new ConstructionResult ( o, bm.getResult () );
    }
}
