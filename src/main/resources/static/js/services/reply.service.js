// Reply服务
app.service('ReplyService', ['$http', function($http) {
    var service = this;
    
    service.sendNewOrderAck = function(clOrdId) {
        const params = new URLSearchParams();
        params.append('clOrdId', clOrdId);
        
        return $http.post('/api/response/execution-report/new', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
    
    service.sendPartialFill = function(data) {
        const params = new URLSearchParams();
        params.append('clOrdId', data.clOrdId);
        params.append('execType', data.execType);
        params.append('ordStatus', data.ordStatus);
        params.append('lastQty', data.lastQty);
        params.append('cumQty', data.cumQty);
        params.append('leavesQty', data.leavesQty);
        
        return $http.post('/api/response/execution-report/partial-fill', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
    
    service.sendFill = function(data) {
        const params = new URLSearchParams();
        params.append('clOrdId', data.clOrdId);
        params.append('execType', data.execType);
        params.append('ordStatus', data.ordStatus);
        params.append('lastQty', data.lastQty);
        params.append('cumQty', data.cumQty);
        params.append('leavesQty', data.leavesQty);
        
        return $http.post('/api/response/execution-report/fill', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
    
    service.sendReject = function(data) {
        const params = new URLSearchParams();
        params.append('clOrdId', data.clOrdId);
        if(data.text) {
            params.append('text', data.text);
        }
        
        return $http.post('/api/response/reject', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
    
    service.sendModifyConfirmation = function(data) {
        const params = new URLSearchParams();
        params.append('clOrdId', data.clOrdId);
        params.append('execType', data.execType);
        params.append('ordStatus', data.ordStatus);
        
        return $http.post('/api/response/execution-report/modify', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
    
    service.sendCancelConfirmation = function(data) {
        const params = new URLSearchParams();
        params.append('clOrdId', data.clOrdId);
        params.append('execType', data.execType);
        params.append('ordStatus', data.ordStatus);
        
        return $http.post('/api/response/execution-report/cancel', params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
    };
}]);