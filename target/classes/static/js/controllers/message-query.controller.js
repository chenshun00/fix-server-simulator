// 报文查询控制器
app.controller('MessageQueryController', ['$scope', 'MessageService', 'ReplyService', '$window', function($scope, MessageService, ReplyService, $window) {
    $scope.messages = [];
    $scope.searchParams = {
        clOrdId: '',
        symbol: ''
    };
    $scope.currentPage = 0;
    $scope.totalPages = 1;
    $scope.pageSize = 10;
    $scope.selectedMessage = null;
    $scope.replyData = {
        replyType: 'new',
        execType: '0',
        ordStatus: '0',
        lastQty: 0,
        cumQty: 0,
        leavesQty: 0,
        rejectText: ''
    };
    
    // 加载消息
    function loadMessages() {
        const params = {
            clOrdId: $scope.searchParams.clOrdId || undefined,
            symbol: $scope.searchParams.symbol || undefined,
            page: $scope.currentPage,
            size: $scope.pageSize
        };
        
        MessageService.getMessages(params).then(function(response) {
            $scope.messages = response.data.content || response.data;
            $scope.totalPages = response.data.totalPages || 1;
        });
    }
    
    // 搜索消息
    $scope.searchMessages = function() {
        $scope.currentPage = 0;
        loadMessages();
    };
    
    // 清空搜索
    $scope.clearSearch = function() {
        $scope.searchParams.clOrdId = '';
        $scope.searchParams.symbol = '';
        $scope.currentPage = 0;
        loadMessages();
    };
    
    // 更改页面
    $scope.changePage = function(page) {
        if(page < 0 || page >= $scope.totalPages) {
            return;
        }
        $scope.currentPage = page;
        loadMessages();
    };
    
    // 获取页码数组
    $scope.getPageNumbers = function() {
        const pages = [];
        const start = Math.max(0, $scope.currentPage - 2);
        const end = Math.min($scope.totalPages, start + 5);
        
        for(let i = start; i < end; i++) {
            pages.push(i);
        }
        return pages;
    };
    
    // 打开回报模态框
    $scope.openReplyModal = function(message) {
        $scope.selectedMessage = message;
        
        // 重置回复数据
        $scope.replyData = {
            replyType: 'new',
            execType: '0',
            ordStatus: '0',
            lastQty: 0,
            cumQty: 0,
            leavesQty: 0,
            rejectText: ''
        };
        
        // 显示模态框
        const modalElement = document.getElementById('replyModal');
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    };
    
    // 发送回报
    $scope.sendReply = function() {
        if(!$scope.selectedMessage) {
            alert('请选择要回复的报文');
            return;
        }
        
        // 构建请求参数
        const clOrdId = $scope.selectedMessage.parsedField.clOrdId;
        if(!clOrdId) {
            alert('报文中没有ClOrdId，无法发送回报');
            return;
        }
        
        let replyPromise;
        
        switch($scope.replyData.replyType) {
            case 'new':
                replyPromise = ReplyService.sendNewOrderAck(clOrdId);
                break;
            case 'partial-fill':
                replyPromise = ReplyService.sendPartialFill({
                    clOrdId: clOrdId,
                    execType: $scope.replyData.execType,
                    ordStatus: $scope.replyData.ordStatus,
                    lastQty: parseFloat($scope.replyData.lastQty),
                    cumQty: parseFloat($scope.replyData.cumQty),
                    leavesQty: parseFloat($scope.replyData.leavesQty)
                });
                break;
            case 'fill':
                replyPromise = ReplyService.sendFill({
                    clOrdId: clOrdId,
                    execType: $scope.replyData.execType,
                    ordStatus: $scope.replyData.ordStatus,
                    lastQty: parseFloat($scope.replyData.lastQty),
                    cumQty: parseFloat($scope.replyData.cumQty),
                    leavesQty: parseFloat($scope.replyData.leavesQty)
                });
                break;
            case 'reject':
                replyPromise = ReplyService.sendReject({
                    clOrdId: clOrdId,
                    text: $scope.replyData.rejectText
                });
                break;
            case 'modify':
                replyPromise = ReplyService.sendModifyConfirmation({
                    clOrdId: clOrdId,
                    execType: $scope.replyData.execType,
                    ordStatus: $scope.replyData.ordStatus
                });
                break;
            case 'cancel':
                replyPromise = ReplyService.sendCancelConfirmation({
                    clOrdId: clOrdId,
                    execType: $scope.replyData.execType,
                    ordStatus: $scope.replyData.ordStatus
                });
                break;
            default:
                alert('未知的回报类型');
                return;
        }
        
        replyPromise.then(function(response) {
            alert('回报发送成功');
            // 关闭模态框
            const modalElement = document.getElementById('replyModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if(modal) {
                modal.hide();
            }
        }).catch(function(error) {
            console.error('回报发送失败:', error);
            alert('回报发送失败: ' + (error.data || error.message || '未知错误'));
        });
    };
    
    // 初始加载
    loadMessages();
}]);