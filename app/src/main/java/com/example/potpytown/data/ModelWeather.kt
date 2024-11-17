package com.example.potpytown.data

import com.google.gson.annotations.SerializedName
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

// 날씨 정보를 담는 데이터 클래스
data class ModelWeather(
    @SerializedName("rainType") var rainType: String = "",      // 강수 형태
    @SerializedName("sky") var sky: String = "",               // 하늘 상태
    @SerializedName("temp") var temp: String = "",             // 기온
    @SerializedName("fcstTime") var fcstTime: String = ""       // 예보 시각
)

@Root(name = "response", strict = false)
data class WEATHER(
    @field:Element(name = "header", required = false)
    var header: HEADER? = null,

    @field:Element(name = "body", required = false)
    var body: BODY? = null
)

@Root(name = "header", strict = false)
data class HEADER(
    @field:Element(name = "resultCode", required = false)
    var resultCode: String? = null,

    @field:Element(name = "resultMsg", required = false)
    var resultMsg: String? = null
)

@Root(name = "body", strict = false)
data class BODY(
    @field:Element(name = "dataType", required = false)
    var dataType: String? = null,

    @field:Element(name = "items", required = false)
    var items: ITEMS? = null,

    @field:Element(name = "numOfRows", required = false)
    var numOfRows: Int? = null,

    @field:Element(name = "pageNo", required = false)
    var pageNo: Int? = null,

    @field:Element(name = "totalCount", required = false)
    var totalCount: Int? = null
)

@Root(name = "items", strict = false)
data class ITEMS(
    @field:ElementList(name = "item", inline = true, required = false)
    var item: List<ITEM>? = null
)

@Root(name = "item", strict = false)
data class ITEM(
    @field:Element(name = "baseDate", required = false)
    var baseDate: String? = null,

    @field:Element(name = "baseTime", required = false)
    var baseTime: String? = null,

    @field:Element(name = "category", required = false)
    var category: String? = null,

    @field:Element(name = "fcstDate", required = false)
    var fcstDate: String? = null,

    @field:Element(name = "fcstTime", required = false)
    var fcstTime: String? = null,

    @field:Element(name = "fcstValue", required = false)
    var fcstValue: String? = null,

    @field:Element(name = "nx", required = false)
    var nx: Int? = null,

    @field:Element(name = "ny", required = false)
    var ny: Int? = null
)