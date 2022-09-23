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
package ivar.fwk.files.download;

import ivar.common.AbstractObject;
import ivar.helper.CollectionFactory;
import ivar.helper.StringHelper;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemDeposit<T> extends AbstractObject {

    private static long TIMEOUT_IN_MSECS = 60000; // 1 minute

    private final Map<String, T> items = CollectionFactory.newConcurrentMap();
    private final Map<String, Long> timeouts = CollectionFactory.newConcurrentMap();

    private String getNewKey() {
        String result = null;
        do {
            result = StringHelper.getRandomString(50);
        } while (items.containsKey(result));

        return result;
    }

    public String addItem(T item) {
        String key = getNewKey();

        items.put(key, item);
        timeouts.put(key, System.currentTimeMillis());
        return key;
    }

    public T getItem(String key) {
        T result = null;
        if (items.containsKey(key)) {
            result = items.get(key);
            items.remove(key);
            timeouts.remove(key);
        }
        clean();
        return result;
    }

    private void clean() {
        final List<String> toDelete = CollectionFactory.newList();
        final long referenceTime = System.currentTimeMillis() - TIMEOUT_IN_MSECS;
        for (Entry<String, Long> entry : timeouts.entrySet()) {
            if (referenceTime > entry.getValue()) {
                toDelete.add(entry.getKey());
            }
        }

        for (String key : toDelete) {
            items.remove(key);
            timeouts.remove(key);
        }
    }
}
