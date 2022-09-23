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

import ivar.helper.StringHelper;

public class SumDescriptor {

    private String ScreenField;
    private String field;

    public SumDescriptor(String screenField, String field) {
        ScreenField = screenField;
        this.field = field;
    }

    public String getScreenField() {
        return ScreenField;
    }

    public String getField() {
        return field;
    }

    public String getFunctionName() {
        return "sum" + StringHelper.getFirstCapitalized(getScreenField()) + StringHelper.getFirstCapitalized(getField());
    }
}
