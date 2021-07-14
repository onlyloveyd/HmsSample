package tech.kicky.hms.colorfilter

/**
 * 滤镜
 * author: yidong
 * 2021-07-10
 */
data class Filter(val code: Int = 0, val name: String, var checked: Boolean = false)

val LocalFilters = listOf(
    Filter(0, "原图"),
    Filter(1, "黑白"),
    Filter(2, "棕调"),
    Filter(3, "慵懒"),
    Filter(4, "小苍"),
    Filter(5, "富士"),
    Filter(6, "桃粉"),
    Filter(7, "海盐"),
    Filter(8, "薄荷"),
    Filter(9, "蒹葭"),
    Filter(10, "复古"),
    Filter(11, "棉花糖"),
    Filter(12, "青苔"),
    Filter(13, "日光"),
    Filter(14, "时光"),
    Filter(15, "雾霾蓝"),
    Filter(16, "向日葵"),
    Filter(17, "硬朗"),
    Filter(18, "古铜黄"),
    Filter(19, "黑白调"),
    Filter(20, "黄绿调"),
    Filter(21, "黄调"),
    Filter(22, "绿调"),
    Filter(23, "青调"),
    Filter(24, "紫调"),
)