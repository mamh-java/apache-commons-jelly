/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jelly/src/java/org/apache/commons/jelly/tags/xml/Attic/XMLTagLibrary.java,v 1.16 2002/11/27 12:43:19 jstrachan Exp $
 * $Revision: 1.16 $
 * $Date: 2002/11/27 12:43:19 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * $Id: XMLTagLibrary.java,v 1.16 2002/11/27 12:43:19 jstrachan Exp $
 */
package org.apache.commons.jelly.tags.xml;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.impl.TagScript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom4j.Dom4jXPath;

/** Describes the Taglib. This class could be generated by XDoclet
  *
  * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
  * @version $Revision: 1.16 $
  */
public class XMLTagLibrary extends TagLibrary {

    /** The Log to which logging calls will be made. */
    private Log log = LogFactory.getLog(XMLTagLibrary.class);

    public XMLTagLibrary() {
        registerTag("out", ExprTag.class);
        registerTag("if", IfTag.class);
        registerTag("forEach", ForEachTag.class);
        registerTag("parse", ParseTag.class);
        registerTag("set", SetTag.class);
        registerTag("transform", TransformTag.class);
        registerTag("param", ParamTag.class);

        // extensions to JSTL
        registerTag("expr", ExprTag.class);
        registerTag("element", ElementTag.class);
        registerTag("attribute", AttributeTag.class);
        registerTag("copy", CopyTag.class);
        registerTag("copyOf", CopyOfTag.class);
        registerTag("doctype", DoctypeTag.class);
    }

    public Expression createExpression(
        ExpressionFactory factory,
        TagScript tagScript,
        String attributeName,
        String attributeValue) throws Exception {

        // #### may need to include some namespace URI information in the XPath instance?

        if (attributeName.equals("select")) {
            if ( log.isDebugEnabled() ) {
                log.debug( "Parsing XPath expression: " + attributeValue );
            }

            try {
                XPath xpath = new Dom4jXPath(attributeValue);
                return new XPathExpression(attributeValue, xpath, tagScript);
            }
            catch (JaxenException e) {
                throw new JellyException( "Could not parse XPath expression: \"" + attributeValue + "\" reason: " + e, e );
            }
        }

        // will use the default expression instead
        return super.createExpression(factory, tagScript, attributeName, attributeValue);
    }
}
