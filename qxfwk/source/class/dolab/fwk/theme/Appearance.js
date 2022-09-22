/* ************************************************************************

 Copyright:

 License:

 Authors:

 ************************************************************************ */

qx.Theme.define("dolab.fwk.theme.Appearance",
{
    extend : qx.theme.modern.Appearance,

    appearances :
    {
        /*
         ---------------------------------------------------------------------------
         TOOLBAR-dolab
         ---------------------------------------------------------------------------
         */

        "toolbar-dolab" :
        {
            style : function(states) {
                return {
                    decorator : "toolbar-dolab",
                    spacing : 2
                };
            }
        },

        "toolbar-dolab/part" :
        {
            style : function(states) {
                return {
                    decorator : "toolbar-dolab-part",
                    spacing : 2
                };
            }
        },

        "toolbar-dolab/part/container" :
        {
            style : function(states) {
                return {
                    paddingLeft : 2,
                    paddingRight : 2
                };
            }
        },

        "toolbar-dolab/part/handle" :
        {
            style : function(states) {
                return {
                    source : "decoration/toolbar/toolbar-handle-knob.gif",
                    marginLeft : 3,
                    marginRight : 3
                };
            }
        },

        "toolbar-dolab-button" :
        {
            alias : "atom",

            style : function(states) {
                return {
                    marginTop : 2,
                    marginBottom : 2,
                    padding : (states.pressed || states.checked || states.hovered) && !states.disabled
                            || (states.disabled && states.checked) ? 3 : 5,
                    decorator : states.pressed || (states.checked && !states.hovered) || (states.checked && states.disabled) ?
                            "toolbar-dolab-button-checked" :
                            states.hovered && !states.disabled ?
                                    "toolbar-dolab-button-hovered" : undefined
                };
            }
        },

        "toolbar-dolab-menubutton" :
        {
            alias : "toolbar-dolab-button",
            include : "toolbar-dolab-button",

            style : function(states) {
                return {
                    showArrow : true
                };
            }
        },

        "toolbar-dolab-menubutton/arrow" :
        {
            alias : "image",
            include : "image",

            style : function(states) {
                return {
                    source : "decoration/arrows/down-small.png"
                };
            }
        },

        "toolbar-dolab-splitbutton" :
        {
            style : function(states) {
                return {
                    marginTop : 2,
                    marginBottom : 2
                };
            }
        },

        "toolbar-dolab-splitbutton/button" :
        {
            alias : "toolbar-dolab-button",
            include : "toolbar-dolab-button",

            style : function(states) {
                return {
                    icon : "decoration/arrows/down.png",
                    marginTop : undefined,
                    marginBottom : undefined
                };
            }
        },

        "toolbar-dolab-splitbutton/arrow" :
        {
            alias : "toolbar-dolab-button",
            include : "toolbar-dolab-button",

            style : function(states) {
                return {
                    padding : states.pressed || states.checked ? 1 : states.hovered ? 1 : 3,
                    icon : "decoration/arrows/down.png",
                    marginTop : undefined,
                    marginBottom : undefined
                };
            }
        },

        "toolbar-dolab-separator" :
        {
            style : function(states) {
                return {
                    decorator : "toolbar-dolab-separator",
                    margin    : 7
                };
            }
        },

        "menu-dolab" :
        {
            style : function(states) {
                var result =
                {
                    decorator : "menu-dolab",
                    shadow : "shadow-popup",
                    spacingX : 6,
                    spacingY : 1,
                    iconColumnWidth : 16,
                    arrowColumnWidth : 4
                };

                if (states.submenu) {
                    result.position = "right-top";
                    result.offset = [-2, -3];
                }

                return result;
            }
        },

        "menu-dolab-separator" :
        {
            style : function(states) {
                return {
                    height : 0,
                    decorator : "menu-dolab-separator",
                    margin    : [ 4, 2 ]
                }
            }
        },

        "menu-dolab-button" :
        {
            alias : "atom",

            style : function(states) {
                return {
                    decorator : states.selected ? "selected-dolab" : undefined,
                    padding   : [ 4, 6 ]
                };
            }
        },

        "menu-dolab-button/icon" :
        {
            include : "image",

            style : function(states) {
                return {
                    alignY : "middle"
                };
            }
        },

        "menu-dolab-button/label" :
        {
            include : "label",

            style : function(states) {
                return {
                    alignY : "middle",
                    padding : 1
                };
            }
        },

        "menu-dolab-button/shortcut" :
        {
            include : "label",

            style : function(states) {
                return {
                    alignY : "middle",
                    marginLeft : 14,
                    padding : 1
                };
            }
        },

        "menu-dolab-button/arrow" :
        {
            style : function(states) {
                return {
                    source : states.selected ? "decoration/arrows/right-invert.png" : "decoration/arrows/right.png",
                    alignY : "middle"
                };
            }
        },

        "menu-dolab-checkbox" :
        {
            alias : "menu-dolab-button",
            include : "menu-dolab-button",

            style : function(states) {
                return {
                    icon : !states.checked ? undefined :
                            states.selected ? "decoration/menu/checkbox-invert.gif" :
                                    "decoration/menu/checkbox.gif"
                }
            }
        },

        "menu-dolab-radiobutton" :
        {
            alias : "menu-dolab-button",
            include : "menu-dolab-button",

            style : function(states) {
                return {
                    icon : !states.checked ? undefined :
                            states.selected ? "decoration/menu/radiobutton-invert.gif" :
                                    "decoration/menu/radiobutton.gif"
                }
            }
        }
    }
});