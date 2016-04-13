# polarcape-cordova-plugin-secureconnect

Cordova plugin for installing wifi certificates (Android)

# Install


# Usage

          secureConnect.secureConnect(callback, args);

# Example
          var usernameAndPass = {'username': $scope.userId, 'password': $scope.wifiPassword};
          secureConnect.secureConnect(function(data) {
             logDebug("SECURE CONNECT INSTALL CERTIFICATE : " + JSON.stringify(data));
         }, usernameAndPass);
