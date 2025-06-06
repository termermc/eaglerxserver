/*
 * HPPC
 *
 * Copyright (C) 2010-2024 Carrot Search s.c. and contributors
 * All rights reserved.
 *
 * Refer to the full license file "LICENSE.txt":
 * https://github.com/carrotsearch/hppc/blob/master/LICENSE.txt
 */
package net.lax1dude.eaglercraft.backend.server.api.collect;

import java.util.Iterator;

/** A generic container holding <code>Object</code>s. */
public interface ObjectContainer<KType> extends Iterable<ObjectCursor<KType>> {
	/**
	 * Returns an iterator to a cursor traversing the collection. The order of
	 * traversal is not defined. More than one cursor may be active at a time. The
	 * behavior of iterators is undefined if structural changes are made to the
	 * underlying collection.
	 *
	 * <p>
	 * The iterator is implemented as a cursor and it returns <b>the same cursor
	 * instance</b> on every call to {@link Iterator#next()} (to avoid boxing of
	 * primitive types). To read the current list's value (or index in the list) use
	 * the cursor's public fields. An example is shown below.
	 *
	 * <pre>
	 * for (ObjectCursor&lt;Object&gt; c : container) {
	 * 	System.out.println(&quot;index=&quot; + c.index + &quot; value=&quot; + c.value);
	 * }
	 * </pre>
	 */
	public Iterator<ObjectCursor<KType>> iterator();

	/**
	 * Lookup a given element in the container. This operation has no speed
	 * guarantees (may be linear with respect to the size of this container).
	 *
	 * @return Returns <code>true</code> if this container has an element equal to
	 *         <code>e</code>.
	 */
	public boolean contains(KType e);

	/**
	 * Return the current number of elements in this container. The time for
	 * calculating the container's size may take <code>O(n)</code> time, although
	 * implementing classes should try to maintain the current size and return in
	 * constant time.
	 */
	public int size();

	/** Shortcut for <code>size() == 0</code>. */
	public boolean isEmpty();

	/**
	 * Copies all elements of this container to an array.
	 *
	 * <p>
	 * The returned array is always a copy, regardless of the storage used by the
	 * container.
	 */
	public Object[] toArray();

	/**
	 * Copies all elements of this container to a dynamically created array of the
	 * given component type.
	 *
	 * @throws ArrayStoreException Thrown if this container's elements are not
	 *                             assignable to the array's component type.
	 */
	public <T> T[] toArray(Class<T> componentClass);

	/**
	 * Applies a <code>procedure</code> to all container elements. Returns the
	 * argument (any subclass of {@link ObjectProcedure}. This lets the caller to
	 * call methods of the argument by chaining the call (even if the argument is an
	 * anonymous type) to retrieve computed values, for example (IntContainer):
	 *
	 * <pre>
	 * int count = container.forEach(new IntProcedure() {
	 * 	int count; // this is a field declaration in an anonymous class.
	 *
	 * 	public void apply(int value) {
	 * 		count++;
	 * 	}
	 * }).count;
	 * </pre>
	 */
	public <T extends ObjectProcedure<? super KType>> T forEach(T procedure);

	/**
	 * Applies a <code>predicate</code> to container elements as long, as the
	 * predicate returns <code>
	 * true</code>. The iteration is interrupted otherwise.
	 */
	public <T extends ObjectPredicate<? super KType>> T forEach(T predicate);
}
