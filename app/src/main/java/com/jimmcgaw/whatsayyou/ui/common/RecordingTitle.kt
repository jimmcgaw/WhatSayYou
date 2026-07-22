package com.jimmcgaw.whatsayyou.ui.common

import java.text.DateFormat
import java.util.Date

fun resolveDisplayTitle(title: String?, recordedAt: Long): String =
    title ?: DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(recordedAt))
