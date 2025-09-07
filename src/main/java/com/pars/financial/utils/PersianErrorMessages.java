package com.pars.financial.utils;

/**
 * Utility class providing Persian error messages for common exceptions
 */
public class PersianErrorMessages {

    // Discount Code related messages
    public static final String DISCOUNT_CODE_NOT_FOUND = "کد تخفیف یافت نشد";
    public static final String DISCOUNT_CODE_EXPIRED = "کد تخفیف منقضی شده است";
    public static final String DISCOUNT_CODE_INACTIVE = "کد تخفیف غیرفعال است";
    public static final String DISCOUNT_CODE_ALREADY_USED = "کد تخفیف قبلاً استفاده شده است";
    public static final String DISCOUNT_CODE_USAGE_LIMIT_REACHED = "محدودیت استفاده از کد تخفیف به پایان رسیده است";
    public static final String DISCOUNT_CODE_ALREADY_EXISTS = "کد تخفیف قبلاً وجود دارد";
    public static final String SERIAL_NUMBER_ALREADY_EXISTS = "شماره سریال قبلاً وجود دارد";
    public static final String MINIMUM_BILL_AMOUNT_NOT_MET = "مبلغ فاکتور کمتر از حداقل مبلغ مورد نیاز است";
    public static final String STORE_NOT_ALLOWED = "کد تخفیف در این فروشگاه قابل استفاده نیست";

    // Gift Card related messages
    public static final String GIFT_CARD_NOT_FOUND = "کارت هدیه یافت نشد";
    public static final String GIFT_CARD_INSUFFICIENT_BALANCE = "موجودی کارت هدیه کافی نیست";
    public static final String GIFT_CARD_EXPIRED = "کارت هدیه منقضی شده است";
    public static final String GIFT_CARD_INACTIVE = "کارت هدیه غیرفعال است";

    // Customer related messages
    public static final String CUSTOMER_NOT_FOUND = "مشتری یافت نشد";
    public static final String CUSTOMER_ALREADY_EXISTS = "مشتری قبلاً وجود دارد";

    // Store related messages
    public static final String STORE_NOT_FOUND = "فروشگاه یافت نشد";
    public static final String STORE_INACTIVE = "فروشگاه غیرفعال است";

    // Transaction related messages
    public static final String TRANSACTION_NOT_FOUND = "تراکنش یافت نشد";
    public static final String TRANSACTION_ALREADY_CONFIRMED = "تراکنش قبلاً تأیید شده است";
    public static final String TRANSACTION_ALREADY_REVERSED = "تراکنش قبلاً برگشت خورده است";
    public static final String TRANSACTION_ALREADY_REFUNDED = "تراکنش قبلاً بازپرداخت شده است";
    public static final String TRANSACTION_NOT_CONFIRMED = "تراکنش هنوز تأیید نشده است";
    public static final String DUPLICATE_TRANSACTION_ID = "شناسه تراکنش تکراری است";

    // Authentication related messages
    public static final String INVALID_CREDENTIALS = "نام کاربری یا رمز عبور اشتباه است";
    public static final String USER_NOT_FOUND = "کاربر یافت نشد";
    public static final String USER_INACTIVE = "حساب کاربری غیرفعال است";
    public static final String INVALID_TOKEN = "توکن نامعتبر است";
    public static final String TOKEN_EXPIRED = "توکن منقضی شده است";
    public static final String INSUFFICIENT_PERMISSIONS = "دسترسی کافی ندارید";

    // Validation messages
    public static final String INVALID_INPUT = "داده ورودی نامعتبر است";
    public static final String REQUIRED_FIELD_MISSING = "فیلد اجباری خالی است";
    public static final String INVALID_FORMAT = "فرمت داده نامعتبر است";
    public static final String INVALID_AMOUNT = "مبلغ نامعتبر است";
    public static final String INVALID_PHONE_NUMBER = "شماره تلفن نامعتبر است";
    public static final String INVALID_EMAIL = "ایمیل نامعتبر است";

    // System messages
    public static final String SYSTEM_ERROR = "خطای سیستمی";
    public static final String DATABASE_ERROR = "خطای پایگاه داده";
    public static final String NETWORK_ERROR = "خطای شبکه";
    public static final String SERVICE_UNAVAILABLE = "سرویس در دسترس نیست";
    public static final String RATE_LIMIT_EXCEEDED = "محدودیت تعداد درخواست";

    // Company related messages
    public static final String COMPANY_NOT_FOUND = "شرکت یافت نشد";
    public static final String COMPANY_INACTIVE = "شرکت غیرفعال است";

    // Item Category related messages
    public static final String ITEM_CATEGORY_NOT_FOUND = "دسته‌بندی کالا یافت نشد";
    public static final String ITEM_CATEGORY_INACTIVE = "دسته‌بندی کالا غیرفعال است";

    /**
     * Get Persian message for a given error code
     * @param errorCode The error code
     * @return Persian error message
     */
    public static String getMessageForErrorCode(int errorCode) {
        return switch (errorCode) {
            case -104 -> DISCOUNT_CODE_NOT_FOUND;
            case -105 -> DISCOUNT_CODE_INACTIVE;
            case -106 -> DISCOUNT_CODE_ALREADY_USED;
            case -107 -> DISCOUNT_CODE_USAGE_LIMIT_REACHED;
            case -108 -> MINIMUM_BILL_AMOUNT_NOT_MET;
            case -109 -> DUPLICATE_TRANSACTION_ID;
            case -110 -> STORE_NOT_FOUND;
            case -111 -> GIFT_CARD_NOT_FOUND;
            case -112 -> TRANSACTION_NOT_FOUND;
            case -113 -> TRANSACTION_ALREADY_CONFIRMED;
            case -114 -> TRANSACTION_ALREADY_REVERSED;
            case -115 -> TRANSACTION_ALREADY_REFUNDED;
            case -116 -> TRANSACTION_NOT_CONFIRMED;
            case -117 -> STORE_NOT_ALLOWED;
            case -118 -> DISCOUNT_CODE_NOT_FOUND;
            case -134 -> COMPANY_NOT_FOUND;
            case -141 -> DISCOUNT_CODE_EXPIRED;
            case -150 -> DISCOUNT_CODE_ALREADY_EXISTS;
            case -151 -> SERIAL_NUMBER_ALREADY_EXISTS;
            default -> SYSTEM_ERROR;
        };
    }
}
