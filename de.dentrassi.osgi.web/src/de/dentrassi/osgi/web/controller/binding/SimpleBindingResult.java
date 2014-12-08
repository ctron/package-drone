package de.dentrassi.osgi.web.controller.binding;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dentrassi.osgi.web.controller.binding.BindingManager.Result;

public class SimpleBindingResult implements BindingResult
{
    private final Map<String, BindingResult> children = new HashMap<> ();

    private final List<BindingError> errors = new LinkedList<> ();

    @Override
    public boolean hasErrors ()
    {
        if ( !this.errors.isEmpty () )
        {
            return true;
        }

        for ( final BindingResult br : this.children.values () )
        {
            if ( br.hasErrors () )
            {
                return true;
            }
        }
        return false;
    }

    public void addChild ( final String name, final BindingResult bindingResult )
    {
        this.children.put ( name, bindingResult );
    }

    @Override
    public BindingResult getChild ( final String name )
    {
        return this.children.get ( name );
    }

    public void addErrors ( final String name, final List<BindingError> errors )
    {
        BindingResult br = this.children.get ( name );
        if ( br == null )
        {
            br = new Result ();
        }

        for ( final BindingError error : errors )
        {
            br.addError ( error );
        }
    }

    @Override
    public void addError ( final BindingError error )
    {
        this.errors.add ( error );
    }

    @Override
    public List<BindingError> getErrors ()
    {
        final List<BindingError> result = new LinkedList<> ( this.errors );

        for ( final BindingResult br : this.children.values () )
        {
            result.addAll ( br.getErrors () );
        }

        return result;
    }
}
