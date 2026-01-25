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
        if(params.sessionKey !== undefined && params.sessionKey !== '') {
            queryParams.append('sessionKey', params.sessionKey);
        }
        if(params.msgType !== undefined && params.msgType !== '') {
            queryParams.append('msgType', params.msgType);
        }
        if(params.startTime !== undefined && params.startTime !== '') {
            queryParams.append('startTime', params.startTime);
        }
        if(params.endTime !== undefined && params.endTime !== '') {
            queryParams.append('endTime', params.endTime);
        }
        queryParams.append('page', params.page || 0);
        queryParams.append('size', params.size || 10);

        return $http.get('/api/messages?' + queryParams.toString());
    };
}]);