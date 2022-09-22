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

import ivar.common.logger.Logger;
import ivar.fwk.rpc.ControllerContext;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import javax.persistence.Query;

public class ApplicationFileIndexController extends AbstractFileIndexController implements Remote {

    @Override
    protected Class<? extends AbstractFileIndex> getPersistentClass() {
        return ApplicationFileIndex.class;
    }

    @Override
    public ApplicationFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId) throws RemoteException {
        return (ApplicationFileIndex) super.getFileIndexById(cc, fileIndexId);
    }

    @Override
    public ApplicationFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId, final boolean withFetchPlan) throws RemoteException {
        return (ApplicationFileIndex) super.getFileIndexById(cc, fileIndexId, withFetchPlan);
    }

    @Override
    public ApplicationFileIndex createInCreate(final ControllerContext cc, final AbstractFileIndex fileIndex) throws RemoteException {
        return (ApplicationFileIndex) super.createInCreate(cc, fileIndex);
    }

    @Override
    public ApplicationFileIndex[] prepareSearchInSearch(final ControllerContext cc) throws RemoteException {
        return (ApplicationFileIndex[]) super.prepareSearchInSearch(cc);
    }

    public List<ApplicationFileIndex> getIndexesForCompilation(final ControllerContext cc, final long applicationId) throws RemoteException {
        final Logger log = cc.getLogger();
        List<ApplicationFileIndex> result = null;
        log.beginBlock("Getting file indexes prior to compilation procedure for application " + applicationId + "...");
        log.beginBlock("Computing fetch plan for this request...");
        cc.addField(AbstractFileIndex.class, "id");
        cc.addField(AbstractFileIndex.class, "fileName");
        cc.addField(AbstractFileIndex.class, "path");
        cc.addField(AbstractFileIndex.class, "user");
        cc.addField(AbstractFileIndex.class, "temporary");
        cc.addField(AbstractFileIndex.class, "lastModified");
        cc.pushFetchPlan();
        cc.computeFetchPlan();
        log.endBlock("Computing fetch plan for this request... done.");
        try {
            cc.beginTransaction();
            final String jpql = "SELECT nfi FROM ApplicationFileIndex nfi, Scenario s "
                    + "WHERE nfi.id IN ("
                    + "SELECT nrt.template FROM FusionTemplate nrt "
                    + "WHERE nrt.id IN ("
                    + "SELECT s.fusionTemplate FROM Application app INNER JOIN app.scenarios s "
                    + "WHERE app.id = " + applicationId
                    + "))";

            final Query query = cc.createQuery(jpql);
            final List results = query.getResultList();
            result = (List<ApplicationFileIndex>) results;
            log.info(results.size() + " results were fetched.");
            cc.commitTransaction();
        } catch (Throwable t) {
            cc.rollbackTransaction();
            log.error(t, true);
        }
        cc.popFetchPlan();
        log.endBlock("Getting file indexes prior to compilation procedure for application " + applicationId + "... Done.");
        return result;
    }
}
