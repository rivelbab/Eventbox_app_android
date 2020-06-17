package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.event.Categories

class CategoryConverter {

    @TypeConverter
    fun toCategories(value: String?) : Categories {
        if (value == null || value.isEmpty()) {
            return Categories()
        }
        val list: List<String> = value.split(",")
        val catList = ArrayList<String>()
        for (item in list) {
            if(item.isNotEmpty()) {
                catList.add(item)
            }
        }
        return Categories(catList)
    }

    @TypeConverter
    fun toString(categories: Categories?): String {
        var string = ""

        if(categories == null){
            return string
        }
        categories.categories.forEach {
            string += "$it,"
        }
        return string
    }
}