/*
   Copyright 2020 Jean-Baptiste BRIAUD.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   imitations under the License.
 */
package ivar.generator;

public class GeneratorConstants {

    public int getFixColumnSize() {
        return 130;
    }

    public int getQueryLimitForSmallSelector() {
        return 40;
    }

    public int getQueryLimitForXSmallSelector() {
        return 10;
    }

    public int getMaxImageSizeTabular() {
        return 50;
    }

    public int getMaxImageSizeFormular() {
        return 400;
    }

    public int getLayoutColumnWidth() {
        return 300;
    }
}
