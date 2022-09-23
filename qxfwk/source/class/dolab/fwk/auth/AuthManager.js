/**
 * User: Loïc Bresson (Pandore)
 * Date: Dec 1, 2009
 * Time: 11:37:23 AM
 */

qx.Class.define("dolab.fwk.auth.AuthManager", {
    extend : qx.core.Object,

    type : "singleton",

    construct : function() {
        this.base(arguments);

        this.roles = [];
    },

    members : {
        user : null,
        roles : null,
        __version : null,

        //        addRole : function(role) {
        //            this.roles.push(role);
        //        },

        //        /**
        //         * Removes a role
        //         * @param role The role to remove
        //         * @return true if the role has been removed, false otherwise
        //         */
        //        removeRole : function(role) {
        //            var roleIndex = this.roles.indexOf(role);
        //            if (roleIndex >= 0) {
        //                this.roles.splice(roleIndex, 1);
        //                return true;
        //            } else {
        //                return false;
        //            }
        //        },

        setRoles : function(roles) {
            this.roles = roles;
        },

        hasRole : function(role) {
            var result = false;
            if ("public" == role) {
                result = true;
            } else {
                for (var i = this.roles.length - 1; i >= 0; i--) {
                    if (this.roles[i] == role) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        },

        hasRoles : function(roles) {
            var result = false;
            for (var i = roles.length - 1; i >= 0; i--) {
                result = this.hasRole(roles[i]);
                if (result == true) {
                    break;
                }
            }
            return result;
        },

        getRoles : function() {
            return this.roles;
        },

        getUser : function() {
            return this.user;
        },

        setUser : function(user) {
            this.user = user;
        },

        setVersion : function(version) {
            this.__version = version;
        },

        getVersion : function() {
            return this.__version;
        }
    }
});