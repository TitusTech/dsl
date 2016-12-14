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
package org.cristalise.dsl.test.persistency.outcome

import org.cristalise.dsl.persistency.outcome.Field
import org.cristalise.dsl.persistency.outcome.SchemaBuilder
import org.cristalise.dsl.test.builders.SchemaTestBuilder
import org.cristalise.kernel.common.InvalidDataException
import org.cristalise.kernel.test.utils.CristalTestSetup

import spock.lang.Specification


/**
 *
 */
class SchemaBuilderSpecs extends Specification implements CristalTestSetup {

    def setup()   { loggerSetup()    }
    def cleanup() { cristalCleanup() }


    def 'Schema can be built from XSD file'() {
        expect:
        SchemaBuilder.build("Test", "TestData", 0, "src/test/data/TestData.xsd").schema.som.isValid()
    }


    def 'Empty specification throws InvalidDataException'() {
        when:
        SchemaBuilder.build('Test', 'TestData', 0) {}.schema.som.isValid()

        then:
        thrown(InvalidDataException)
    }


    def 'Empty named Structure builds a valid Schema'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData')
        }
        .compareXML("""<?xml version='1.0' encoding='utf-8'?>
                       <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                         <xs:element name='TestData'>
                           <xs:complexType>
                             <xs:sequence />
                           </xs:complexType>
                         </xs:element>
                       </xs:schema>""")
    }


    def 'Building empty Structure with documentation adds xs:annotation to the xs:element'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData', documentation: "Test data documentation")
        }
        .compareXML("""<?xml version='1.0' encoding='utf-8'?>
                       <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                         <xs:element name='TestData'>
                           <xs:annotation>
                             <xs:documentation>Test data documentation</xs:documentation>
                           </xs:annotation>
                           <xs:complexType>
                             <xs:sequence />
                           </xs:complexType>
                         </xs:element>
                       </xs:schema>""")
    }


    def 'Default type is string for Fields'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') { 
                field(name:'stringField')
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name='stringField' type='xs:string' minOccurs='1' maxOccurs='1' />
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }


    def 'Field only accepts a number of types: string, boolean, integer, decimal, date, time, dateTime'() {
        expect: "Accepted types are ${org.cristalise.dsl.persistency.outcome.Field.types}"
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') {
                Field.types.each {
                    field(name:"${it}Field", type: it)
                }
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name='stringField'   type='xs:string'   minOccurs='1' maxOccurs='1' />
                                        <xs:element name='booleanField'  type='xs:boolean'  minOccurs='1' maxOccurs='1' />
                                        <xs:element name='integerField'  type='xs:integer'  minOccurs='1' maxOccurs='1' />
                                        <xs:element name='decimalField'  type='xs:decimal'  minOccurs='1' maxOccurs='1' />
                                        <xs:element name='dateField'     type='xs:date'     minOccurs='1' maxOccurs='1' />
                                        <xs:element name='timeField'     type='xs:time'     minOccurs='1' maxOccurs='1' />
                                        <xs:element name='dateTimeField' type='xs:dateTime' minOccurs='1' maxOccurs='1' />
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }


    def 'Unknown field type throws InvalidDataException'() {
        when: "Accepted types are ${org.cristalise.dsl.persistency.outcome.Field.types}"
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') { 
                field(name: 'byteField', type: 'byte')
            }
        }

        then:
        thrown(InvalidDataException)
    }

    def 'Field can specify multiplicity'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') {
                field(name:'default')
                field(name:'many',        multiplicity:'*')
                field(name:'fivehundred', multiplicity:'500')
                field(name:'zeroToMany',  multiplicity:'0..*')
                field(name:'oneToFive',   multiplicity:'1..5')
                field(name:'reset',       multiplicity:'')
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name='default'     type='xs:string' minOccurs='1' maxOccurs='1' />
                                        <xs:element name='many'        type='xs:string' minOccurs='0' />
                                        <xs:element name='fivehundred' type='xs:string' minOccurs='500' maxOccurs='500' />
                                        <xs:element name='zeroToMany'  type='xs:string' minOccurs='0' />
                                        <xs:element name='oneToFive'   type='xs:string' minOccurs='1' maxOccurs='5' />
                                        <xs:element name='reset'       type='xs:string' />
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }

    def 'Field can have a predefined set of values'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') {
                field(name: 'Gender', type: 'string', values: ['male', 'female'])
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element minOccurs="1" maxOccurs="1" name="Gender">
                                            <xs:simpleType>
                                                <xs:restriction base="xs:string">
                                                   <xs:enumeration value="male" />
                                                   <xs:enumeration value="female" />
                                                </xs:restriction>
                                            </xs:simpleType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }


    def 'Field can have Unit which is added as attribute of type string'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') {
                field(name: 'Weight', type: 'decimal') { unit() }
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name='Weight' minOccurs='1' maxOccurs='1'>
                                            <xs:complexType>
                                                <xs:simpleContent>
                                                    <xs:extension base='xs:decimal'>
                                                        <xs:attribute name='unit' type='xs:string' use='required'/>
                                                    </xs:extension>
                                                </xs:simpleContent>
                                            </xs:complexType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }


    def 'Unit can specify the list of values it contains'() {
        expect:
        SchemaTestBuilder.build('Test', 'TestData', 0) {
            struct(name: 'TestData') {
                field(name: 'Weight', type: 'decimal') { unit(values: ['g', 'kg']) }
            }
        }.compareXML("""<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                            <xs:element name='TestData'>
                                <xs:complexType>
                                    <xs:sequence>
                                        <xs:element name='Weight' minOccurs='1' maxOccurs='1'>
                                            <xs:complexType>
                                                <xs:simpleContent>
                                                    <xs:extension base='xs:decimal'>
                                                        <xs:attribute name='unit' use='required'>
                                                            <xs:simpleType>
                                                                <xs:restriction base="xs:string">
                                                                   <xs:enumeration value="g" />
                                                                   <xs:enumeration value="kg" />
                                                                </xs:restriction>
                                                            </xs:simpleType>
                                                        </xs:attribute>
                                                    </xs:extension>
                                                </xs:simpleContent>
                                            </xs:complexType>
                                        </xs:element>
                                    </xs:sequence>
                                </xs:complexType>
                            </xs:element>
                        </xs:schema>""")
    }


    def 'Complex example using PatientDetails from Basic Tutorial'() {
        expect:
        SchemaTestBuilder.build('test', 'PatientDetails', 0) {
            struct(name: 'PatientDetails', documentation: 'This is the Schema for Basic Tutorial') {
                field(name: 'InsuranceNumber', type: 'string', default: '123456789ABC')
                field(name: 'DateOfBirth',     type: 'date')
                field(name: 'Gender',          type: 'string', values: ['male', 'female'])
                field(name: 'Weight',          type: 'decimal') { unit(values: ['g', 'kg'], default: 'kg') }
            }
        }.compareXML(
            """<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                 <xs:element name="PatientDetails">
                   <xs:annotation>
                     <xs:documentation>This is the Schema for Basic Tutorial</xs:documentation>
                   </xs:annotation>
                   <xs:complexType>
                   <xs:sequence>
                     <xs:element minOccurs="1" maxOccurs="1" name="InsuranceNumber" type="xs:string" default= "123456789ABC"/>
                     <xs:element minOccurs="1" maxOccurs="1" name="DateOfBirth" type="xs:date"/>
                     <xs:element minOccurs="1" maxOccurs="1" name="Gender">
                       <xs:simpleType>
                         <xs:restriction base="xs:string">
                           <xs:enumeration value="male" />
                           <xs:enumeration value="female" />
                         </xs:restriction>
                       </xs:simpleType>
                     </xs:element>
                     <xs:element name='Weight' minOccurs='1' maxOccurs='1'>
                       <xs:complexType>
                         <xs:simpleContent>
                           <xs:extension base='xs:decimal'>
                             <xs:attribute name='unit' default='kg' use='optional'>
                               <xs:simpleType>
                                 <xs:restriction base='xs:string'>
                                   <xs:enumeration value='g' />
                                   <xs:enumeration value='kg' />
                                 </xs:restriction>
                               </xs:simpleType>
                             </xs:attribute>
                           </xs:extension>
                         </xs:simpleContent>
                       </xs:complexType>
                     </xs:element>
                   </xs:sequence>
                 </xs:complexType>
               </xs:element>
             </xs:schema>""")
    }

    def 'Commodity'() {
        expect:
        SchemaTestBuilder.build('test', 'Commodity', 0) {
            struct(name: 'Commodity') {
                field(name: 'code',           type: 'string')
                field(name: 'name',           type: 'string')
                field(name: 'color',          type: 'string',  multiplicity:'0..1')
            }
        }.compareXML(
            """<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
                 <xs:element name="Commodity">
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element minOccurs="1" maxOccurs="1" name="code" type="xs:string"/>
                       <xs:element minOccurs="1" maxOccurs="1" name="name" type="xs:string"/>
                       <xs:element minOccurs="0" maxOccurs="1" name="color" type="xs:string"/>
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Grade'() {
        expect:
        SchemaTestBuilder.build('test', 'Grade', 0) {
            struct(name: 'Grade') {
                field(name: 'code',           type: 'string')  // cristal "name"
                field(name: 'name',           type: 'string')
                field(name: 'color',          type: 'string',  multiplicity:'0..1')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Grade'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='code' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='color' type='xs:string' minOccurs='0' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Season'() {
        expect:
        SchemaTestBuilder.build('test', 'Season', 0) {
            struct(name: 'Season') {
                field(name: 'name',           type: 'string')
                field(name: 'code',           type: 'string')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Season'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='code' type='xs:string' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Test'() {
        expect:
        SchemaTestBuilder.build('test', 'Test', 0) {
            struct(name: 'Test') {
                field(name: 'name',           type: 'string')
                field(name: 'unit',           type: 'string')
                field(name: 'min',            type: 'decimal')
                field(name: 'max',            type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Test'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='unit' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='min' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='max' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Treatment'() {
        expect:
        SchemaTestBuilder.build('test', 'Treatment', 0) {
            struct(name: 'Treatment') {
                field(name: 'name',           type: 'string')
                field(name: 'type',           type: 'string', values: ['Pesticide', 'Other'])
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Treatment'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='type' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Pesticide' />
                             <xs:enumeration value='Other' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }
    
    def 'Customer'() {
        expect:
        SchemaTestBuilder.build('test', 'Customer', 0) {
            struct(name: 'Customer') {
                field(name: 'name',           type: 'string')
                field(name: 'shortName',      type: 'string')
                field(name: 'address',        type: 'string')
                field(name: 'phone',          type: 'string')
                field(name: 'fax',            type: 'string')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Customer'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='shortName' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='address' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='phone' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='fax' type='xs:string' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Weighbridge'() {
        expect:
        SchemaTestBuilder.build('test', 'Weighbridge', 0) {
            struct(name: 'Weighbridge') {
                field(name: 'name',        type: 'string')
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Weighbridge'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Panel'() {
        expect:
        SchemaTestBuilder.build('test', 'Panel', 0) {
            struct(name: 'Panel') {
                field(name: 'name',           type: 'string')
                field(name: 'orientation',    type: 'string', values: ['Horizontal', 'Vertical'])
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Panel'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='orientation' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Horizontal' />
                             <xs:enumeration value='Vertical' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }

    def 'Storage'() {
        expect:
        assert SchemaTestBuilder.build('test', 'Storage', 0) {
            struct(name: 'Storage') {
                field(name: 'name',           type: 'string')
                field(name: 'type',           type: 'string', values: ['Bin', 'Warehouse'])
                field(name: 'capacity',       type: 'decimal')
                field(name: 'note',           type: 'string')
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.getSchema().schemaData == """<?xml version='1.0' encoding='utf-8'?>
               <xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Storage'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='type' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Bin' />
                             <xs:enumeration value='Warehouse' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                       <xs:element name='capacity' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='note' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""".replaceAll("\n               ", "\n")
    }
    
    def 'Sparkline'() {
        expect:
        SchemaTestBuilder.build('test', 'Sparkline', 0) {
            struct(name: 'Sparkline') {
                field(name: 'name',           type: 'string')
                field(name: 'format',         type: 'string')
                field(name: 'unit',           type: 'string')
                field(name: 'valueQuery',     type: 'string')
                field(name: 'chartQuery',     type: 'string')
                field(name: 'highQuery',      type: 'string')
                field(name: 'lowQuery',       type: 'string')
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='Sparkline'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='format' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='unit' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='valueQuery' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='chartQuery' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='highQuery' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='lowQuery' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>
""")
    }

    def 'DashboardPanel'() {
        expect:
        SchemaTestBuilder.build('test', 'DashboardPanel', 0) {
            struct(name: 'DashboardPanel') {
                field(name: 'name',           type: 'string')
                field(name: 'type',           type: 'string', values: ['Chart', 'TreeTable', 'Grid', 'TextArea'])
                field(name: 'query',          type: 'string')
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='DashboardPanel'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='type' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Chart' />
                             <xs:enumeration value='TreeTable' />
                             <xs:enumeration value='Grid' />
                             <xs:enumeration value='TextArea' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                       <xs:element name='query' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }
/*
    def 'Transaction'() {
        expect:
        SchemaTestBuilder.build('test', 'Transaction', 0) {
            struct(name: 'Transaction') {
                field(name: 'name',           type: 'string')
                field(name: 'type',           type: 'string', values: ['Chart', 'TreeTable', 'Grid', 'TextArea'])
                field(name: 'query',          type: 'string')
                field(name: 'posx',           type: 'decimal')
                field(name: 'posy',           type: 'decimal')
                field(name: 'width',          type: 'decimal')
                field(name: 'height',         type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='DashboardPanel'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='type' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Chart' />
                             <xs:enumeration value='TreeTable' />
                             <xs:enumeration value='Grid' />
                             <xs:enumeration value='TextArea' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                       <xs:element name='query' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }
    def 'Order'() {
        expect:
        SchemaTestBuilder.build('test', 'Order', 0) {
            struct(name: 'Order') {
                field(name: 'grade',          type: 'Grade')
                field(name: 'season',         type: 'Season')
                field(name: 'direction',      type: 'string', values: ['Intake', 'Outtake'])
                field(name: 'customer',       type: 'Customer')
                field(name: 'quantity',       type: 'decimal')
            }
        }.compareXML(
            """<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>
                 <xs:element name='DashboardPanel'>
                   <xs:complexType>
                     <xs:sequence>
                       <xs:element name='name' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='type' minOccurs='1' maxOccurs='1'>
                         <xs:simpleType>
                           <xs:restriction base='xs:string'>
                             <xs:enumeration value='Chart' />
                             <xs:enumeration value='TreeTable' />
                             <xs:enumeration value='Grid' />
                             <xs:enumeration value='TextArea' />
                           </xs:restriction>
                         </xs:simpleType>
                       </xs:element>
                       <xs:element name='query' type='xs:string' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posx' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='posy' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='width' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                       <xs:element name='height' type='xs:decimal' minOccurs='1' maxOccurs='1' />
                     </xs:sequence>
                   </xs:complexType>
                 </xs:element>
               </xs:schema>""")
    }
*/
}
