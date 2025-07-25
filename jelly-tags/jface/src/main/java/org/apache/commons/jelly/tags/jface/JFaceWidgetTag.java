/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jelly.tags.jface;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowImpl;
import org.apache.commons.jelly.tags.jface.window.ApplicationWindowTag;
import org.apache.commons.jelly.tags.jface.wizard.WizardPageTag;
import org.apache.commons.jelly.tags.swt.WidgetTag;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of SWT WidgetTag
 */
public class JFaceWidgetTag extends WidgetTag implements Tag {

    /**
     * @param widgetClass
     */
    public JFaceWidgetTag(final Class widgetClass) {
        super(widgetClass);
    }

    /**
     * @param widgetClass
     * @param style
     */
    public JFaceWidgetTag(final Class widgetClass, final int style) {
        super(widgetClass, style);
    }

    /*
     * @see org.apache.commons.jelly.tags.swt.WidgetTag#attachWidgets(java.lang.Object, org.eclipse.swt.widgets.Widget)
     */
    @Override
    protected void attachWidgets(final Object parent, final Widget widget) throws JellyTagException {
        super.attachWidgets(parent, widget);

        // set Parent composite of wizard page
        if (getParent() instanceof WizardPageTag) {
            final WizardPageTag tag = (WizardPageTag) getParent();
            if (tag.getWizardPageImpl().getParentControl() == null) {
                if (!(widget instanceof Composite)) {
                    throw new JellyTagException("First child of a <wizardPage> must be of type Composite");
                }
                tag.getWizardPageImpl().setParentComposite((Composite) widget);
            }
        }
    }

    /*
     * @see org.apache.commons.jelly.tags.swt.WidgetTag#getParentWidget()
     */
    @Override
    public Widget getParentWidget() {
        parent = super.getParentWidget();

        if (parent == null && getParent() instanceof WizardPageTag) {
            final WizardPageTag tag = (WizardPageTag) getParent();
            if (tag != null) {
                final WizardPageTag.WizardPageImpl page = tag.getWizardPageImpl();
                return page.getControl();
            }
        }

        if (parent == null) {
            final ApplicationWindowTag tag =
                (ApplicationWindowTag) findAncestorWithClass(ApplicationWindowTag.class);
            if (tag != null) {
                final Window window = tag.getWindow();
                if (window != null && window instanceof ApplicationWindowImpl) {
                    return ((ApplicationWindowImpl) window).getContents();
                }
            }
        }

        return parent;
    }

}
