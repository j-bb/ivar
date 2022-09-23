qx.Class.define("dolab.fwk.helper.ImageHelper", {
    statics : {
        getScaledDimensions : function(width, height, max) {
            var result = { width : width, height : height };

            if (width != null && height != null) {
                if (width > max || height > max) {
                    if (height > width) {
                        result.height = max;
                        result.width = (max * width / height);
                    } else {
                        result.width = max;
                        result.height = (max * height / width);
                    }
                }
            } else {
                result.width = max;
                result.height = max;
            }

            return result;
        }
    }
});