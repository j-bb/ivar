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
package ivar.metamodel.target.generic.ui;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import java.util.Set;

public class Menu extends AbstractObject {

    private static long generalInstanceCount = 0;
    private long instanceCount = 0;
    private String text;
    private Screen screen;
    private Set<Menu> menus = CollectionFactory.newSetWithInsertionOrderPreserved();
    private boolean root = false;
    private Menu parent = null;

    public Menu() {
        root = true;
        instanceCount = ++generalInstanceCount;
    }

    public Menu(final String text) {
        this.text = text;
        instanceCount = ++generalInstanceCount;
    }

    public Menu(final Screen screen) {
        this.screen = screen;
        this.text = screen.getTitle();
        instanceCount = ++generalInstanceCount;
    }

    public void addMenu(final Menu menu) {
        menu.setParent(this);
        menus.add(menu);
    }

    public void setParent(final Menu menu) {
        parent = menu;
    }

    public boolean noParent() {
        return parent == null;
    }

    public boolean isRoot() {
        return parent == null || parent.root;
    }

    public Menu getMenu(final String text) {
        Menu result = null;
        for (final Menu menu : menus) {
            if (menu.getText() != null && menu.getText().equals(text)) {
                result = menu;
                break;
            }
        }
        return result;
    }

    public Set<Menu> getMenus() {
        return menus;
    }

    public boolean isLeaf() {
        return screen != null;
    }

    public String getWindowClassName() {
        return screen.getQxClassName();
    }

    public String getText() {
        return text;
    }

    public Menu getParent() {
        return parent;
    }

    public String getJsUniqueKey() {

        String result = null;
        if (screen != null) {
            result = screen.getJsUniqueKey();
        } else if (text != null) {
            // TODO use that instance count only and only if a conflict is possible for better code
            // The considion of conflict happen on intermediate menu that do not have screen.
            // screen guarantee that none return the same string but here, it rely only on getText() which is not guarentee to be unique.
            result = StringHelper.getJavaString(getText()).toLowerCase();// + "_" + instanceCount + "_";
        }
        result = result.replaceAll("/", "_");
        return result;
    }

    public String getScreenBusinessObject() {
        String result = null;
        if (screen != null) {
            result = screen.getBusinessObjectNameFirstCapitalized();
        }
        return result;
    }

    public String getPrepareCallName() {
        String result = null;
        if (screen != null && screen.getPrepareCall() != null) {
            result = screen.getPrepareCall().getServerMethodName();
        }
        return result;
    }

    public boolean isDSearch() {
        return screen.isDSearch();
    }

    public boolean isCreate() {
        return screen.isCreate();
    }

    public boolean isDelete() {
        return screen.isDelete();
    }

    public boolean isSearch() {
        return screen.isSearch();
    }

    public boolean isUpdate() {
        return screen.isUpdate();
    }

    public Set<String> getRoles() {
        Set<String> roles = null;
        if (screen != null) {
            roles = screen.getRoles();
        } else {
            roles = CollectionFactory.newSet();
            for (Menu sub : getMenus()) {
                roles.addAll(sub.getRoles());
            }
            if (roles.size() == 0) {
                roles = null;
            }
        }
        return roles;
    }

    public boolean hasRoles() {
        boolean hasRoles = false;
        if (screen != null) {
            hasRoles = screen.hasRoles();
        } else {
            for (Menu sub : getMenus()) {
                hasRoles = hasRoles || sub.hasRoles();
                if (hasRoles) {
                    break;
                }
            }
        }
        return hasRoles;
    }

    public boolean isRolesAllowed() {
        boolean rolesAllowed = false;
        if (screen != null) {
            rolesAllowed = screen.isRolesAllowed();
        }
        return rolesAllowed;
    }

    @Override
    public String toString() {
        final String jsUniqueKey = getJsUniqueKey();
        return "Menu{"
                + ", text='" + (text != null ? text : "null") + '\''
                + ", jsUniqueKey=" + (jsUniqueKey != null ? jsUniqueKey : "null")
                + ", noParent=" + noParent()
                + ", isRoot=" + isRoot()
                + ", isLeaf=" + isLeaf()
                + ", menus size=" + menus.size()
                + '}';
    }

}
