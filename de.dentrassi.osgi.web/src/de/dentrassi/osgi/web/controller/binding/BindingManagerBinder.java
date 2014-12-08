package de.dentrassi.osgi.web.controller.binding;

import java.util.Map;

import de.dentrassi.osgi.web.controller.converter.ConverterManager;

public class BindingManagerBinder extends MapBinder
{
    private boolean added;

    public BindingManagerBinder ()
    {
        super ();
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        if ( !this.added )
        {
            final Map<String, Object> data = getObjects ();
            data.put ( "bindingManager", bindingManager );
            data.put ( "bindingResult", bindingManager.getResult () );
            this.added = true;
        }

        return super.performBind ( target, converter, bindingManager );
    }

}
