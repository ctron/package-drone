package de.dentrassi.osgi.web.controller.binding;

import de.dentrassi.osgi.web.controller.converter.ConverterManager;
import de.dentrassi.osgi.web.controller.routing.RequestMappingInformation.Match;

public class PathVariableBinder implements Binder
{
    private final Match match;

    public PathVariableBinder ( final Match match )
    {
        this.match = match;
    }

    @Override
    public Binding performBind ( final BindTarget target, final ConverterManager converter, final BindingManager bindingManager )
    {
        final PathVariable pv = target.getAnnotation ( PathVariable.class );
        if ( pv == null )
        {
            return null;
        }

        final String valueString = this.match.getAttributes ().get ( pv.value () );

        final Object value = converter.convertTo ( valueString, target.getType () );

        return Binding.simpleBinding ( value );
    }
}
