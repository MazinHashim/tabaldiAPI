package com.tabaldi.api.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.List;
import java.util.Locale;

public class MessagesUtils {

    public static String getFetchMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("success.data.fetching", args, locale);
    }
    public static String getSavedDataMessage(MessageSource messageSource, String en, String ar, String tyEn, String tyAr) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en, langCheck ? tyAr : tyEn).toArray(new String[0]);
        return messageSource.getMessage("success.data.saving", args, locale);
    }

    public static String getDeletedMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("success.data.deleting", args, locale);
    }
    public static String getCategoryPublishMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("success.category.publish", args, locale);
    }
    public static String getStatusChangedMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("success.order.status.changed", args, locale);
    }

    public static String getNotFoundMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.not.found", args, locale);
    }
    public static String getAlreadySelectedMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.already.selected", args, locale);
    }

    public static String getOrderStatusMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.order.current.status", args, locale);
    }

    public static String getAlreadyExistMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.already.exist", args, locale);
    }
    public static String getAlreadyHasPendingOrderMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.pending.order", args, locale);
    }
    public static String getMismatchRoleMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.mismatch.role", args, locale);
    }
    public static String getNotChangeUserMessage(MessageSource messageSource, String en, String ar) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en).toArray(new String[0]);
        return messageSource.getMessage("error.change.user.not.allowed", args, locale);
    }

    public static String getInvalidFormatMessage(MessageSource messageSource, String en, String ar, String tyEn, String tyAr) {
        Locale locale = LocaleContextHolder.getLocale();
        boolean langCheck = locale.getLanguage().equalsIgnoreCase("ar");
        String[] args = List.of(langCheck ? ar : en, langCheck ? tyAr : tyEn).toArray(new String[0]);
        return messageSource.getMessage("error.invalid.format", args, locale);
    }
}
