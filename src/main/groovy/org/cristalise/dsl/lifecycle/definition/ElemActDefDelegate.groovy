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
package org.cristalise.dsl.lifecycle.definition

import groovy.transform.CompileStatic

import org.cristalise.dsl.property.PropertyDelegate
import org.cristalise.kernel.lifecycle.ActivityDef


/**
 * Wrapper/Delegate class of Elementary Activity definition
 *
 */
@CompileStatic
class ElemActDefDelegate extends PropertyDelegate {

    ActivityDef elemActDef

    public void processClosure(String name, int version, Closure cl) {
        assert cl, "ElemActDefDelegate only works with a valid Closure"

        elemActDef = new ActivityDef()
        elemActDef.name = name
        elemActDef.version = version

        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()

        props.each { k, v ->
            elemActDef.properties.put(k, v, props.getAbstract().contains(k) ? true: false)
        }
    }
}