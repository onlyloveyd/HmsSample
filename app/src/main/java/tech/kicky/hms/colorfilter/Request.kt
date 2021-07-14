package tech.kicky.hms.colorfilter

import org.json.JSONObject

/**
 * Auth Json
 * author: yidong
 * 2021-07-10
 */
data class RequestJson(
    val requestId: String = "",
    val taskJson: TaskJson,
    val authJson: AuthJson
)

data class TaskJson(
    // 颜色映射的图片索引，索引范围[0,24]（0为原图）
    val filterType: Int,
    // 滤镜强度，取值范围[0,1.0]，默认为1.0。
    val intensity: Float,
    // 压缩率，取值范围（0,1.0]，默认为1.0。
    val compressRate: Float
)


data class AuthJson(
    val projectId: String,
    val appId: String,
    val authApiKey: String,
    val clientSecret: String,
    val clientId: String,
)


var stringLocal =
    "{\"projectId\":\"736430079245736563\",\"appId\":\"104358187\",\"authApiKey\":\"CgB6e3x9E+mUHoMgSSeRUxNHXL6I2pyf6Ri4BzqWB7eXOtB+EWUqFs1TjDFR911yB8Lw2o/6YOnnXJa93gch8UPo\",\"clientSecret\":\"930975B5741441EAC4D95A6B125EA3445B4E3EEAB247BDD5A4B6B5FFC388B17F\",\"clientId\":\"635146018899374464\",\"token\":\"tokenTest\"}"

val LocalAuthJson = JSONObject(stringLocal)