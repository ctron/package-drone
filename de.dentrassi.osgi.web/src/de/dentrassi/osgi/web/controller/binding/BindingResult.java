package de.dentrassi.osgi.web.controller.binding;

import java.util.List;

public interface BindingResult
{
    public static final String ATTRIBUTE_NAME = BindingResult.class.getName ();

    public boolean hasErrors ();

    public BindingResult getChild ( String name );

    public void addChild ( String name, BindingResult bindingResult );

    public void addError ( BindingError error );

    public List<BindingError> getErrors ();
}
