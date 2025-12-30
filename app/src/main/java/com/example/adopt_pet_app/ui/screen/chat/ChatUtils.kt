package com.example.adopt_pet_app.ui.screen.chat

import java.text.SimpleDateFormat
import java.util.*

object ChatUtils {

    fun formatMessageTime(timestamp: Long): String {
        val messageDate = Date(timestamp)
        val currentDate = Date()

        val messageCalendar = Calendar.getInstance().apply { time = messageDate }
        val currentCalendar = Calendar.getInstance().apply { time = currentDate }

        val isSameDay = messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)

        // Kiểm tra hôm qua
        val yesterdayCalendar = Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val isYesterday = messageCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR) &&
                messageCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR)

        val isSameWeek = messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                messageCalendar.get(Calendar.WEEK_OF_YEAR) == currentCalendar.get(Calendar.WEEK_OF_YEAR)

        return when {
            isSameDay -> {
                // Cùng ngày: hiển thị giờ
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)
            }
            isYesterday -> {
                // Hôm qua
                "Hôm qua ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)}"
            }
            isSameWeek -> {
                // Cùng tuần: hiển thị thứ
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(messageDate)
                "$dayOfWeek ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)}"
            }
            else -> {
                // Khác ngày: hiển thị ngày tháng
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(messageDate)
            }
        }
    }


    fun formatConversationTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp

        return when {
            diff < 60 * 1000 -> "Vừa xong" // Dưới 1 phút
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} phút" // Dưới 1 giờ
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} giờ" // Dưới 1 ngày
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} ngày" // Dưới 1 tuần
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))
        }
    }


    fun formatLastSeenTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp

        return when {
            diff < 60 * 1000 -> "Online" // Dưới 1 phút
            diff < 60 * 60 * 1000 -> "Hoạt động ${diff / (60 * 1000)} phút trước"
            diff < 24 * 60 * 60 * 1000 -> "Hoạt động ${diff / (60 * 60 * 1000)} giờ trước"
            else -> "Hoạt động ${SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(timestamp))}"
        }
    }
}