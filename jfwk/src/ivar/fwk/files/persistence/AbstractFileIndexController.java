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
import ivar.fwk.util.Generated;
import java.rmi.RemoteException;
import java.util.List;
import javax.persistence.Query;

@Generated(origin = "files.json", partial = true)
public abstract class AbstractFileIndexController {

    protected abstract Class<? extends AbstractFileIndex> getPersistentClass();

    protected String getTableName() {
        return getPersistentClass().getSimpleName();
    }

    public AbstractFileIndex createInCreate(final ControllerContext cc, final AbstractFileIndex fileIndex) throws RemoteException {
        final Logger log = cc.getLogger();
        boolean transactionBegun = false;
        AbstractFileIndex result = null;
        log.beginBlock("AbstractFileIndexController.createInCreate(fileIndex)");
        log.debug("Attempt to add a AbstractFileIndex");
        log.debug("-------------------------");
        log.debug(fileIndex.toString());
        log.debug("-------------------------");

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
            if (!cc.isTransactionActive()) {
                cc.beginTransaction();
            } else {
                transactionBegun = true;
            }

            result = cc.merge(fileIndex);
            if (!transactionBegun) {
                cc.commitTransaction();
            }
        } catch (Throwable t) {
            cc.rollbackTransaction();
            log.error(t, true);
            throw new RemoteException(t.getMessage(), t);
        } finally {
            cc.popFetchPlan();
            log.endBlock("AbstractFileIndexController.createInCreate(fFileIndex)");
        }
        return result;
    }

    public void deleteInDelete(final ControllerContext cc, final long fileIndexId) throws RemoteException {
        final Logger log = cc.getLogger();
        boolean transactionBegun = false;
        log.beginBlock("AbstractFileIndexController.deleteInDelete(fileIndexId = " + fileIndexId + ")");
        log.beginBlock("Computing fetch plan for this request...");
        cc.addField(AbstractFileIndex.class, "id");
        cc.pushFetchPlan();
        cc.computeFetchPlan();
        log.endBlock("Computing fetch plan for this request... done.");
        /* Supplementary code to delete file on file system */
        try {
            AbstractFileIndex fileIndex = cc.find(getPersistentClass(), fileIndexId);
            if (!fileIndex.removeFile()) {
                log.warning("File has not been deleted on filesystem! Maybe it was already gone?");
            }
        } catch (Throwable t) {
            log.error(t, true);
            throw new RemoteException(t.getMessage(), t);
        }
        /* End of custom code */
        try {
            if (!cc.isTransactionActive()) {
                cc.beginTransaction();
            } else {
                transactionBegun = true;
            }
            final String ql = "DELETE FROM " + getTableName() + " fileIndex WHERE fileIndex.id=" + fileIndexId;
            final Query query = cc.createQuery(ql);
            final int deletedRows = query.executeUpdate();
            log.info(deletedRows + " object(s) were affected by the delete query in AbstractFileIndexController.deleteInDelete()");
            if (!transactionBegun) {
                cc.commitTransaction();
            }
        } catch (Throwable t) {
            if (cc.isTransactionActive()) {
                cc.rollbackTransaction();
            }
            log.error(t, true);
            throw new RemoteException(t.getMessage(), t);
        } finally {
            cc.popFetchPlan();
            log.endBlock("AbstractFileIndexController.deleteById(" + fileIndexId + ")");
        }
    }

    public AbstractFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId) throws RemoteException {
        return getFileIndexById(cc, fileIndexId, true);
    }

    public AbstractFileIndex getFileIndexById(final ControllerContext cc, final long fileIndexId, final boolean withFetchPlan) throws RemoteException {
        final Logger log = cc.getLogger();
        log.beginBlock("AbstractFileIndexController.getFileIndexById(id= " + fileIndexId + ")");
        AbstractFileIndex result = null;
        try {
            log.beginBlock("Computing fetch plan for this request...");
            if (withFetchPlan) {
                cc.addField(AbstractFileIndex.class, "id");
                cc.addField(AbstractFileIndex.class, "fileName");
                cc.addField(AbstractFileIndex.class, "path");
                cc.addField(AbstractFileIndex.class, "user");
                cc.addField(AbstractFileIndex.class, "temporary");
                cc.addField(AbstractFileIndex.class, "lastModified");
                cc.pushFetchPlan();
                cc.computeFetchPlan();
            }
            log.endBlock("Computing fetch plan for this request... done.");
            result = cc.find(getPersistentClass(), fileIndexId);
        } catch (Throwable t) {
            log.error(t, true);
            throw new RemoteException(t.getMessage(), t);
        } finally {
            if (withFetchPlan) {
                cc.popFetchPlan();
            }
            log.endBlock("AbstractFileIndexController.getFileIndexById(id= " + fileIndexId + ")");
        }
        return result;
    }

    public AbstractFileIndex[] prepareSearchInSearch(final ControllerContext cc) throws RemoteException {
        final Logger log = cc.getLogger();
        log.beginBlock("AbstractFileIndexController.prepareSearchInSearch()");

        log.beginBlock("Computing fetch plan for this request...");
        cc.addField(AbstractFileIndex.class, "id");
        cc.addField(AbstractFileIndex.class, "fileName");
        cc.addField(AbstractFileIndex.class, "path");
        cc.addField(AbstractFileIndex.class, "user");
        cc.addField(AbstractFileIndex.class, "temporary");
        cc.addField(AbstractFileIndex.class, "lastModified");
        cc.computeFetchPlan();
        log.endBlock("Computing fetch plan for this request... done.");
        AbstractFileIndex[] result = null;
        try {
            final String ql = "SELECT fileIndex FROM " + getTableName() + " fileIndex";
            final Query query = cc.createQuery(ql);
            final List requestResult = query.getResultList();
            final List<AbstractFileIndex> fileIndex = (List<AbstractFileIndex>) requestResult;

            result = fileIndex.toArray(new AbstractFileIndex[fileIndex.size()]);
        } catch (Throwable t) {
            log.error(t, true);
            throw new RemoteException(t.getMessage(), t);
        } finally {
            log.endBlock("AbstractFileIndexController.prepareSearchInSearch()");
        }

        return result;
    }

    /* Custom method */
    public void removeAll(final ControllerContext cc, final Integer[] ids) throws RemoteException {
        final Logger log = cc.getLogger();
        log.beginBlock("AbstractFileIndexController.removeAll(ids=" + ids.toString() + ")");
        cc.beginTransaction();
        try {
            for (final Integer id : ids) {
                try {
                    deleteInDelete(cc, id);
                } catch (RemoteException e) {
                    if (cc.isTransactionActive()) {
                        cc.rollbackTransaction();
                    }
                    throw e;
                }
            }
        } finally {
            if (cc.isTransactionActive()) {
                cc.commitTransaction();
            }
            log.endBlock("AbstractFileIndexController.removeAll(ids=" + ids.toString() + ")");
        }
    }
    /* End custom method */
}
