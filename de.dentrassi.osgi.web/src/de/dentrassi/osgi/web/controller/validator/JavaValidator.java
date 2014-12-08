package de.dentrassi.osgi.web.controller.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import de.dentrassi.osgi.web.controller.binding.BindingError;
import de.dentrassi.osgi.web.internal.Activator;

public class JavaValidator implements Validator
{
    public static class ConstraintViolationBindingError implements BindingError
    {
        private final ConstraintViolation<?> violation;

        public ConstraintViolationBindingError ( final ConstraintViolation<?> violation )
        {
            this.violation = violation;
        }

        @Override
        public String getMessage ()
        {
            return this.violation.getMessage ();
        }
    }

    private final javax.validation.Validator validator;

    public JavaValidator ()
    {
        final ValidatorFactory factory = Validation.byDefaultProvider ().providerResolver ( Activator.getValidationProviderResolver () ).configure ().buildValidatorFactory ();
        this.validator = factory.getValidator ();
    }

    @Override
    public Map<String, List<BindingError>> validate ( final Object target )
    {
        final Set<ConstraintViolation<Object>> vr = this.validator.validate ( target );

        if ( vr == null || vr.isEmpty () )
        {
            return Collections.emptyMap ();
        }

        final Map<String, List<BindingError>> result = new HashMap<> ();

        for ( final ConstraintViolation<Object> entry : vr )
        {
            final String path = makePath ( entry );
            List<BindingError> errors = result.get ( path );
            if ( errors == null )
            {
                errors = new LinkedList<> ();
                result.put ( path, errors );
            }
            errors.add ( convert ( entry ) );
        }

        return result;
    }

    private BindingError convert ( final ConstraintViolation<Object> entry )
    {
        return new ConstraintViolationBindingError ( entry );
    }

    private String makePath ( final ConstraintViolation<Object> entry )
    {
        final StringBuilder sb = new StringBuilder ();

        final Path p = entry.getPropertyPath ();
        for ( final Node n : p )
        {
            if ( sb.length () > 0 )
            {
                sb.append ( '.' );
            }
            sb.append ( n.getName () );
        }

        return sb.toString ();
    }
}
