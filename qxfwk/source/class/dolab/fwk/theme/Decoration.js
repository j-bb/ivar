/* ************************************************************************

 Copyright:

 License:

 Authors:

 #asset(dolab/fwk/decoration/*)

 ************************************************************************ */

qx.Theme.define("dolab.fwk.theme.Decoration",
{
    extend : qx.theme.modern.Decoration,

    decorations :
    {
        "selected-dolab" :
        {
            decorator : qx.ui.decoration.Background,

            style :
            {
                backgroundColor : "#b5e0ff",
                backgroundRepeat : "scale"
            }
        },

        /*
         ---------------------------------------------------------------------------
         TOOLBAR-dolab
         ---------------------------------------------------------------------------
         */

        "toolbar-dolab" :
        {
            decorator : qx.ui.decoration.Background,

            style :
            {
                backgroundColor : "#8CCBFF",
                //backgroundImage : "decoration/toolbar/toolbar-gradient-dolab.png",
                backgroundRepeat : "scale"
            }
        },

// 2017
//        "toolbar-dolab-button-hovered" :
//        {
//            decorator : qx.ui.decoration.Beveled,
//
//            style :
//            {
//                outerColor : "#d8f0ff",
//                innerColor : "#c9eaff",
//                backgroundColor : "#B5E0FF",
//                //backgroundImage : "decoration/form/button-c.png",
//                backgroundRepeat : "scale"
//            }
//        },

// 2017
//        "toolbar-dolab-button-checked" :
//        {
//            decorator : qx.ui.decoration.Beveled,
//
//            style :
//            {
//                outerColor : "#d8f0ff",
//                innerColor : "#c9eaff",
//                backgroundColor : "#B5E0FF",
//                //backgroundImage : "decoration/form/button-checked-c.png",
//                backgroundRepeat : "scale"
//            }
//        },

        "toolbar-dolab-separator" :
        {
            decorator : qx.ui.decoration.Single,

            style :
            {
                widthLeft : 1,
                widthRight : 1,

                colorLeft : "#b8b8b8",
                colorRight : "#f4f4f4",

                styleLeft : "solid",
                styleRight : "solid"
            }
        },

        "toolbar-dolab-part" :
        {
            decorator : qx.ui.decoration.Background,

            style :
            {
                backgroundImage  : "decoration/toolbar/toolbar-part.gif",
                backgroundRepeat : "repeat-y"
            }
        },

        "menu-dolab" :
        {
            decorator : qx.ui.decoration.Single,

            style :
            {
                backgroundColor  : "#8CCBFF",

                width : 1,
                color : "border-main",
                style : "solid"
            }
        },

        "menu-dolab-separator" :
        {
            decorator : qx.ui.decoration.Single,

            style :
            {
                backgroundColor: "#8CCBFF",

                widthTop    : 1,
                colorTop    : "#d8f0ff",

                widthBottom : 1,
                colorBottom : "#c9eaff"
            }
        },

        /* Selectbox error */
        "button-error" :
        {
            decorator : qx.ui.decoration.Grid,

            style :
            {
                baseImage : "dolab/fwk/decoration/form/button-error.png",
                insets    : 2
            }
        }
    }
});