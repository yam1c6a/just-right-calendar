package com.yam1c6a.justrightcalendar

enum class MarkType(val symbol: String) {
    DOUBLE_CIRCLE("◎"),
    STAR("☆"),
    CIRCLE("〇"),
    TRIANGLE("△"),
    CHECK("✓");

    companion object {
        fun fromString(value: String?): MarkType? {
            return values().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }

        fun orderedMarks(marks: Set<MarkType>): List<MarkType> {
            return values().filter { marks.contains(it) }
        }
    }
}
