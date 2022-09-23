/*
   Copyright (c) 2004-2020, Jean-Baptiste BRIAUD. All Rights Reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License
 */
package ivar.metamodel.spec;

import ivar.helper.CollectionFactory;
import ivar.metamodel.IdentifiableObject;
import ivar.metamodel.target.generic.ui.Screen;
import java.util.Map;
import java.util.Set;

public class Jump extends IdentifiableObject {

    private String targetKeyname = null;
    private String name = null;
    private CRUD crud = null;
    private Integer pos;
    private boolean isDefault = false;
    private DataSize size = DataSize.LARGE;
    transient private Scenario targetScenario;
    transient private Scenario sourceScenario;
    transient private Screen screen;
    // Containt a list of attribute that are part of the context stored as String
    transient private Map<String, String> context = CollectionFactory.newMapWithInsertionOrderPreserved();
    transient private boolean compiled;
    transient private boolean miniJump = false;
    transient private boolean showNoToolTip = false;

    public Jump() {
    }

    public Jump(final int pos, final String targetKeyname) {
        this(pos, targetKeyname, null);
    }

    public Jump(final int pos, final String targetKeyname, final CRUD crud) {
        this.pos = pos;
        this.targetKeyname = targetKeyname;
        this.crud = crud;
    }

    public String getTitle() {
        String result;
        final Scenario targetScenario = getTargetScenario();
        result = targetScenario.getName();
        if (targetScenario.getDocumentation() != null && targetScenario.getDocumentation().length() > 1) {
            result = "<html><body>" + targetScenario.getName();
            result += "<br/>";
            result += targetScenario.getDocumentation();
            result += "</body></html>";
        }

        return result;
    }

    public boolean hasRichTitle() {
        return getTitle().startsWith("<html>");
    }

    public String getTargetKeyname() {
        return targetKeyname;
    }

    public CRUD getCrud() {
        return crud;
    }

    public CRUD getCrudOrTargetCrud() {
        final CRUD result;

        if (crud != null) {
            result = crud;
        } else {
            result = (hasTargetScenario()) ? getTargetScenario().getCrud() : null;
        }

        return result;
    }

    @Override
    public Jump getJPAClone() {
        return getJPAClone(new Jump());
    }

    protected Jump getJPAClone(final Jump dst) {
        super.getJPAClone(dst);
        if (pos != null) {
            dst.pos = new Integer(pos);
        } else {
            dst.pos = null;
        }

        if (targetKeyname != null) {
            dst.targetKeyname = new String(targetKeyname);
        } else {
            dst.targetKeyname = null;
        }

        if (name != null) {
            dst.name = new String(name);
        } else {
            dst.name = null;
        }

        dst.crud = crud;

        dst.isDefault = isDefault;

        dst.size = size;

        // Non JPA attribute
        dst.targetScenario = targetScenario;
        dst.sourceScenario = sourceScenario;
        dst.screen = screen;
        dst.context.putAll(context);
        dst.compiled = compiled;

        return dst;
    }

    @Override
    public String toString() {
        return "Jump{"
                + pos
                + " - targetKeyname='" + targetKeyname + '\''
                + ", name='" + name + '\''
                + ", crud=" + crud
                + ", isDefault=" + isDefault
                + ", context={ " + getContextAsString() + " }"
                + ", size=" + size
                + '}';
    }

    public void setSourceScenario(final Scenario sourceScenario) {
        this.sourceScenario = sourceScenario;
    }

    public void setTargetScenario(final Scenario targetScenario) {
        this.targetScenario = targetScenario;
    }

    public Scenario getTargetScenario() {
        return targetScenario;
    }

    public Screen getTargetScreen() {
        return (hasTargetScenario()) ? targetScenario.getAssociatedScreen() : null;
    }

    public Scenario getSourceScenario() {
        return sourceScenario;
    }

    public boolean isSmallSelector() {
        return (isSmall() || isXSmall()) && getCrudOrTargetCrud().isSearch();
    }

    private boolean isTransitionWithUniqueId() {
        boolean result = false;
        final CRUD targetScenarioCRUD = targetScenario.getCrud();
        if (targetScenarioCRUD.equals(CRUD.read) || targetScenarioCRUD.equals(CRUD.delete) || targetScenarioCRUD.equals(CRUD.update)) {
            result = true;
        }

        return result;
    }

    public Set<String> getContext() {
        return context.keySet();
    }

    public void computeJumpContext() {
        final boolean isTransitionWithUniqueId = isTransitionWithUniqueId();
        if (isTransitionWithUniqueId) {
            context.put("id", "id");
        } else {
            final Filter dynamicFilter = targetScenario.getDynamicFilter();
            if (dynamicFilter != null) {
                for (final FilterElement filterElement : dynamicFilter.getFilterElements()) {
                    final String contextElement = filterElement.getAssociatedBusinessObjectAttribute().getKeyname();
                    context.put(contextElement, contextElement);
                }
            }
        }

        // Check
        for (final String contextElement : context.keySet()) {
            if (!"id".equals(contextElement)) {
                boolean found = false;
                for (final Step step : sourceScenario.getSteps()) {
                    if (step.getKeyname().equals(contextElement)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    compileError("Unable to find context element " + contextElement + " in the source scenario " + sourceScenario.getKeyname() + "'s steps");
                    beginCompileHelpBlock("For helping purpose, here are the step of the source scenario. We try to find " + contextElement + " as a context element in");
                    for (final Step step : sourceScenario.getSteps()) {
                        compileHelp("   * " + step.getKeyname());
                    }
                    endCompileHelpBlock();
                }
            }
        }
    }

    public String getContextAsString() {
        String result = "{";
        for (final String contextElement : context.keySet()) {
            result += contextElement;
            result += ", ";
        }
        result += "}";
        return result;
    }

    public boolean isCreate() {
        return hasTargetScenario() && getTargetScenario().getCrud().isCreate();
    }

    public boolean isUpdate() {
        return hasTargetScenario() && getTargetScenario().getCrud().isUpdate();
    }

    public boolean isDelete() {
        return hasTargetScenario() && getTargetScenario().getCrud().isDelete();
    }

    public boolean isRead() {
        return hasTargetScenario() && getTargetScenario().getCrud().isRead();
    }

    public boolean isStrictlyRead() {
        return hasTargetScenario() && getTargetScenario().getCrud().isStrictlyRead();
    }

    public boolean isSearch() {
        return hasTargetScenario() && getTargetScenario().getCrud().isSearch();
    }

    public boolean hasTargetScenario() {
        return getTargetScenario() != null;
    }

    public boolean isXSmall() {
        return size != null && size.equals(DataSize.XSMALL);
    }

    public boolean isSmall() {
        return size != null && size.equals(DataSize.SMALL);
    }

    public boolean isLarge() {
        return size == null || size.equals(DataSize.LARGE);
    }

    public boolean isXLarge() {
        return size != null && size.equals(DataSize.XLARGE);
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name != null ? name : getTargetScenario().getName();
    }

    public Integer getPos() {
        return pos;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean hasName() {
        return name != null && name.length() > 0;
    }

    public boolean isCompiled() {
        return compiled;
    }

    public void setCompiled() {
        compiled = true;
    }

    public String getTargetBusinessObjectName() {
        return targetScenario.getRootBusinesObject().getName().toLowerCase();
    }

    public void free() {
        targetScenario = null;

        sourceScenario = null;

        screen = null;
        if (context != null) {
            context.clear();
        }
        context = null;
    }

    public boolean isMiniJump() {
        return miniJump;
    }

    public void setMiniJump(final boolean mini) {
        miniJump = mini;
    }

    public boolean isShowNoToolTip() {
        return !miniJump && showNoToolTip;
    }

    public void setShowNoToolTip(final boolean dontShow) {
        showNoToolTip = dontShow;
    }
}
