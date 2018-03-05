cordova.define("com.mysoft.core.MicCore", function(require, exports, module) {
    var argscheck = require('cordova/argscheck');
    var exec = require('cordova/exec');

    exports.connect = function(success, error){
        exec(success, error, "MicCore", "connect", []);
    };

    exports.close = function(success, error) {
        exec(success, error, "MicCore", "close", []);
    }

    exports.keepScreenOn = function(screenOn, success, error) {
        exec(success, error, "MicCore", "keepScreenOn",  [screenOn]);
    }

    exports.sentryLogSwitch = function(isOff, success, error, options) {
            exec(success, error, "MicCore", "sentryLogSwitch",  [isOff, options]);
    }

    exports.saveBusiness = function(businessID,success,error){
            exec(success, error, "MicCore", "saveBusiness",  [businessID]);
        };
});