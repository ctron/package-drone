package de.dentrassi.osgi.web.controller.validator;

import java.util.List;
import java.util.Map;

import de.dentrassi.osgi.web.controller.binding.BindingError;

public interface Validator
{
    public Map<String, List<BindingError>> validate ( Object target );
}
