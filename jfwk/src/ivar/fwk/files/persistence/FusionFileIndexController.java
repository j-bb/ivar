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
package ivar.fwk.files.persistence;

import ivar.fwk.rpc.ControllerContext;
import java.rmi.RemoteException;

public class FusionFileIndexController extends AbstractFileIndexController {

    @Override
    protected Class<? extends AbstractFileIndex> getPersistentClass() {
        return FusionFileIndex.class;
    }

    @Override
    public FusionFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId) throws RemoteException {
        return (FusionFileIndex) super.getFileIndexById(cc, fileIndexId);
    }

    @Override
    public FusionFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId, final boolean withFetchPlan) throws RemoteException {
        return (FusionFileIndex) super.getFileIndexById(cc, fileIndexId, withFetchPlan);
    }

    @Override
    public FusionFileIndex createInCreate(final ControllerContext cc, final AbstractFileIndex fileIndex) throws RemoteException {
        return (FusionFileIndex) super.createInCreate(cc, fileIndex);
    }

    @Override
    public FusionFileIndex[] prepareSearchInSearch(final ControllerContext cc) throws RemoteException {
        return (FusionFileIndex[]) super.prepareSearchInSearch(cc);
    }
}
