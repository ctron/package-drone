package de.dentrassi.osgi.web.controller.binding;

public class SimpleBinding implements Binding
{
    private final Object value;

    private final BindingResult bindingResult;

    public SimpleBinding ( final Object value )
    {
        this.value = value;
        this.bindingResult = new SimpleBindingResult ();
    }

    public SimpleBinding ( final Object value, final BindingResult bindingResult )
    {
        this.value = value;
        this.bindingResult = bindingResult;
    }

    @Override
    public Object getValue ()
    {
        return this.value;
    }

    @Override
    public BindingResult getBindingResult ()
    {
        return this.bindingResult;
    }
}
