// Session服务
app.service('SessionService', ['$http', function($http) {
    var service = this;
    
    service.getSessions = function() {
        return $http.get('/api/sessions');
    };
}]);