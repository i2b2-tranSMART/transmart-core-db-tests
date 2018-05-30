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

package org.thehyve.commons.test

import org.hamcrest.Matcher
import org.springframework.util.Assert

import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.hasEntry
import static org.hamcrest.Matchers.hasProperty
import static org.hamcrest.Matchers.is

/**
 * Methods to create hamcrest matchers from Maps and Lists, all of them recursing into the objects where needed.
 * This makes it easier to create matchers from simple structures (Maps and Lists) that represent the expected values,
 * and wraps all the elements with the correct Matcher to avoid the pitfalls of some groovy conversions that are not
 * recognized in hamcrest.
 * In general terms:
 *  - Map is promoted to a mapWith()
 *  - List is promoted to a listOf()
 *  - Object is promoted to is()
 *  Where a strict order list is required, we should wrap that element with listOfWithOrder().
 *  The recursion will promote whatever it can to Matcher, but will leave the existing Matchers.
 */
class FastMatchers {

	/**
	 * @return allOf(hasEntry()) Matcher for the given map
	 */
	static Matcher mapWith(Map map) {
		allOf(map.collect { entryOf(it.key, it.value) })
	}

	/**
	 * Creates an entryOf matcher, recursing and promoting the value (and key) when needed
	 */
	private static Matcher entryOf(Object key, Object value) {
		if (value instanceof Matcher) {
			hasEntry matcherOf(key), value
		}
		else if (value instanceof Map) {
			hasEntry matcherOf(key), mapWith(value)
		}
		else if (value instanceof List) {
			hasEntry matcherOf(key), listOf(value)
		}
		else {
			//if both key and value are 'regular' objects, then will match them directly (not wrapping with is())
			hasEntry fix(key), fix(value)
		}
	}

	/**
	 * @return allOf(hasProperty()) for the given map
	 */
	static Matcher propsWith(Map map) {
		allOf(map.collect { hasProperty((String) it.key, matcherOf(it.value)) })
	}

	/**
	 * @return containsInAnyOrder() for the given list
	 */
	static Matcher listOf(List list) {
		Assert.notEmpty list, 'List cannot be empty'

		containsInAnyOrder list.collect { matcherOf(it) }
	}

	/**
	 * @return contains() for the given list
	 */
	static Matcher listOfWithOrder(List list) {
		Assert.notEmpty list, 'List cannot be empty'

		contains list.collect { matcherOf(it) }
	}

	/**
	 * Generic entry point to promote an object to a Matcher
	 * @return Matcher for the given object, or itself (if is already a Matcher)
	 */
	private static Matcher matcherOf(obj) {
		if (obj instanceof Matcher) {
			(Matcher) obj //already a matcher
		}
		else if (obj instanceof Map) {
			mapWith obj //promoted to a mapWith
		}
		else if (obj instanceof List) {
			listOf obj //promoted to a listOf
		}
		else {
			is fix(obj) //promoted to a is, with some check
		}
	}

	/**
	 * Fixes (if necessary) the value to avoid some groovy/java interoperability pitfalls
	 */
	private static fix(obj) {
		obj instanceof GString ? obj.toString() : obj
	}
}
