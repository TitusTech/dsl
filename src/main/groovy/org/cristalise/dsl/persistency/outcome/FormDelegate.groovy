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

import groovy.xml.MarkupBuilder

import org.cristalise.kernel.common.InvalidDataException
import org.cristalise.kernel.utils.Logger


class FormDelegate {

	String html

	public void processClosure(Closure cl) {
		assert cl, "Form can only be generated from a valid Closure"

		Logger.msg 1, "Form(start) ---------------------------------------"

		def objBuilder = new ObjectGraphBuilder()
		objBuilder.classLoader = this.class.classLoader
		objBuilder.classNameResolver = 'org.cristalise.dsl.persistency.outcome'

		cl.delegate = objBuilder

		html = buildHtml( cl() )

		Logger.msg 1, "Form(end) +++++++++++++++++++++++++++++++++++++++++"
	}

	public String buildHtml(Struct s) {
		if ( ! s ) throw new InvalidDataException("Form cannot be built from empty declaration")
		
		def writer = new StringWriter()
		def builder = new MarkupBuilder(writer)

		builder.setOmitEmptyAttributes(true)
		builder.setOmitNullAttributes(true)

		builder.mkp.xmlDeclaration(version: "1.0", encoding: "utf-8")

		builder.'xs:schema'('xmlns:xs': 'http://www.w3.org/2001/XMLSchema') {
			buildStruct(builder, s)
		}

		return writer.toString()
	}

	private void buildStruct(builder, Struct s) {
		Logger.msg 1, "FormDelegate.buildStruct() - Struct: $s.name"

		builder.'xs:element'(name: s.name) {

			if(s.documentation) 'xs:annotation' { 'xs:documentation'(s.documentation) }

			'xs:complexType' {
				'xs:sequence' {
					s.fields.each { Field f -> buildField(builder, f) }
				}
			}
		}
	}

	private void buildField(builder, Field f) {
		Logger.msg 1, "FormDelegate.buildField() - Field: $f.name"

		builder.'xs:element'(name: f.name, type: (!f.values && !f.unit ? f.type : ''), 'default': f.defaultVal, minOccurs: f.minOccurs, maxOccurs: f.maxOccurs) {
			if(f.unit) {
				'xs:complexType' {
					'xs:simpleContent' {
						'xs:extension'(base: f.type) {
							'xs:attribute'(name:"unit", type: (!f.unit.values ? 'xs:string' : ''), 'default': f.unit.defaultVal, 'use': (f.unit.required && f.unit.defaultVal ? "optional": "required")) {
								if(f.unit.values) {
									buildRestriction(builder, 'xs:string', f.unit.values)
								}
							}
						}
					}
				}
			}
			else if(f.values) {
				buildRestriction(builder, f.type, f.values)
			}
		}
	}

	private void buildRestriction(builder, String type, List values) {
		Logger.msg 1, "FormDelegate.buildRestriction() - type:$type, values: $values"

		builder.'xs:simpleType' {
			'xs:restriction'(base: type) {
				values.each {
					'xs:enumeration'(value: it)
				}
			}
		}
	}
}
