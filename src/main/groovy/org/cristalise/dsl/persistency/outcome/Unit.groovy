/**
 * This file is part of the CRISTAL-iSE kernel.
 * Copyright (c) 2001-2015 The CRISTAL Consortium. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package org.cristalise.dsl.persistency.outcome

import org.cristalise.kernel.common.InvalidDataException


/**
 *
 */
class Unit {
    List<String> values = []
    String defaultVal
    boolean required = true

    /**
     * 'default' is a keyword, so it cannot be used as a variable name, 
     * but this method makes the default keyword usable in the SchemaBuilder DSL
     * 
     * @param val
     */
    public void setDefault(String val) {
        if(values && !values.contains(val)) throw new InvalidDataException("Default value '$val' is wrong, it must be one of these: $values")
        
        defaultVal = val
    }
}
