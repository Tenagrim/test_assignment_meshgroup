package com.tenagrim.meshgrouptestassg.util;

import org.springframework.dao.DataIntegrityViolationException;

public class Persistence {
    public static boolean isConstraint(DataIntegrityViolationException e, String constraintName) {
        return e.getCause() instanceof org.hibernate.exception.ConstraintViolationException
                && constraintName.equalsIgnoreCase(
                ((org.hibernate.exception.ConstraintViolationException) e.getCause()).getConstraintName());
    }
}
