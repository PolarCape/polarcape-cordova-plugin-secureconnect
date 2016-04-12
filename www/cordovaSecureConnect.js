
    var exec = require('cordova/exec');

    var secureConnect = {
        secureConnect: function(callback, args) {
            exec(
                    function(data) {
                        callback(data);
                    },
                    function(err) {
                        callback(err);
                    },
                    "SecureConnect",
                    'installCertificates',
                    [args]
                    );
        }
    };

    module.exports = secureConnect;