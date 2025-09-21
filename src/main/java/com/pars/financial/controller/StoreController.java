package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.dto.StoreTransactionSummary;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.service.StoreService;
import com.pars.financial.service.StoreUserService;
import com.pars.financial.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/store")
@Tag(name = "Store", description = "Store management and store user operations")
public class StoreController {

    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;
    private final StoreUserService storeUserService;
    private final UserService userService;

    public StoreController(StoreService storeService, StoreUserService storeUserService, UserService userService) {
        this.storeService = storeService;
        this.storeUserService = storeUserService;
        this.userService = userService;
    }

    /**
     * Get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String username = authentication.getName();
        try {
            return userService.getUserEntityByUsername(username);
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping
    public GenericResponse<List<StoreDto>> getStores() {
        GenericResponse<List<StoreDto>> genericResponseDto = new GenericResponse<>();
        var ls = storeService.getAllStores();
        if(ls == null || ls.isEmpty()){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Store not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @GetMapping("/{storeId}")
    public GenericResponse<StoreDto> getStore(@PathVariable Long storeId) {
        GenericResponse<StoreDto> genericResponseDto = new GenericResponse<>();
        var ls = storeService.getStore(storeId);
        if(ls == null){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Store not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    // ==================== STORE USER ENDPOINTS ====================

    /**
     * Get store transaction summary with time-based subtotals for store users
     */
    @GetMapping("/user/transaction-summary")
    @Operation(summary = "Get store transaction summary", 
               description = "Get transaction summary for the authenticated store user including today, last 7 days, and last 30 days subtotals")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StoreTransactionSummary.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user"),
        @ApiResponse(responseCode = "404", description = "Store not found for user")
    })
    public GenericResponse<StoreTransactionSummary> getStoreUserTransactionSummary() {
        logger.info("GET /api/v1/store/user/transaction-summary called");

        GenericResponse<StoreTransactionSummary> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                logger.warn("Non-store user {} attempted to access store user endpoint", currentUser.getUsername());
                return response;
            }

            StoreTransactionSummary summary = storeUserService.getStoreTransactionSummary(currentUser);
            if (summary == null) {
                response.status = -1;
                response.message = "Store not found for user";
                return response;
            }

            response.data = summary;
            response.message = "Transaction summary retrieved successfully";

        } catch (Exception e) {
            logger.error("Error getting transaction summary", e);
            response.status = -1;
            response.message = "Error retrieving transaction summary: " + e.getMessage();
        }

        return response;
    }

    /**
     * Get store information for the authenticated store user
     */
    @GetMapping("/user/info")
    @Operation(summary = "Get store information", 
               description = "Get store information for the authenticated store user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user"),
        @ApiResponse(responseCode = "404", description = "Store not found for user")
    })
    public GenericResponse<Store> getStoreUserInfo() {
        logger.info("GET /api/v1/store/user/info called");

        GenericResponse<Store> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return response;
            }

            Store store = storeUserService.getStoreForUser(currentUser);
            if (store == null) {
                response.status = -1;
                response.message = "Store not found for user";
                return response;
            }

            response.data = store;
            response.message = "Store information retrieved successfully";

        } catch (Exception e) {
            logger.error("Error getting store information", e);
            response.status = -1;
            response.message = "Error retrieving store information: " + e.getMessage();
        }

        return response;
    }

    /**
     * Get all gift card transactions for the store
     */
    @GetMapping("/user/gift-card-transactions")
    @Operation(summary = "Get store gift card transactions", 
               description = "Get all gift card transactions for the authenticated store user's store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card transactions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user")
    })
    public GenericResponse<List<GiftCardTransaction>> getStoreUserGiftCardTransactions() {
        logger.info("GET /api/v1/store/user/gift-card-transactions called");

        GenericResponse<List<GiftCardTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return response;
            }

            List<GiftCardTransaction> transactions = storeUserService.getGiftCardTransactionsForStore(currentUser);
            response.data = transactions;
            response.message = "Gift card transactions retrieved successfully";

        } catch (Exception e) {
            logger.error("Error getting gift card transactions", e);
            response.status = -1;
            response.message = "Error retrieving gift card transactions: " + e.getMessage();
        }

        return response;
    }

    /**
     * Get all discount code transactions for the store
     */
    @GetMapping("/user/discount-code-transactions")
    @Operation(summary = "Get store discount code transactions", 
               description = "Get all discount code transactions for the authenticated store user's store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code transactions retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user")
    })
    public GenericResponse<List<DiscountCodeTransaction>> getStoreUserDiscountCodeTransactions() {
        logger.info("GET /api/v1/store/user/discount-code-transactions called");

        GenericResponse<List<DiscountCodeTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return response;
            }

            List<DiscountCodeTransaction> transactions = storeUserService.getDiscountCodeTransactionsForStore(currentUser);
            response.data = transactions;
            response.message = "Discount code transactions retrieved successfully";

        } catch (Exception e) {
            logger.error("Error getting discount code transactions", e);
            response.status = -1;
            response.message = "Error retrieving discount code transactions: " + e.getMessage();
        }

        return response;
    }

    /**
     * Search gift card transactions by serial number for the store
     */
    @GetMapping("/user/gift-card-transactions/search")
    @Operation(summary = "Search gift card transactions by serial number", 
               description = "Search gift card transactions for the store by gift card serial number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gift card transactions found successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user")
    })
    public GenericResponse<List<GiftCardTransaction>> searchStoreUserGiftCardTransactions(
            @Parameter(description = "Gift card serial number to search for", required = true)
            @RequestParam String serialNumber) {
        
        logger.info("GET /api/v1/store/user/gift-card-transactions/search called with serial: {}", serialNumber);

        GenericResponse<List<GiftCardTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return response;
            }

            List<GiftCardTransaction> transactions = storeUserService.searchGiftCardTransactionsBySerial(currentUser, serialNumber);
            response.data = transactions;
            response.message = "Gift card transactions search completed successfully";

        } catch (Exception e) {
            logger.error("Error searching gift card transactions", e);
            response.status = -1;
            response.message = "Error searching gift card transactions: " + e.getMessage();
        }

        return response;
    }

    /**
     * Search discount code transactions by discount code for the store
     */
    @GetMapping("/user/discount-code-transactions/search")
    @Operation(summary = "Search discount code transactions by code", 
               description = "Search discount code transactions for the store by discount code")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discount code transactions found successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User is not a store user")
    })
    public GenericResponse<List<DiscountCodeTransaction>> searchStoreUserDiscountCodeTransactions(
            @Parameter(description = "Discount code to search for", required = true)
            @RequestParam String discountCode) {
        
        logger.info("GET /api/v1/store/user/discount-code-transactions/search called with code: {}", discountCode);

        GenericResponse<List<DiscountCodeTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return response;
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return response;
            }

            List<DiscountCodeTransaction> transactions = storeUserService.searchDiscountCodeTransactionsByCode(currentUser, discountCode);
            response.data = transactions;
            response.message = "Discount code transactions search completed successfully";

        } catch (Exception e) {
            logger.error("Error searching discount code transactions", e);
            response.status = -1;
            response.message = "Error searching discount code transactions: " + e.getMessage();
        }

        return response;
    }
}
