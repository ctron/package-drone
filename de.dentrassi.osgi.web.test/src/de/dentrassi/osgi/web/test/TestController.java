package de.dentrassi.osgi.web.test;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;

@Controller
@ViewResolver ( "WEB-INF/views/%s.jsp" )
public class TestController
{
    @RequestMapping ( "/" )
    public ModelAndView main ()
    {
        return new ModelAndView ( "index" );
    }
}
