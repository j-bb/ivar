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
package ivar.fwk.auth;

import ivar.helper.CollectionFactory;
import java.util.List;

public abstract class AbstractAuthenticationResult {

    private boolean logged = false;
    private List<String> roles = CollectionFactory.newList();
    private String composerVersion = null;

    public AbstractAuthenticationResult() {
    }

    public final boolean isLogged() {
        return logged;
    }

    public final void setLogged(final boolean logged) {
        this.logged = logged;
    }

    public final List<String> getRoles() {
        return roles;
    }

    public final void addRole(final String role) {
        this.roles.add(role);
    }

    public final void setComposerVersion(final String version) {
        composerVersion = version;
    }

    public final String getComposerVersion() {
        return composerVersion;
    }
}
