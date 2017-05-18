/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.expression;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;

/**
 * Tests the use of Expression parsing
 *
 * @author <a href="mailto:jstrachan@apache.org">James Strachan</a>
 * @version $Revision$
 */
public class TestExpressions extends TestCase {

    protected JellyContext context = new JellyContext();
    protected ExpressionFactory factory = new JexlExpressionFactory();

    private static final class TestException extends Exception {
        public TestException() {
            super("Test Exception");
        }
    }
    public static final class TestHelper {
        public Object throwAnException() throws TestException {
            throw new TestException();
        }
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TestExpressions.class);
    }

    public TestExpressions(String testName) {
        super(testName);
    }

    public void testExpresssions() throws Exception {
        context.setVariable("topping", "cheese");
        context.setVariable("type", "deepPan");


        assertExpression("foo", "foo");
        assertExpression("${topping}", "cheese");
        assertExpression("some${topping}", "somecheese");
        assertExpression(" some ${topping} ", " some cheese ");
        assertExpression("${topping}y", "cheesey");
        assertExpression("A ${topping} ${type} pizza", "A cheese deepPan pizza");
        assertExpression("${topping}-${type}", "cheese-deepPan");
        assertExpression("$type${topping}", "$typecheese");
        assertExpression("$type$topping", "$type$topping");
        assertExpression("type$$topping$", "type$$topping$");
        assertExpressionNotExpressionText("$${topping}", "${topping}");
        assertExpressionNotExpressionText("$$type$${topping}$$", "$$type${topping}$$");

        try {
            assertExpression("${ some junk !< 4}", Boolean.TRUE);
            assertTrue("An illegal expression was allowed", false);
        }catch(JellyException e) {
            // Nothing, the test passed
        }
        context.setVariable("test", new TestHelper());
        try {
            assertExpression("${test.throwAnException()}", Boolean.TRUE);
            assertTrue("An exception was suppressed while processing the JEXL script", false);
        }catch(IllegalStateException e) {
            if (!(e.getCause() instanceof TestException))
                throw e;
            // Nothing, the test passed
        }
    }

    public void testAntExpresssions() throws Exception {
        context.setVariable("maven.home.foo", "cheese");

        assertExpression("${maven.home.foo}", "cheese");
        assertExpression("${maven.some.madeup.name}", null);
        assertExpression("cheese ${maven.some.madeup.name}pizza", "cheese pizza");
        assertExpression("ham and ${maven.home.foo} pizza", "ham and cheese pizza");
        assertExpression("${maven.home.foo.length()}", new Integer(6));
    }
    
    /** tests that $${xx} is output as ${xx}. This trick is ued
        by several plugins to generate other jelly files or ant files.
        The maven ant plugin is one of them. */
    public void testExpressionsEvalOutput() throws Exception {
        String expressionText = "ham and $${maven.home.foo} pizza";
        Expression expression = CompositeExpression.parse(expressionText, factory);
        assertTrue( "Created a valid expression for: " + expressionText, expression != null );
        String value = (String) expression.evaluate(context);
        assertEquals("$${xx} should output ${xx}","ham and ${maven.home.foo} pizza",value);
    }

    public void testNotConditions() throws Exception {
        context.setVariable("a", Boolean.TRUE);
        context.setVariable("b", Boolean.FALSE);
        context.setVariable("c", "true");
        context.setVariable("d", "false");

        assertExpression("${a}", Boolean.TRUE);
        assertExpression("${!a}", Boolean.FALSE);
        assertExpression("${b}", Boolean.FALSE);
        assertExpression("${!b}", Boolean.TRUE);

        assertExpression("${c}", "true");
        assertExpression("${!c}", Boolean.FALSE);
        assertExpression("${d}", "false");
        assertExpression("${!d}", Boolean.TRUE);
    }

    public void testNotConditionsWithDot() throws Exception {
        context.setVariable("x.a", Boolean.TRUE);
        context.setVariable("x.b", Boolean.FALSE);
        context.setVariable("x.c", "true");
        context.setVariable("x.d", "false");

        assertExpression("${x.a}", Boolean.TRUE);
        assertExpression("${!x.a}", Boolean.FALSE);
        assertExpression("${x.b}", Boolean.FALSE);
        assertExpression("${!x.b}", Boolean.TRUE);

        assertExpression("${x.c}", "true");
        assertExpression("${!x.c}", Boolean.FALSE);
        assertExpression("${x.d}", "false");
        assertExpression("${!x.d}", Boolean.TRUE);
    }

    public void testNull() throws Exception {
        context.setVariable("something.blank", "");
        context.setVariable("something.ok", "cheese");

        assertExpression("${something.blank.length() == 0}", Boolean.TRUE);
        assertExpression("${something.blank == ''}", Boolean.TRUE);
        assertExpression("${something.ok != null}", Boolean.TRUE);
        assertExpression("${something.ok != ''}", Boolean.TRUE);
        // null is a reserved word
        //assertExpression("${something.null != ''}", Boolean.FALSE);
        assertExpression("${unknown == null}", Boolean.TRUE);
    }

    protected void assertExpression(String expressionText, Object expectedValue) throws Exception {
        Expression expression = CompositeExpression.parse(expressionText, factory);
        assertTrue( "Created a valid expression for: " + expressionText, expression != null );
        Object value = expression.evaluate(context);
        assertEquals( "Wrong result for expression: " + expressionText, expectedValue, value );

        String text = expression.getExpressionText();
        assertEquals( "Wrong textual representation for expression text: ", expressionText, text);
    }

    protected void assertExpressionNotExpressionText(String expressionText, Object expectedValue) throws Exception {
        Expression expression = CompositeExpression.parse(expressionText, factory);
        assertTrue( "Created a valid expression for: " + expressionText, expression != null );
        Object value = expression.evaluate(context);
        assertEquals( "Wrong result for expression: " + expressionText, expectedValue, value );
    }


}
