package com.yam1c6a.justrightcalendar

enum class MarkType(val symbol: String) {
    CIRCLE("〇"),
    CHECK("✓"),
    STAR("☆");

    companion object {
        fun fromString(value: String?): MarkType? {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}
