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
package ivar.generator;

public class Differentiator {

    private String prefix = null;

    public Differentiator(final String prefix) {
        setPrefix(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        if (prefix == null) {
            this.prefix = null;
            return;
        }

        if (!prefix.matches("[a-z0-9]{3}")) {
            throw new IllegalArgumentException("Differentiator must contain exactly three letters or numbers.");
        }
        this.prefix = prefix;
    }
}
