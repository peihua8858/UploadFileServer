package com.example.uploadfileserver

data class SiteMessageData(
    /**
     * 短信模板参数项目名称
     */
    val projectName: String,
    /**
     * 短信模板参数错误率
     */
    val errorRate: String,
    /**
     * 短信模板参数错误信息
     */
    val errorMsg: String,
    /**
     * 短信模板参数涉及接口
     */
    val plat: String

){
    override fun toString(): String {
        return "{'projectName':'$projectName','errorRate':'$errorRate','errorMsg':'$errorMsg','plat':'$plat'}"
    }

}