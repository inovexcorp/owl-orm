/*
 *
 *   owl-orm: A Maven Plugin and API for working with POJOs representing ontological classes on top of RDF4j
 *   Copyright (c) 2024 RealmOne (https://realmone.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.realmone.owl.orm.types;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.base.CoreDatatype;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * {@link com.realmone.owl.orm.types.ValueConverter} for creating {@link Calendar} objects from statements.
 *
 * @author bdgould
 */
public class CalendarValueConverter extends AbstractValueConverter<Calendar> {

    /**
     * Default constructor.
     */
    public CalendarValueConverter() {
        super(Calendar.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Calendar convertValue(@NonNull Value value)
            throws ValueConversionException {
        try {
            // Use the standard XMLGregorianCalendar object.
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(value.stringValue()).toGregorianCalendar();
        } catch (DatatypeConfigurationException e) {
            throw new ValueConversionException("Environment issue: Cannot instantiate XML Gregorian Calendar data.", e);
        } catch (IllegalArgumentException e) {
            throw new ValueConversionException("Issue converting value of statement into a date object.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value convertType(@NonNull Calendar type) throws ValueConversionException {
        try {
            final GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTimeInMillis(type.getTimeInMillis());
            return this.valueFactory.createLiteral(
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal).toXMLFormat(), CoreDatatype.XSD.DATETIME);
        } catch (Exception e) {
            throw new ValueConversionException("Issue converting calendar into Value", e);
        }
    }
}
