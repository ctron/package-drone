package de.dentrassi.pm.storage.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.dentrassi.pm.storage.web.menu.DefaultMenuExtender;

@Controller
public class WelcomeController extends AbstractDefaultController
{

    @Override
    protected void fillMenu ( final DefaultMenuExtender menuExtener )
    {
        menuExtener.addEntry ( "/", "Home", 0 );
    }

    @RequestMapping ( value = "/", method = RequestMethod.GET )
    public ModelAndView main ()
    {
        if ( Activator.getTracker ().getStorageService () != null )
        {
            return new ModelAndView ( "index" );
        }
        else
        {
            return new ModelAndView ( "redirect:/setup" );
        }
    }
}
