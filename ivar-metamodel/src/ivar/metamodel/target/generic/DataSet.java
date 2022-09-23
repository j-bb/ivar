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
package ivar.metamodel.target.generic;

import ivar.common.AbstractObject;
import ivar.metamodel.spec.Application;
import ivar.metamodel.spec.DataSize;
import ivar.metamodel.spec.DatasetData;
import java.util.Set;
import ivar.helper.StringHelper;

public class DataSet extends AbstractObject {

    private Application application;
    private ivar.metamodel.spec.Dataset dataset;

    public DataSet(final Application application, final ivar.metamodel.spec.Dataset dataSet) {
        this.application = application;
        this.dataset = dataSet;
    }

    public String getClassName() {
        return StringHelper.getStringForJavaPackage(application.getKeyname()) + ".datasets." + getFSName();
    }

    public String getFSName() {
        return StringHelper.getFirstCapitalized(dataset.getKeyname());
    }

    public Set<DatasetData> getDatas() {
        return dataset.getDatas();
    }

    public ivar.metamodel.spec.Dataset getDataset() {
        return dataset;
    }

    public boolean isSmall() {
        return dataset.getSize().equals(DataSize.SMALL);
    }

    public boolean isXSmall() {
        return dataset.getSize().equals(DataSize.XSMALL);
    }

    public String getKeyname() {
        return dataset.getKeyname();
    }

    public void free() {
        application = null;
        dataset.free();
        dataset = null;
    }
}
