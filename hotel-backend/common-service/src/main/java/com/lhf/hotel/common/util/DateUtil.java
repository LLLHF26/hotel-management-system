package com.lhf.hotel.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具
 */
public final class DateUtil {

    private DateUtil() {
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter COMPACT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 今天 yyyyMMdd（用于订单号） */
    public static String todayStr() {
        return LocalDate.now().format(COMPACT_DATE);
    }

    /** LocalDate → yyyy-MM-dd */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    /** LocalDateTime → yyyy-MM-dd HH:mm:ss */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FMT) : "";
    }

    /** 计算两个日期之间的天数 */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /** 解析日期字符串 */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FMT);
    }
}
