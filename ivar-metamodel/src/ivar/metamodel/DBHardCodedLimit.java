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
package ivar.metamodel;

public interface DBHardCodedLimit {

    static final int smallEnumLength = 10;
    static final int stdEnumLength = 60;

    static final int shortDescription = 25;
    static final int stdDescription = 100;
    // WARNING !! 10000 doesn't work under SQLServer ... the max allowed is 8000
    // TODO : Find a new type like textual "blob" ? Something able to store large quantity of text without limit.
    static final int longDescription = 8000;

    static final int stdIdentifier = 60;
    static final int stdNumber = 10;

    static final int stdTechIdentifier = 50;
    static final int longTechIdentifier = 200;
}
