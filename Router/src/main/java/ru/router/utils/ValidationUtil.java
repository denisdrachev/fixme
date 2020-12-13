package ru.router.utils;

import ru.router.model.Fix;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class ValidationUtil {

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static Validator validator = factory.getValidator();

    synchronized public static Set<ConstraintViolation<Fix>> validate(Fix fix) {
        return validator.validate(fix);
    }
}
