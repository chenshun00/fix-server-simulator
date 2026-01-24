// 主应用程序模块
var app = angular.module('fixSimulatorApp', []);

// 配置路由（使用简单的控制器切换）
app.run(function($rootScope) {
    $rootScope.currentView = 'session-status'; // 默认视图
    
    $rootScope.selectView = function(viewName) {
        $rootScope.currentView = viewName;
        
        // 更新活动链接
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => link.classList.remove('active'));
        
        // 根据视图名称激活相应链接
        if(viewName === 'session-status') {
            event.target.classList.add('active');
        } else if(viewName === 'message-query') {
            event.target.classList.add('active');
        }
    };
});