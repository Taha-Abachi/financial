package com.pars.financial.constants;

/**
 * Centralized error codes with English and Persian messages
 * This provides a single source of truth for all error codes and their translations
 */
public class ErrorCodes {
    
    // ===== SUCCESS CODES =====
    public static final ErrorCode SUCCESS = new ErrorCode(0, "Success", "موفقیت");
    
    // ===== GENERAL ERROR CODES =====
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode(-1, "System error", "خطای سیستمی");
    public static final ErrorCode DATABASE_ERROR = new ErrorCode(-2, "Database error", "خطای پایگاه داده");
    public static final ErrorCode NETWORK_ERROR = new ErrorCode(-3, "Network error", "خطای شبکه");
    public static final ErrorCode SERVICE_UNAVAILABLE = new ErrorCode(-4, "Service unavailable", "سرویس در دسترس نیست");
    public static final ErrorCode RATE_LIMIT_EXCEEDED = new ErrorCode(-5, "Rate limit exceeded", "محدودیت تعداد درخواست");
    public static final ErrorCode INVALID_REQUEST = new ErrorCode(-6, "Invalid request", "درخواست نامعتبر");
    public static final ErrorCode UNAUTHORIZED = new ErrorCode(-7, "Unauthorized", "دسترسی غیرمجاز");
    public static final ErrorCode FORBIDDEN = new ErrorCode(-8, "Forbidden", "دسترسی ممنوع");
    public static final ErrorCode NOT_FOUND = new ErrorCode(-9, "Not found", "یافت نشد");
    public static final ErrorCode CONFLICT = new ErrorCode(-10, "Conflict", "تداخل");
    
    // ===== VALIDATION ERROR CODES =====
    public static final ErrorCode VALIDATION_ERROR = new ErrorCode(-100, "Validation error", "خطای اعتبارسنجی");
    public static final ErrorCode REQUIRED_FIELD_MISSING = new ErrorCode(-101, "Required field missing", "فیلد اجباری خالی است");
    public static final ErrorCode INVALID_FORMAT = new ErrorCode(-102, "Invalid format", "فرمت نامعتبر");
    public static final ErrorCode INVALID_AMOUNT = new ErrorCode(-103, "Invalid amount", "مبلغ نامعتبر");
    public static final ErrorCode INVALID_PHONE_NUMBER = new ErrorCode(-104, "Invalid phone number", "شماره تلفن نامعتبر");
    public static final ErrorCode INVALID_EMAIL = new ErrorCode(-105, "Invalid email", "ایمیل نامعتبر");
    public static final ErrorCode INVALID_DATE = new ErrorCode(-106, "Invalid date", "تاریخ نامعتبر");
    public static final ErrorCode INVALID_STATUS = new ErrorCode(-107, "Invalid status", "وضعیت نامعتبر");
    public static final ErrorCode INVALID_ROLE = new ErrorCode(-108, "Invalid role", "نقش نامعتبر");
    
    // ===== CUSTOMER ERROR CODES =====
    public static final ErrorCode CUSTOMER_NOT_FOUND = new ErrorCode(-200, "Customer not found", "مشتری یافت نشد");
    public static final ErrorCode CUSTOMER_ALREADY_EXISTS = new ErrorCode(-201, "Customer already exists", "مشتری قبلاً وجود دارد");
    public static final ErrorCode CUSTOMER_INACTIVE = new ErrorCode(-202, "Customer inactive", "مشتری غیرفعال");
    public static final ErrorCode CUSTOMER_PHONE_REQUIRED = new ErrorCode(-203, "Customer phone number required", "شماره تلفن مشتری الزامی است");
    
    // ===== STORE ERROR CODES =====
    public static final ErrorCode STORE_NOT_FOUND = new ErrorCode(-300, "Store not found", "فروشگاه یافت نشد");
    public static final ErrorCode STORE_ALREADY_EXISTS = new ErrorCode(-301, "Store already exists", "فروشگاه قبلاً وجود دارد");
    public static final ErrorCode STORE_INACTIVE = new ErrorCode(-302, "Store inactive", "فروشگاه غیرفعال");
    public static final ErrorCode STORE_NOT_ALLOWED = new ErrorCode(-303, "Store not allowed", "فروشگاه مجاز نیست");
    
    // ===== COMPANY ERROR CODES =====
    public static final ErrorCode COMPANY_NOT_FOUND = new ErrorCode(-400, "Company not found", "شرکت یافت نشد");
    public static final ErrorCode COMPANY_ALREADY_EXISTS = new ErrorCode(-401, "Company already exists", "شرکت قبلاً وجود دارد");
    public static final ErrorCode COMPANY_INACTIVE = new ErrorCode(-402, "Company inactive", "شرکت غیرفعال");
    
    // ===== DISCOUNT CODE ERROR CODES =====
    public static final ErrorCode DISCOUNT_CODE_NOT_FOUND = new ErrorCode(-500, "Discount code not found", "کد تخفیف یافت نشد");
    public static final ErrorCode DISCOUNT_CODE_ALREADY_EXISTS = new ErrorCode(-501, "Discount code already exists", "کد تخفیف قبلاً وجود دارد");
    public static final ErrorCode DISCOUNT_CODE_ALREADY_USED = new ErrorCode(-502, "Discount code already used", "کد تخفیف قبلاً استفاده شده است");
    public static final ErrorCode DISCOUNT_CODE_EXPIRED = new ErrorCode(-503, "Discount code expired", "کد تخفیف منقضی شده است");
    public static final ErrorCode DISCOUNT_CODE_INACTIVE = new ErrorCode(-504, "Discount code inactive", "کد تخفیف غیرفعال");
    public static final ErrorCode DISCOUNT_CODE_USAGE_LIMIT_REACHED = new ErrorCode(-505, "Discount code usage limit reached", "محدودیت استفاده از کد تخفیف به پایان رسیده است");
    public static final ErrorCode MINIMUM_BILL_AMOUNT_NOT_MET = new ErrorCode(-506, "Minimum bill amount not met", "مبلغ فاکتور کمتر از حداقل مبلغ مورد نیاز است");
    public static final ErrorCode DISCOUNT_CODE_INVALID = new ErrorCode(-507, "Invalid discount code", "کد تخفیف نامعتبر");
    
    // ===== GIFT CARD ERROR CODES =====
    public static final ErrorCode GIFT_CARD_NOT_FOUND = new ErrorCode(-600, "Gift card not found", "کارت هدیه یافت نشد");
    public static final ErrorCode GIFT_CARD_ALREADY_EXISTS = new ErrorCode(-601, "Gift card already exists", "کارت هدیه قبلاً وجود دارد");
    public static final ErrorCode GIFT_CARD_INSUFFICIENT_BALANCE = new ErrorCode(-602, "Gift card insufficient balance", "موجودی کارت هدیه کافی نیست");
    public static final ErrorCode GIFT_CARD_EXPIRED = new ErrorCode(-603, "Gift card expired", "کارت هدیه منقضی شده است");
    public static final ErrorCode GIFT_CARD_INACTIVE = new ErrorCode(-604, "Gift card inactive", "کارت هدیه غیرفعال");
    public static final ErrorCode GIFT_CARD_INVALID = new ErrorCode(-605, "Invalid gift card", "کارت هدیه نامعتبر");
    public static final ErrorCode SERIAL_NUMBER_ALREADY_EXISTS = new ErrorCode(-606, "Serial number already exists", "شماره سریال قبلاً وجود دارد");
    public static final ErrorCode GIFT_CARD_CUSTOMER_MISMATCH = new ErrorCode(-607, "Gift card is already associated with a different customer", "کارت هدیه قبلاً به مشتری دیگری اختصاص یافته است");
    public static final ErrorCode GIFT_CARD_CUSTOMER_REQUIRED = new ErrorCode(-608, "Customer is required for gift card transaction", "مشتری برای تراکنش کارت هدیه الزامی است");
    public static final ErrorCode GIFT_CARD_DATA_INTEGRITY_ERROR = new ErrorCode(-609, "Gift card data integrity error - used but no customer assigned", "خطای یکپارچگی داده کارت هدیه - استفاده شده اما مشتری اختصاص نیافته");
    
    // ===== TRANSACTION ERROR CODES =====
    public static final ErrorCode TRANSACTION_NOT_FOUND = new ErrorCode(-700, "Transaction not found", "تراکنش یافت نشد");
    public static final ErrorCode TRANSACTION_ALREADY_CONFIRMED = new ErrorCode(-701, "Transaction already confirmed", "تراکنش قبلاً تأیید شده است");
    public static final ErrorCode TRANSACTION_ALREADY_REVERSED = new ErrorCode(-702, "Transaction already reversed", "تراکنش قبلاً برگشت خورده است");
    public static final ErrorCode TRANSACTION_ALREADY_REFUNDED = new ErrorCode(-703, "Transaction already refunded", "تراکنش قبلاً بازپرداخت شده است");
    public static final ErrorCode TRANSACTION_NOT_CONFIRMED = new ErrorCode(-704, "Transaction not confirmed", "تراکنش هنوز تأیید نشده است");
    public static final ErrorCode DUPLICATE_TRANSACTION_ID = new ErrorCode(-705, "Duplicate transaction ID", "شناسه تراکنش تکراری است");
    public static final ErrorCode TRANSACTION_INVALID = new ErrorCode(-706, "Invalid transaction", "تراکنش نامعتبر");
    public static final ErrorCode TRANSACTION_FAILED = new ErrorCode(-707, "Transaction failed", "تراکنش ناموفق");
    
    // ===== AUTHENTICATION ERROR CODES =====
    public static final ErrorCode INVALID_CREDENTIALS = new ErrorCode(-800, "Invalid credentials", "نام کاربری یا رمز عبور اشتباه است");
    public static final ErrorCode USER_NOT_FOUND = new ErrorCode(-801, "User not found", "کاربر یافت نشد");
    public static final ErrorCode USER_ALREADY_EXISTS = new ErrorCode(-802, "User already exists", "کاربر قبلاً وجود دارد");
    public static final ErrorCode USER_INACTIVE = new ErrorCode(-803, "User inactive", "حساب کاربری غیرفعال");
    public static final ErrorCode INVALID_TOKEN = new ErrorCode(-804, "Invalid token", "توکن نامعتبر");
    public static final ErrorCode TOKEN_EXPIRED = new ErrorCode(-805, "Token expired", "توکن منقضی شده است");
    public static final ErrorCode INSUFFICIENT_PERMISSIONS = new ErrorCode(-806, "Insufficient permissions", "دسترسی کافی ندارید");
    public static final ErrorCode API_KEY_INVALID = new ErrorCode(-807, "Invalid API key", "کلید API نامعتبر");
    public static final ErrorCode API_KEY_EXPIRED = new ErrorCode(-808, "API key expired", "کلید API منقضی شده است");
    
    // ===== BATCH ERROR CODES =====
    public static final ErrorCode BATCH_NOT_FOUND = new ErrorCode(-900, "Batch not found", "دسته‌بندی یافت نشد");
    public static final ErrorCode BATCH_ALREADY_EXISTS = new ErrorCode(-901, "Batch already exists", "دسته‌بندی قبلاً وجود دارد");
    public static final ErrorCode BATCH_INACTIVE = new ErrorCode(-902, "Batch inactive", "دسته‌بندی غیرفعال");
    public static final ErrorCode BATCH_PROCESSING_FAILED = new ErrorCode(-903, "Batch processing failed", "پردازش دسته‌بندی ناموفق");
    public static final ErrorCode BATCH_ALREADY_PROCESSED = new ErrorCode(-904, "Batch already processed", "دسته‌بندی قبلاً پردازش شده است");
    public static final ErrorCode BATCH_CANNOT_BE_CANCELLED = new ErrorCode(-905, "Batch cannot be cancelled", "دسته‌بندی قابل لغو نیست");
    public static final ErrorCode BATCH_REPORT_GENERATION_FAILED = new ErrorCode(-906, "Batch report generation failed", "تولید گزارش دسته‌بندی ناموفق");
    
    // ===== ITEM CATEGORY ERROR CODES =====
    public static final ErrorCode ITEM_CATEGORY_NOT_FOUND = new ErrorCode(-1000, "Item category not found", "دسته‌بندی کالا یافت نشد");
    public static final ErrorCode ITEM_CATEGORY_ALREADY_EXISTS = new ErrorCode(-1001, "Item category already exists", "دسته‌بندی کالا قبلاً وجود دارد");
    public static final ErrorCode ITEM_CATEGORY_INACTIVE = new ErrorCode(-1002, "Item category inactive", "دسته‌بندی کالا غیرفعال");
    public static final ErrorCode ITEM_CATEGORY_CANNOT_BE_DELETED = new ErrorCode(-1003, "Item category cannot be deleted", "دسته‌بندی کالا قابل حذف نیست");
    
    // ===== USER ROLE ERROR CODES =====
    public static final ErrorCode USER_ROLE_NOT_FOUND = new ErrorCode(-1100, "User role not found", "نقش کاربر یافت نشد");
    public static final ErrorCode USER_ROLE_ALREADY_EXISTS = new ErrorCode(-1101, "User role already exists", "نقش کاربر قبلاً وجود دارد");
    public static final ErrorCode USER_ROLE_CANNOT_BE_DELETED = new ErrorCode(-1102, "User role cannot be deleted", "نقش کاربر قابل حذف نیست");
    
    // ===== SUCCESS MESSAGE CODES =====
    public static final ErrorCode CREATED_SUCCESSFULLY = new ErrorCode(100, "Created successfully", "با موفقیت ایجاد شد");
    public static final ErrorCode UPDATED_SUCCESSFULLY = new ErrorCode(101, "Updated successfully", "با موفقیت به‌روزرسانی شد");
    public static final ErrorCode DELETED_SUCCESSFULLY = new ErrorCode(102, "Deleted successfully", "با موفقیت حذف شد");
    public static final ErrorCode OPERATION_SUCCESSFUL = new ErrorCode(103, "Operation successful", "عملیات موفق");
    public static final ErrorCode LOGIN_SUCCESSFUL = new ErrorCode(104, "Login successful", "ورود موفق");
    public static final ErrorCode LOGOUT_SUCCESSFUL = new ErrorCode(105, "Logout successful", "خروج موفق");
    public static final ErrorCode REPORT_GENERATED_SUCCESSFULLY = new ErrorCode(106, "Report generated successfully", "گزارش با موفقیت تولید شد");
    public static final ErrorCode BATCH_CREATED_SUCCESSFULLY = new ErrorCode(107, "Batch created successfully", "دسته‌بندی با موفقیت ایجاد شد");
    public static final ErrorCode BATCH_PROCESSED_SUCCESSFULLY = new ErrorCode(108, "Batch processed successfully", "دسته‌بندی با موفقیت پردازش شد");
    
    /**
     * Get error code by code number
     * @param code The error code number
     * @return ErrorCode object or SYSTEM_ERROR if not found
     */
    public static ErrorCode getByCode(int code) {
        // This could be implemented with a Map for better performance
        // For now, we'll use a switch statement
        return switch (code) {
            case 0 -> SUCCESS;
            case -1 -> SYSTEM_ERROR;
            case -2 -> DATABASE_ERROR;
            case -3 -> NETWORK_ERROR;
            case -4 -> SERVICE_UNAVAILABLE;
            case -5 -> RATE_LIMIT_EXCEEDED;
            case -6 -> INVALID_REQUEST;
            case -7 -> UNAUTHORIZED;
            case -8 -> FORBIDDEN;
            case -9 -> NOT_FOUND;
            case -10 -> CONFLICT;
            case -100 -> VALIDATION_ERROR;
            case -101 -> REQUIRED_FIELD_MISSING;
            case -102 -> INVALID_FORMAT;
            case -103 -> INVALID_AMOUNT;
            case -104 -> INVALID_PHONE_NUMBER;
            case -105 -> INVALID_EMAIL;
            case -106 -> INVALID_DATE;
            case -107 -> INVALID_STATUS;
            case -108 -> INVALID_ROLE;
            case -200 -> CUSTOMER_NOT_FOUND;
            case -201 -> CUSTOMER_ALREADY_EXISTS;
            case -202 -> CUSTOMER_INACTIVE;
            case -203 -> CUSTOMER_PHONE_REQUIRED;
            case -300 -> STORE_NOT_FOUND;
            case -301 -> STORE_ALREADY_EXISTS;
            case -302 -> STORE_INACTIVE;
            case -303 -> STORE_NOT_ALLOWED;
            case -400 -> COMPANY_NOT_FOUND;
            case -401 -> COMPANY_ALREADY_EXISTS;
            case -402 -> COMPANY_INACTIVE;
            case -500 -> DISCOUNT_CODE_NOT_FOUND;
            case -501 -> DISCOUNT_CODE_ALREADY_EXISTS;
            case -502 -> DISCOUNT_CODE_ALREADY_USED;
            case -503 -> DISCOUNT_CODE_EXPIRED;
            case -504 -> DISCOUNT_CODE_INACTIVE;
            case -505 -> DISCOUNT_CODE_USAGE_LIMIT_REACHED;
            case -506 -> MINIMUM_BILL_AMOUNT_NOT_MET;
            case -507 -> DISCOUNT_CODE_INVALID;
            case -600 -> GIFT_CARD_NOT_FOUND;
            case -601 -> GIFT_CARD_ALREADY_EXISTS;
            case -602 -> GIFT_CARD_INSUFFICIENT_BALANCE;
            case -603 -> GIFT_CARD_EXPIRED;
            case -604 -> GIFT_CARD_INACTIVE;
            case -605 -> GIFT_CARD_INVALID;
            case -606 -> SERIAL_NUMBER_ALREADY_EXISTS;
            case -700 -> TRANSACTION_NOT_FOUND;
            case -701 -> TRANSACTION_ALREADY_CONFIRMED;
            case -702 -> TRANSACTION_ALREADY_REVERSED;
            case -703 -> TRANSACTION_ALREADY_REFUNDED;
            case -704 -> TRANSACTION_NOT_CONFIRMED;
            case -705 -> DUPLICATE_TRANSACTION_ID;
            case -706 -> TRANSACTION_INVALID;
            case -707 -> TRANSACTION_FAILED;
            case -800 -> INVALID_CREDENTIALS;
            case -801 -> USER_NOT_FOUND;
            case -802 -> USER_ALREADY_EXISTS;
            case -803 -> USER_INACTIVE;
            case -804 -> INVALID_TOKEN;
            case -805 -> TOKEN_EXPIRED;
            case -806 -> INSUFFICIENT_PERMISSIONS;
            case -807 -> API_KEY_INVALID;
            case -808 -> API_KEY_EXPIRED;
            case -900 -> BATCH_NOT_FOUND;
            case -901 -> BATCH_ALREADY_EXISTS;
            case -902 -> BATCH_INACTIVE;
            case -903 -> BATCH_PROCESSING_FAILED;
            case -904 -> BATCH_ALREADY_PROCESSED;
            case -905 -> BATCH_CANNOT_BE_CANCELLED;
            case -906 -> BATCH_REPORT_GENERATION_FAILED;
            case -1000 -> ITEM_CATEGORY_NOT_FOUND;
            case -1001 -> ITEM_CATEGORY_ALREADY_EXISTS;
            case -1002 -> ITEM_CATEGORY_INACTIVE;
            case -1003 -> ITEM_CATEGORY_CANNOT_BE_DELETED;
            case -1100 -> USER_ROLE_NOT_FOUND;
            case -1101 -> USER_ROLE_ALREADY_EXISTS;
            case -1102 -> USER_ROLE_CANNOT_BE_DELETED;
            case 100 -> CREATED_SUCCESSFULLY;
            case 101 -> UPDATED_SUCCESSFULLY;
            case 102 -> DELETED_SUCCESSFULLY;
            case 103 -> OPERATION_SUCCESSFUL;
            case 104 -> LOGIN_SUCCESSFUL;
            case 105 -> LOGOUT_SUCCESSFUL;
            case 106 -> REPORT_GENERATED_SUCCESSFULLY;
            case 107 -> BATCH_CREATED_SUCCESSFULLY;
            case 108 -> BATCH_PROCESSED_SUCCESSFULLY;
            default -> SYSTEM_ERROR;
        };
    }
}
