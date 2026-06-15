package com.levonty.unitalk.data.local

import androidx.room.TypeConverter
import com.levonty.unitalk.data.model.Gender
import com.levonty.unitalk.data.model.Interest
import com.levonty.unitalk.data.model.Language
import com.levonty.unitalk.data.model.MessageStatus
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter fun fromGender(v: Gender?): String? = v?.name
    @TypeConverter fun toGender(v: String?): Gender? = v?.let { Gender.valueOf(it) }

    @TypeConverter fun fromMessageStatus(v: MessageStatus): String = v.name
    @TypeConverter fun toMessageStatus(v: String): MessageStatus = MessageStatus.valueOf(v)

    @TypeConverter
    fun fromLanguages(list: List<Language>): String {
        val arr = JSONArray()
        list.forEach { lang ->
            arr.put(JSONObject().apply {
                put("code", lang.code)
                put("nameRu", lang.nameRu)
                put("nameEn", lang.nameEn)
                put("level", lang.level)
                put("isNative", lang.isNative)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toLanguages(json: String): List<Language> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Language(
                code = o.getString("code"),
                nameRu = o.getString("nameRu"),
                nameEn = o.getString("nameEn"),
                level = o.getInt("level"),
                isNative = o.getBoolean("isNative")
            )
        }
    }

    @TypeConverter
    fun fromInterests(list: List<Interest>): String {
        val arr = JSONArray()
        list.forEach { interest ->
            arr.put(JSONObject().apply {
                put("id", interest.id)
                put("nameRu", interest.nameRu)
                put("nameEn", interest.nameEn)
                put("namePl", interest.namePl)
                put("nameDe", interest.nameDe)
                put("category", interest.category)
                put("usageCount", interest.usageCount)
            })
        }
        return arr.toString()
    }

    @TypeConverter
    fun toInterests(json: String): List<Interest> {
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Interest(
                id = o.getLong("id"),
                nameRu = o.getString("nameRu"),
                nameEn = o.getString("nameEn"),
                namePl = o.optString("namePl", ""),
                nameDe = o.optString("nameDe", ""),
                category = o.optString("category", ""),
                usageCount = o.optInt("usageCount", 0)
            )
        }
    }
}