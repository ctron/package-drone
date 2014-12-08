package de.dentrassi.osgi.web.controller.binding;

import de.dentrassi.osgi.web.ModelAndView;

public abstract class ModelAndViewAwareBinding extends SimpleBinding
{
    public ModelAndViewAwareBinding ( final Object value, final BindingResult bindingResult )
    {
        super ( value, bindingResult );
    }

    @Override
    public Object postProcess ( final Object result )
    {
        if ( result instanceof ModelAndView )
        {
            postProcessModelAndView ( (ModelAndView)result );
        }
        else if ( result instanceof String )
        {
            final ModelAndView mav = new ModelAndView ( (String)result );
            postProcessModelAndView ( mav );
            return mav;
        }

        return result;
    }

    public abstract void postProcessModelAndView ( ModelAndView mav );
}
