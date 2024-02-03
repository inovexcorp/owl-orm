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
