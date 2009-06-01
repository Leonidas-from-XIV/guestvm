/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 * 
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties.
 * 
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 * 
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 * 
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 * 
 */
package com.sun.guestvm.sched;

/**
 * Interface for objects that can be linked into a @See RingRunQueue.
 * This interface requires navigation both forwards and backwards
 * from a given entry, i.e., getNext and getPrev. (aka a doubly-linked list).
 *
 * @author Harald Roeck
 *
 */
public interface RingRunQueueEntry {

    /**
     * Return the next element in the list.
     *
     * @return the next element in the list
     */
    RingRunQueueEntry getNext();

    /**
     * Set next element in the list to given element.
     * @param element
     */
    void setNext(RingRunQueueEntry element);

    /**
     * Return previous element in the list.
     * @return previous element in the list
     */
    RingRunQueueEntry getPrev();

    /**
     * Set previous element in the list to the given element.
     * @param element
     */
    void setPrev(RingRunQueueEntry element);

}
