package com.example.just_right_calendar

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
