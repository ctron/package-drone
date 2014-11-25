package de.dentrassi.pm.p2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Handler
{
    public void prepare () throws Exception;

    public void process ( HttpServletRequest req, HttpServletResponse resp ) throws Exception;
}
