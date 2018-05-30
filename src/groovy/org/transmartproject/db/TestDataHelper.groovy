/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transmartproject.db

/**
 * Helper class for dealing with test data.
 */
class TestDataHelper {

	/**
	 * Fills the object with dummy values for all the fields that are mandatory (nullable = false) and have no value
	 */
	static void completeObject(obj) {
		List<MetaProperty> fields = getMandatoryProps(obj.getClass()).findAll { it.getProperty(obj) == null } //all without value
		for (MetaProperty f in fields) {
			f.setProperty obj, getDummyObject(f.type)
		}
	}

	private static getDummyObject(Class type) {
		switch (type) {
			case String: return ''
			case Character: return ''
			case Integer: return 0
			case Date: return new Date()
			default: throw new UnsupportedOperationException('Not supported: ' + type.name + '. Care to add it?')
		}
	}

	private static List<MetaProperty> getMandatoryProps(Class clazz) {
		def mandatory = clazz.constraints?.findAll { !it.value.nullable } //get all not nullable properties
		clazz.metaClass.properties.findAll { mandatory.containsKey it.name }
	}

	static List<String> getMissingValueFields(obj, Collection<String> fields) {
		List<MetaProperty> props = obj.getClass().metaClass.properties.findAll { fields.contains(it.name) }
		props.findAll({ !it.getProperty(obj) }).collect({ it.name })
	}
}
