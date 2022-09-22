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
package ivar.common.fwk;

public class RuntimeFilterElement {

    private String booleanOperator = null;
    private String requestJoinElement;
    private String requestWhereElement = null;
    private String requestWhereElementAgainstNull = null;

    public RuntimeFilterElement(final String booleanOperator, final String requestWhereElement, final String requestWhereElementAgainstNull) {
        this(booleanOperator, null, requestWhereElement, requestWhereElementAgainstNull);
    }

    public RuntimeFilterElement(final String booleanOperator, final String requestJoinElement, final String requestWhereElement, final String requestWhereElementAgainstNull) {

        this.booleanOperator = booleanOperator;
        this.requestJoinElement = requestJoinElement;
        this.requestWhereElement = requestWhereElement;
        this.requestWhereElementAgainstNull = requestWhereElementAgainstNull;
    }

    public String getBooleanOperator() {
        return booleanOperator != null ? booleanOperator : "";
    }

    public String getRequestWhereElement() {
        return requestWhereElement;
    }

    public String getRequestWhereElementAgainstNull() {
        return requestWhereElementAgainstNull;
    }

    public boolean isThereRequestJoinElement() {
        return requestJoinElement != null;
    }

    public String getRequestJoinElement() {
        return requestJoinElement;
    }
}
