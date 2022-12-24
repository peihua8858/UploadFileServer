onmessage = function (message) {
    const content = message.data;
    const msg = buildMessage(content);
    if (msg === undefined) {
        return
    }
    postMessage(msg)
};
Date.prototype.Format = function (fmt) { //author: meizz
    const o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (const k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}
/**
 * 系统消息
 * @type {number}
 */
const SYSTEM_MSG_CODE = 0;
/**
 * 用户消息
 * @type {number}
 */
const USER_MSG_CODE = 1;

//将消息显示在网页上
function buildMessage(messages) {
    const data = messages.data;
    let content = {};
    try {
        content = JSON.parse(data);
    } catch (err) {
        content = JSON.parse(JSON.stringify(data));
        console.log(err.message);
    }
    const code = parseInt(content.code);
    const message = content.msg;
    if (isNull(message)) {
        return undefined;
    }
    let str = "";
    if (code === SYSTEM_MSG_CODE) {
        //系统消息
        str = "<div class='logCat'>" + "<span style='color:red;'>系统消息：</span>" + "<span>" + message + "</span>" + "<br/>" + "</div>";
    } else if (code === USER_MSG_CODE) {
        try {
            content = JSON.parse(message);
        } catch (err) {
            content = JSON.parse(JSON.stringify(message));
            try {
                content = JSON.parse(message);
            } catch (err) {
                content = ""
                str = "<div class='logCat'>"
                    + "<div> ------ " + new Date(Date.now()).Format("yyyy-MM-dd hh:mm:ss")  + " ------ </div>"
                    + "<div style='font-size: medium'>" + message + "</div><br/>"
                    + "</div>";
            }
        }
        if (isNull(content)) {
            return str;
        }
        const body = content.body;
        if (isNull(body)) {
            return "<div class='logCat'>"
                + "<div> ------ " + new Date(Date.now()).Format("yyyy-MM-dd hh:mm:ss") + " ------ </div>"
                + "<div style='font-size: medium'>" + message + "</div><br/>"
                + "</div>";
        }
        let level = content.level;
        const url = content.url;
        // const curSystem = messages.curSystem;
        const platform = messages.platform;
        // if (curSystem === 2) {
        //     //过滤非ios数据
        //     if (platform.toLowerCase().indexOf("ios") < 0) {
        //         return undefined;
        //     }
        // }
        // if (curSystem === 1) {
        //     //过滤非Android数据
        //     if (platform.toLowerCase().indexOf("android") < 0) {
        //         return undefined;
        //     }
        // }
        const curProject = messages.curProject;
        if (!isNull(curProject)) {
            //项目过滤
            if (curProject.toLowerCase() !== content.appName.toLowerCase()) {
                return undefined;
            }
        }
        if (!isNull(platform)) {
            //标签过滤
            if (platform.toLowerCase() !== content.platform.toLowerCase()) {
                return undefined;
            }
        }
        const keyword = messages.keyword;
        if (!isNull(keyword)) {
            //关键词过滤
            let keywordLowerCase = keyword.toLowerCase()
            if (body.toLowerCase().indexOf(keywordLowerCase) < 0
                && level.toLowerCase().indexOf(keywordLowerCase) < 0
                && url.toLowerCase().indexOf(keywordLowerCase) < 0
                &&platform.toLowerCase().indexOf(keywordLowerCase)<0
            ) {
                return undefined;
            }
        }
        //过滤掉.google链接
        if (url != null && url.toLowerCase().indexOf('\.google') > 0) {
            return undefined;
        }
        if (isNull(level)) {
            level = ""
        } else {
            level += " : "
        }
        str = "<div class='logCat'>"
            + "<div> ------ " + level + new Date(content.timestamp).Format("yyyy-MM-dd hh:mm:ss") + " ------ </div>"
            + "<div style='font-size: medium'>" + body + "</div><br/>"
            + "</div>";
    }
    return str;
}

function isNull(data) {
    return (data === "" || data === undefined || data == null);
}