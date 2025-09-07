package com.pars.financial.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pars.financial.dto.DiscountCodeTransactionDto;
import com.pars.financial.dto.DiscountCodeValidationResponse;
import com.pars.financial.entity.DiscountCode;
import com.pars.financial.entity.Store;
import com.pars.financial.enums.DiscountType;
import com.pars.financial.enums.TransactionType;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.CustomerRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.repository.ItemCategoryRepository;
import com.pars.financial.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class DiscountCodeServiceTest {

    @Mock
    private DiscountCodeRepository codeRepository;
    
    @Mock
    private CompanyRepository companyRepository;
    
    @Mock
    private StoreRepository storeRepository;
    
    @Mock
    private ItemCategoryRepository itemCategoryRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private DiscountCodeService discountCodeService;
    
    private DiscountCode validDiscountCode;
    private Store validStore;
    private DiscountCodeTransactionDto validRequest;
    
    @BeforeEach
    void setUp() {
        // Setup valid discount code
        validDiscountCode = new DiscountCode();
        validDiscountCode.setCode("TEST123");
        validDiscountCode.setActive(true);
        validDiscountCode.setUsed(false);
        validDiscountCode.setExpiryDate(LocalDate.now().plusDays(30));
        validDiscountCode.setUsageLimit(10);
        validDiscountCode.setCurrentUsageCount(5);
        validDiscountCode.setMinimumBillAmount(1000);
        validDiscountCode.setPercentage(20);
        validDiscountCode.setMaxDiscountAmount(5000);
        validDiscountCode.setDiscountType(DiscountType.PERCENTAGE);
        validDiscountCode.setStoreLimited(false);
        validDiscountCode.setItemCategoryLimited(false);
        
        // Setup valid store
        validStore = new Store();
        validStore.setId(1L);
        validStore.setStore_name("Test Store");
        
        // Setup valid request
        validRequest = new DiscountCodeTransactionDto();
        validRequest.code = "TEST123";
        validRequest.originalAmount = 5000;
        validRequest.storeId = 1L;
        validRequest.phoneNo = "09123456789";
        validRequest.clientTransactionId = "TXN001";
        validRequest.trxType = TransactionType.Redeem;
    }
    
    @Test
    void testValidateDiscountCodeRules_ValidCode() {
        // Given
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(validStore));
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertTrue(response.isValid);
        assertEquals("Discount code is valid", response.message);
        assertEquals(1000, response.calculatedDiscountAmount); // 20% of 5000
        assertEquals(DiscountType.PERCENTAGE, response.discountType);
        assertEquals(20, response.percentage);
        assertEquals(5000, response.maxDiscountAmount);
        assertEquals(1000, response.minimumBillAmount);
        assertEquals(10, response.usageLimit);
        assertEquals(5, response.currentUsageCount);
        assertFalse(response.storeLimited);
        assertFalse(response.itemCategoryLimited);
    }
    
    @Test
    void testValidateDiscountCodeRules_CodeNotFound() {
        // Given
        when(codeRepository.findByCode("INVALID")).thenReturn(null);
        validRequest.code = "INVALID";
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Discount Code not found.", response.message);
        assertEquals("DISCOUNT_CODE_NOT_FOUND", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_ExpiredCode() {
        // Given
        validDiscountCode.setExpiryDate(LocalDate.now().minusDays(1));
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Discount code is expired.", response.message);
        assertEquals("DISCOUNT_CODE_EXPIRED", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_InactiveCode() {
        // Given
        validDiscountCode.setActive(false);
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Discount code is inactive.", response.message);
        assertEquals("DISCOUNT_CODE_INACTIVE", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_AlreadyUsed() {
        // Given
        validDiscountCode.setUsed(true);
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Discount already used.", response.message);
        assertEquals("DISCOUNT_CODE_ALREADY_USED", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_UsageLimitReached() {
        // Given
        validDiscountCode.setCurrentUsageCount(10);
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Discount code usage limit reached.", response.message);
        assertEquals("DISCOUNT_CODE_USAGE_LIMIT_REACHED", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_MinimumBillAmountNotMet() {
        // Given
        validRequest.originalAmount = 500; // Less than minimum 1000
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        // Store lookup is not needed since validation fails before reaching that point
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Original amount is less than minimum bill amount required.", response.message);
        assertEquals("MINIMUM_BILL_AMOUNT_NOT_MET", response.errorCode);
    }
    
    @Test
    void testValidateDiscountCodeRules_StoreNotFound() {
        // Given
        when(codeRepository.findByCode("TEST123")).thenReturn(validDiscountCode);
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When
        DiscountCodeValidationResponse response = discountCodeService.validateDiscountCodeRules(validRequest);
        
        // Then
        assertNotNull(response);
        assertFalse(response.isValid);
        assertEquals("Store Not Found", response.message);
        assertEquals("STORE_NOT_FOUND", response.errorCode);
    }
}
