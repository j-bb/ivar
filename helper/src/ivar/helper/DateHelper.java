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
package ivar.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateHelper {

    private static final SimpleDateFormat FOLDER_SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final SimpleDateFormat LOG_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private DateHelper() {
    }

    public static String getDateForFolder(final Date date) {
        return FOLDER_SIMPLE_DATE_FORMAT.format(date);
    }

    public static String getLogDate(final Date date) {
        return LOG_FORMAT.format(date);
    }
}
