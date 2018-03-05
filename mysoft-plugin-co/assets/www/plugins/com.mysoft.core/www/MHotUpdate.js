cordova.define("com.mysoft.core.MHotUpdate", function(require, exports, module) { 
	var exec = require('cordova/exec');

    // 下载离线web包
    exports.downloadWebContent = function(zipUrl,success,fail) {
        exec(success, fail, "MHotUpdate", "downloadWebContent", [zipUrl]);
    };

});
