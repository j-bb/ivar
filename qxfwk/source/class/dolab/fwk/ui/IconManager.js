/*
 #asset(dolab/fwk/*)
 */

qx.Class.define("dolab.fwk.ui.IconManager", {
    extend: qx.core.Object,

    statics: {
        DEBUGICONS: false,
        URLPrefix: "/icons/icons/",
        keys: null,
        res: null,

        getIcon: function (key, res, pack) {
            return dolab.fwk.ui.IconManager.getIconFull(key, res, "plain", pack);
        },

        getIconFull: function (userkey, userres, type, pack) {
            var result = null;
            var key = dolab.fwk.ui.IconManager.getRealKey(userkey);
            var res = dolab.fwk.ui.IconManager.getRealRes(userres);
            var globalURLprefix = dolab.fwk.ui.IconManager.URLPrefix + (pack ? pack : "icons");
            if (key.charAt(0) == "/") {
                globalURLprefix = "";
            }
                    
            if (res == 0) {
                result = globalURLprefix + "/" + key + "." + userres;
            } else {
                result = globalURLprefix + "/" + res + "x" + res + "/" + type + "/" + key + ".png";
            }
            var logger = new qx.core.Object();
            logger.debug("[IconManager] getIconFull(" + userkey + ", " + userres + ", " + type + ", " + pack + ") : " + result);
            return result;
        },

        getRealKey: function (key) {
            var result = dolab.fwk.ui.IconManager.keys[key];
            if (result == null) {
                result = key;
            }
            return result;
        },

        getRealRes: function (r) {
            return dolab.fwk.ui.IconManager.res[r];
        },

        init: function () {
            //this.base(arguments);

            var keys = [];
            var res = [];

            res["menu"] = 16;
            res["tool"] = 24;
            res["jump"] = 24;
            res["minitool"] = 16;
            res["icon"] = 24;
            res["bigico"] = 48;
            res["win"] = 16;
            res["png"] = 0;
            res["gif"] = 0;

            keys["login"] = "key1";
            keys["default-icon"] = "note_pinned";
            keys["add login"] = "key1_preferences";


            keys["small_logo"] = "/icons/do/small_logo_white";
            keys["documentation"] = "book_open";
            keys["comment"] = "note";
            keys["finder refresh"] = "element_refresh";

            keys["kind"] = "element";
            keys["calc kind"] = "calculator";
            keys["chk kind"] = "element_ok";
            keys["chk state kind"] = "element_selection";
            keys["chk notstate kind"] = "element_selection_delete";
            keys["set state kind"] = "element_preferences";
            keys["format kind"] = "element_time";
            keys["run kind"] = "element_run";

            keys["hook"] = "element";
            keys["hook before"] = "element_new_before";
            keys["hook after"] = "element_new_after";
            keys["hook error"] = "element_delete";
            keys["hook finally"] = "element_down";
            keys["hook useraction"] = "element_into_input";

            keys["tbl finder"] = "elements2";
            keys["icn finder"] = "elements1";

            keys["data"] = "data";
            keys["new data"] = "data_add";
            keys["edt data"] = "data_edit";
            keys["del data"] = "data_delete";

            keys["datasetdata"] = "row";
            keys["new datasetdata"] = "cube_blue_add";
            keys["del datasetdata"] = "cube_blue_delete";

            keys["rule"] = "cube_blue";
            keys["new rule"] = "cube_blue_add";
            keys["edt rule"] = "cube_blue_preferences";
            keys["del rule"] = "cube_blue_delete";

            keys["role"] = "pawn";
            keys["new role"] = "pawn_add";
            keys["edt role"] = "pawn_preferences";
            keys["del role"] = "pawn_delete";

            keys["step"] = "coffeebean";
            keys["new step"] = "coffeebean_add";
            keys["edt step"] = "coffeebean_preferences";
            keys["del step"] = "coffeebean_delete";

            keys["scenario"] = "jar_bean";
            keys["new scenario"] = "jar_bean_add";
            keys["edt scenario"] = "jar_bean_preferences";
            keys["del scenario"] = "jar_bean_delete";

            keys["static filter"] = "layout_horizontal";
            keys["dynamic filter"] = "table_selection_row";
            keys["new filter element"] = "row_add";
            keys["del filter element"] = "row_delete";

            keys["field"] = "id_card";
            keys["field err"] = "id_card_error";
            keys["field wrn"] = "id_card_warning";
            keys["field rsh"] = "element_replace";
            keys["field del"] = "element_delete";
            keys["field add"] = "element_add";
            keys["field edt"] = "element_preferences";

            keys["application"] = "server";
            keys["run application"] = "application_server_run";
            keys["edt application"] = "server_preferences";
            keys["rpt application"] = "server_information";
            keys["cmp application"] = "server_client_exchange";
            keys["del application"] = "server_delete";
            keys["new application"] = "server_add";
            keys["imp application"] = "server_from_client";
            keys["exp application"] = "server_to_client";
            keys["upl application"] = "server_document";
            keys["upd application"] = "server_mail";
            keys["dup application"] = "server_new";
            keys["cmp info"] = "server_unknown";

            keys["search result"] = "table_sql_view";

            keys["ok"] = "001_06";
            keys["search"] = "001_38";
            keys["close"] = "001_09";
            keys["cancel"] = "001_05";
            keys["refresh"] = "001_39";
            keys["update"] = "001_45";
            keys["yes"] = keys["ok"];
            keys["no"] = keys["cancel"];
            keys["yesdelete"] = "001_49";
            keys["run"] = "001_25";
            keys["download"] = "download";
            keys["warning"] = "001_11";

            keys["targapp create"] = "note_add";
            keys["targapp update"] = "note_edit";
            keys["targapp delete"] = "note_delete";
            keys["targapp search"] = "note_view";
            keys["targapp read"] = "note";
            keys["targapp bug"] = "bug_red";

            keys["csv export"] = "File_Excel";

            dolab.fwk.ui.IconManager.keys = keys;
            dolab.fwk.ui.IconManager.res = res;
        }
    }
});