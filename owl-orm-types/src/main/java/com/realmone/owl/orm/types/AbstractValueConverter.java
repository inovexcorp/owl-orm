package com.realmone.owl.orm.types;

/*-
 * #%L
 * RDF ORM
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2016 - 2023 iNovex Information Systems, Inc.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import lombok.Getter;
import lombok.NonNull;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValidatingValueFactory;

/**
 * This is an {@link AbstractValueConverter} for implementations to extend.
 * Basically just provides the type-methods for implementations.
 *
 * @param <T> The type of {@link ValueConverter} your extension is
 * @author bdgould
 */
public abstract class AbstractValueConverter<T> implements ValueConverter<T> {

    /**
     * The type this {@link ValueConverter} will produce.
     */
    @Getter
    protected final Class<T> type;
    /**
     * A {@link ValueFactory} instance to use by default for doing conversion.
     */
    protected final ValueFactory valueFactory = new ValidatingValueFactory();

    /**
     * Construct a new {@link AbstractValueConverter}.
     *
     * @param type The type of object this {@link ValueConverter} will produce
     */
    protected AbstractValueConverter(@NonNull final Class<T> type) {
        this.type = type;
    }
}
