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

public enum CRUD {
    create,
    read, // Read one Person on form with formular view, not table view since there is only one Person. Only specified attributes.
    dread, // Read one Person on form with formular view, not table view since there is only one Person. Defaultattribute
    search, // Read several Persons, it is a tabular view. Only specified attribute
    dsearch, // Read several Persons, it is a tabular view. Default, so all attributes will be there
    update,
    delete;

    public boolean isCreate() {
        return this.equals(CRUD.create);
    }

    public boolean isUpdate() {
        return this.equals(CRUD.update);
    }

    public boolean isDelete() {
        return this.equals(CRUD.delete);
    }

    public boolean isRead() {
        return this.equals(CRUD.search) || this.equals(CRUD.dsearch) || this.equals(CRUD.read) || this.equals(CRUD.dread);
    }

    public boolean isStrictlyRead() {
        return this.equals(CRUD.read) || this.equals(CRUD.dread);
    }

    public boolean isSearch() {
        return isRead() && !isStrictlyRead();
    }

    public boolean isDefault() {
        return this.equals(CRUD.dsearch) || this.equals(CRUD.dread);
    }

    public String getAsString() {
        final String result;
        switch (this) {
            case dread:
            case read:
                result = "Read";
                break;
            case dsearch:
            case search:
                result = "Search";
                break;
            case create:
                result = "Create";
                break;
            case delete:
                result = "Delete";
                break;
            case update:
                result = "Update";
                break;
            default:
                result = "";
        }

        return result;
    }

    public static String getCRUDForUAT(final CRUD crud) {
        String result = null;
        switch (crud) {
            case read:
                result = "Read";
                break;
            case dread:
                result = "Default Read";
                break;
            case search:
                result = "Search";
                break;
            case dsearch:
                result = "Default Search";
                break;
            case delete:
                result = "Delete";
                break;
            case update:
                result = "Update";
                break;
            case create:
                result = "Create";
                break;
            default:
                throw new RuntimeException("Unknown CRUD for UAT : " + crud);
        }

        return result;
    }
}
