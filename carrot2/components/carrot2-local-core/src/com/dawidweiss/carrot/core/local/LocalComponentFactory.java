
/*
 * Carrot2 Project
 * Copyright (C) 2002-2005, Dawid Weiss
 * Portions (C) Contributors listed in carrot2.CONTRIBUTORS file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the CVS checkout or at:
 * http://www.cs.put.poznan.pl/dweiss/carrot2.LICENSE
 *
 * Sponsored by: CCG, Inc.
 */

package com.dawidweiss.carrot.core.local;

/**
 * Objects implementing this interface can produce new instances of {@link
 * LocalComponent} objects. The instances produced by a factory can be pooled
 * and reused by the component container.
 *
 * @author Dawid Weiss
 *
 * @see LocalComponent
 */
public interface LocalComponentFactory {
    /**
     * Produces a new instance of a component implementing {@link
     * LocalComponent} interface.
     *
     * @return A new instance of a component. The method should never return
     *         <code>null</code>. In case of any error, an unchecked exception
     *         should be thrown.
     */
    public LocalComponent getInstance();
    
    
    /**
     * @return returns a name for components created by this factory, or
     * <code>null</code> if name is not available.
     */
    public String getName();
 
    /**
     * @return returns a description for components created by this factory
     * or <code>null</code> if description is not available.
     */
    public String getDescription();
}
