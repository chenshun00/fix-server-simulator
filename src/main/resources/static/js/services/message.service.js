// Message服务
app.service('MessageService', ['$http', function($http) {
    var service = this;
    
    service.getMessages = function(params) {
        // 构建查询参数
        let queryParams = new URLSearchParams();
        if(params.clOrdId !== undefined && params.clOrdId !== '') {
            queryParams.append('clOrdId', params.clOrdId);
        }
        if(params.symbol !== undefined && params.symbol !== '') {
            queryParams.append('symbol', params.symbol);
        }
        queryParams.append('page', params.page || 0);
        queryParams.append('size', params.size || 10);
        
        return $http.get('/api/messages/search?' + queryParams.toString());
    };
}]);