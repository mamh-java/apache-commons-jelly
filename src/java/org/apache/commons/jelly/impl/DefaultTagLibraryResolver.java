/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 * 
 */
package org.apache.commons.jelly.impl;

import org.apache.commons.discovery.ServiceDiscovery;
import org.apache.commons.discovery.ServiceInfo;

import org.apache.commons.jelly.TagLibrary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p><code>DefaultTagLibraryResolver</code> is a default implemenation
 * which attempts to interpret the URI as a String called 'jelly:className'
 * and class load the given Java class. Otherwise META-INF/services/jelly/uri 
 * is searched for on the thread context's class path and, if found, that
 * class will be loaded.</p>
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision: 1.12 $
 */
public class DefaultTagLibraryResolver implements TagLibraryResolver {

    /** The Log to which logging calls will be made. */
    private static final Log log = LogFactory.getLog(DefaultTagLibraryResolver.class);

    private ServiceDiscovery discovery;
    
    /**
     * The class loader to use for instantiating application objects.
     * If not specified, the context class loader, or the class loader
     * used to load this class itself, is used, based on the value of the
     * <code>useContextClassLoader</code> variable.
     */
    private ClassLoader classLoader;

    /**
     * Do we want to use the Context ClassLoader when loading classes
     * for instantiating new objects?  Default is <code>false</code>.
     */
    private boolean useContextClassLoader = false;
    
    
    public DefaultTagLibraryResolver() {
    }

    
    // TagLibraryResolver interface
    //-------------------------------------------------------------------------                
    
    /**
     * Attempts to resolve the given URI to be associated with a TagLibrary
     * otherwise null is returned to indicate no tag library could be found
     * so that the namespace URI should be treated as just vanilla XML.
     */    
    public TagLibrary resolveTagLibrary(String uri) {
        ServiceDiscovery discovery = getServiceDiscovery();
        String name = uri;
        if ( uri.startsWith( "jelly:" ) ) {
            name = "jelly." + uri.substring(6);
        }
        
        log.info( "Looking up service name: " + name );
        
        ServiceInfo[] infoArray = discovery.findServices(name);
        
        if ( infoArray != null && infoArray.length > 0 ) {
            for (int i = 0; i < infoArray.length; i++ ) {
                ServiceInfo info = infoArray[i];
                try {                
                    Class typeClass = info.getLoader().loadClass( info.getImplName() );
                    if ( typeClass != null ) {
                        return newInstance(uri, typeClass);
                    }
                }
                catch (Exception e) {
                    log.error( "Could not load service: " + info.getImplName()
                       + " with loader: " + info.getLoader() 
                   );
                }
            }
        }
        else {
            log.info( "Could not find any services for name: " + name );
        }
        return null;
    }
    
    // Properties
    //-------------------------------------------------------------------------                
    
    /**
     * Return the class loader to be used for instantiating application objects
     * when required.  This is determined based upon the following rules:
     * <ul>
     * <li>The class loader set by <code>setClassLoader()</code>, if any</li>
     * <li>The thread context class loader, if it exists and the
     *     <code>useContextClassLoader</code> property is set to true</li>
     * <li>The class loader used to load the XMLParser class itself.
     * </ul>
     */
    public ClassLoader getClassLoader() {
        if (this.classLoader != null) {
            return (this.classLoader);
        }
        if (this.useContextClassLoader) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader != null) {
                return (classLoader);
            }
        }
        return (this.getClass().getClassLoader());
    }
    
    /**
     * Set the class loader to be used for instantiating application objects
     * when required.
     *
     * @param classLoader The new class loader to use, or <code>null</code>
     *  to revert to the standard rules
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    
    /**
     * Return the boolean as to whether the context classloader should be used.
     */
    public boolean getUseContextClassLoader() {
        return useContextClassLoader;
    }

    /**
     * Determine whether to use the Context ClassLoader (the one found by
     * calling <code>Thread.currentThread().getContextClassLoader()</code>)
     * to resolve/load classes.  If not
     * using Context ClassLoader, then the class-loading defaults to
     * using the calling-class' ClassLoader.
     *
     * @param boolean determines whether to use JellyContext ClassLoader.
     */
    public void setUseContextClassLoader(boolean use) {
        useContextClassLoader = use;
    }

    /**
     * @return the ServiceDiscovery instance to use to locate services.
     *  This object is lazily created if it has not been configured.
     */
    public ServiceDiscovery getServiceDiscovery() {
        if ( discovery == null ) {
            discovery = ServiceDiscovery.getServiceDiscovery();
            discovery.addClassLoader( getClassLoader() );
        }
        return discovery;
    }
    
    /**
     * Sets the fully configured ServiceDiscovery instance to be used to 
     * lookup services
     */
    public void setServiceDiscovery(ServiceDiscovery discovery) {
        this.discovery = discovery;
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------                

    /**
     * Instantiates the given class name. Otherwise an exception is logged 
     * and null is returned
     */
    protected TagLibrary loadClass(String uri, String className) {
        try {
            Class theClass = getClassLoader().loadClass(className);
            if ( theClass != null ) {
                return newInstance(uri, theClass);
            }
        }
        catch (ClassNotFoundException e) {
            log.error("Could not find the class: " + className + " when trying to resolve URI: " + uri, e);
        }
        return null;
    }
            

    /**
     * Creates a new instance of the given TagLibrary class or
     * return null if it could not be instantiated.
     */
    protected TagLibrary newInstance(String uri, Class theClass) {
        try {
            Object object = theClass.newInstance();
            if (object instanceof TagLibrary) {
                return (TagLibrary) object;
            }                
            else {
                log.error(
                    "The tag library object mapped to: "
                        + uri
                        + " is not a TagLibrary. Object = "
                        + object);
            }
        }
        catch (Exception e) {
            log.error(
                "Could not instantiate instance of class: " + theClass.getName() + ". Reason: " + e,
                e);
        }
        return null;
    }
    
}