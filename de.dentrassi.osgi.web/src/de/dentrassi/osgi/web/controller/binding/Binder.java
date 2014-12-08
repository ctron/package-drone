package de.dentrassi.osgi.web.controller.binding;

import de.dentrassi.osgi.web.controller.converter.ConverterManager;

public interface Binder
{
    public Binding performBind ( BindTarget target, ConverterManager converter, BindingManager bindingManager );
}
