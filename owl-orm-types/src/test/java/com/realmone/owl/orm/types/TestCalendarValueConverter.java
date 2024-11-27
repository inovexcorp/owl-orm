/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the MIT License
 */
package com.realmone.owl.orm.types;

import org.eclipse.rdf4j.model.Literal;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class TestCalendarValueConverter extends AbstractValueConverterTest<Calendar, Literal> {

    public TestCalendarValueConverter() {
        super(new GregorianCalendar(), VALUE_FACTORY.createLiteral("not a calendar"), new CalendarValueConverter());
    }

    @Override
    protected boolean valuesEqual(Calendar before, Calendar after) {
        return before.getTimeInMillis() == after.getTimeInMillis();
    }
}
