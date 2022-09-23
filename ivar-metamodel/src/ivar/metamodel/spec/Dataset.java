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

import ivar.metamodel.MetaObject;
import java.util.Set;
import ivar.helper.CollectionFactory;

public class Dataset extends MetaObject {

    private Set<DatasetData> dataset = CollectionFactory.newSetWithInsertionOrderPreserved();
    private DataSize size = DataSize.SMALL;

    public Dataset() {
    }

    public Dataset(final String keyname, final String name) {
        setKeyname(keyname);
        setName(name);
    }

    public void addData(final DatasetData newDatasetData) throws DuplicateDatasetDataException {
        for (final DatasetData datasetData : dataset) {
            if (datasetData.getValue().equals(newDatasetData.getValue())) {
                throw new DuplicateDatasetDataException(this, datasetData, newDatasetData);
            }
        }
        dataset.add(newDatasetData);
    }

    public Set<DatasetData> getDatas() {
        return dataset;
    }

    public DataSize getSize() {
        return size;
    }

    public int getMaxDataLength() {
        int result = -1;
        for (DatasetData dataSetData : dataset) {
            result = Math.max(result, dataSetData.getValue().length());
        }
        return result;
    }

    @Override
    public Dataset getJPAClone() {
        return getJPAClone(new Dataset());
    }

    protected Dataset getJPAClone(Dataset dst) {
        super.getJPAClone(dst);

        dst.getDatas().clear();
        for (final DatasetData data : dataset) {
            dst.dataset.add(data.getJPAClone());
        }

        dst.size = size;

        return dst;
    }

    public void free() {
        if (dataset != null) {
            dataset.clear();
            dataset = null;
        }
    }
}
