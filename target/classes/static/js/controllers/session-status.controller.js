// Session状态控制器
app.controller('SessionStatusController', ['$scope', 'SessionService', function($scope, SessionService) {
    $scope.sessions = {};
    
    // 初始化加载会话数据
    function loadSessions() {
        SessionService.getSessions().then(function(response) {
            $scope.sessions = response.data;
        });
    }
    
    // 初始加载
    loadSessions();
    
    // 设置定时刷新（每5秒）
    setInterval(loadSessions, 5000);
}]);