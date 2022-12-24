/**
 * 处理控制台日志打印按钮操作
 */
$(function () {
    // 禁止滚动条
    $(document.body).css({
        "overflow-x": "hidden",
        "overflow-y": "hidden"
    });
    $(document).ready(function () {
        openSocket()
    });
    const keyword = sessionStorage.getItem("keyword");
    $("#keywordFilter").val(keyword);
    $("#keywordFilter").on('input propertychange', function () {
        const keyword = $("#keywordFilter").val();
        removeAllMessage(keyword, "keyword");
        sessionStorage.setItem("keyword", keyword);
        $("#log_console").empty();
    });
    $("#openLog").click(function () {
        openSocket();
    });

    $("#closeLog").click(function () {
        mouseHover = false;
        closeWebSocket();
    });
    $("#clearLog").click(function () {
        mouseHover = false;
        $("#log_console").empty();
    });
    //关键词过滤
    $("#keywordFilter").change(function () {
        const keyword = $("#keywordFilter").val();
        removeAllMessage(keyword, "keyword");
        sessionStorage.setItem("keyword", keyword);
    });
    $('div.logContent').mousewheel(function (event, delta, deltaX, deltaY) {
        //监听鼠标滚轮，向上滚动，则停止自动滚动，向下滚动则自动滚动到底部
        if (delta <= 0) {
            const nScrollHeight = $(this)[0].scrollHeight;
            const nScrollTop = $(this)[0].scrollTop;
            const nDivHeight = Math.round($("#log_console").height());
            const paddingBottom = parseInt($(this).css('padding-bottom')),
                paddingTop = parseInt($(this).css('padding-top'));
            const scrollToBottom = (nScrollTop + nDivHeight + paddingBottom + paddingTop) >= nScrollHeight;
            mouseHover = !scrollToBottom;
            postShowMsg();
        } else {
            mouseHover = delta > 0;
        }
    });
});

const divMessages = [];

/**
 * 根据条件删除缓存的历史记录
 * @param changeValue
 * @param valueName
 * @author dingpeihua
 * @date 2019/10/22 14:32
 * @version 1.0
 */
function removeAllMessage(changeValue, valueName) {
    const oldValue = sessionStorage.getItem(valueName);
    if (changeValue !== oldValue) {
        divMessages.splice(0, divMessages.length);
    }
}

let allMsgNumber = 0;
let websocket = null;
const maxLogCount = 30;
let lastHeartBeat;
let mouseHover = false;

//关闭连接
function closeWebSocket() {
    websocket.close();
}

/**
 * 系统消息
 * @type {number}
 */
const SYSTEM_MSG_CODE = 0;

function openSocket() {
    mouseHover = false;
    if (websocket != null) {
        closeWebSocket()
    }
    lastHeartBeat = new Date().getTime();
    //判断当前浏览器是否支持WebSocket
    let curSchema = window.location.protocol;
    let socketSchema = "ws://";
    if (curSchema.indexOf("https") >= 0) {
        socketSchema = "wss://";
    }
    let socketUrl = socketSchema + window.location.host + "/websocket?platform=5566" ;
    if ('WebSocket' in window) {
        websocket = new WebSocket(socketUrl);
    } else if ('MozWebSocket' in window) {
        websocket = new MozMozWebSocket(socketUrl);
    } else {
        alert("你的浏览器不支持");
        return;
    }
    websocket.onerror = function () {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接错误，请重试..."}));
    };

    //连接成功建立的回调方法
    websocket.onopen = function (event) {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接成功了..."}));
    };
    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        lastHeartBeat = new Date().getTime();
        postMessage(event.data);
    };
    //连接关闭的回调方法
    websocket.onclose = function () {
        postMessage(JSON.stringify({code: SYSTEM_MSG_CODE, msg: "连接关闭了..."}));
    };
    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = function () {
        websocket.close();
    }
}

const worker = new Worker("static/js/task.js");
worker.onmessage = function (message) {
    if (divMessages.length >= maxLogCount) {
        divMessages.shift();
    }
    divMessages.push(message.data);
    postShowMsg();
};

worker.onerror = function (error) {
    console.log(error.filename, error.lineno, error.message);
};

function postShowMsg() {
    if (divMessages.length > 0 && !mouseHover) {
        showMessage(divMessages.shift());
        console.log('messages.length :' + divMessages.length);
        if (divMessages.length > 0 && !mouseHover) {
            setTimeout(postShowMsg, 200);
        }
    }
}

function postMessage(message) {
    const messages = {};
    // messages.curSystem = sessionStorage.getItem("cur_system");
    messages.data = message;
    messages.keyword = sessionStorage.getItem("keyword");
    // messages.platform = sessionStorage.getItem("platform");
    // messages.curProject = sessionStorage.getItem("cur_project");
    worker.postMessage(messages);
    allMsgNumber = allMsgNumber + 1;
}

//将消息显示在网页上
function showMessage(data) {
    const div = document.getElementById('log_console');
    const length = div.childElementCount;
    if (length >= maxLogCount) {
        const childD = div.childNodes.item(length - (maxLogCount - 1));
        $(childD).prevAll().remove();
    }
    const childDiv = document.createElement("div");
    let content = data;
    try {
        //解码url
        content = decodeURIComponent(data)
    } catch (e) {
        console.log("error:" + e.message)
    }
    childDiv.innerHTML = content;
    div.appendChild(childDiv);
    div.scrollTop = div.scrollHeight;//这里是关键的实现
}

function isNull(data) {
    return (data == "" || data == undefined || data == null);
}
