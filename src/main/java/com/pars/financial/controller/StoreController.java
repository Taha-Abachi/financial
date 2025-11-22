package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.dto.StoreCreateRequest;
import com.pars.financial.dto.StoreUpdateRequest;
import com.pars.financial.dto.StoreTransactionSummary;
import com.pars.financial.entity.DiscountCodeTransaction;
import com.pars.financial.entity.GiftCardTransaction;
import com.pars.financial.entity.Store;
import com.pars.financial.entity.User;
import com.pars.financial.service.StoreService;
import com.pars.financial.service.StoreUserService;
import com.pars.financial.service.SecurityContextService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

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
    private final SecurityContextService securityContextService;

    public StoreController(StoreService storeService, StoreUserService storeUserService, SecurityContextService securityContextService) {
        this.storeService = storeService;
        this.storeUserService = storeUserService;
        this.securityContextService = securityContextService;
    }


    @GetMapping
    public ResponseEntity<GenericResponse<PagedResponse<StoreDto>>> getStores(
            @RequestParam(value="page", defaultValue = "0", required = false) Integer page,
            @RequestParam(value="size", defaultValue = "10", required = false) Integer size,
            @RequestParam(value="companyId", required = false) Long companyId) {
        logger.info("GET /api/v1/store called with pagination - page: {}, size: {}, companyId: {}", page, size, companyId);
        GenericResponse<PagedResponse<StoreDto>> genericResponseDto = new GenericResponse<>();
        int pageValue = (page != null) ? page : 0;
        int sizeValue = (size != null) ? size : 10;    
        try {
            PagedResponse<StoreDto> pagedStores = storeService.getStoresForCurrentUser(pageValue, sizeValue, companyId);
            logger.info("Found {} stores for current user (page {})", pagedStores.getContent().size(), pageValue);
            if (pagedStores.getContent() == null || pagedStores.getContent().isEmpty()) {
                genericResponseDto.status = -1;
                if (companyId != null) {
                    genericResponseDto.message = "No stores found for company ID: " + companyId;
                } else {
                    genericResponseDto.message = "No stores found for your access level";
                }
                return ResponseEntity.ok(genericResponseDto);
            } else {
                genericResponseDto.message = "Stores retrieved successfully";
                genericResponseDto.data = pagedStores;
                return ResponseEntity.ok(genericResponseDto);
            }
        } catch (Exception e) {
            logger.error("Error fetching stores with pagination: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = "Error fetching stores: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(genericResponseDto);
        }
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<GenericResponse<StoreDto>> getStore(@PathVariable Long storeId) {
        GenericResponse<StoreDto> genericResponseDto = new GenericResponse<>();
        try {
            StoreDto store = storeService.getStoreForCurrentUser(storeId);
            if (store == null) {
                genericResponseDto.status = -1;
                genericResponseDto.message = "Store not found or access denied";
                return ResponseEntity.notFound().build();
            }

            genericResponseDto.data = store;
            genericResponseDto.message = "Store retrieved successfully";
            return ResponseEntity.ok(genericResponseDto);
        } catch (Exception e) {
            logger.error("Error fetching store with ID {}: {}", storeId, e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = "Error fetching store: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(genericResponseDto);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new store", 
               description = "Create a new store for a company. Requires appropriate permissions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<GenericResponse<StoreDto>> createStore(@RequestBody StoreCreateRequest request) {
        logger.info("POST /api/v1/store called - creating store: {}", request.getStoreName());
        GenericResponse<StoreDto> genericResponseDto = new GenericResponse<>();
        
        try {
            StoreDto createdStore = storeService.createStore(request);
            genericResponseDto.data = createdStore;
            genericResponseDto.message = "Store created successfully";
            return ResponseEntity.ok(genericResponseDto);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = e.getMessage();
            return ResponseEntity.badRequest().body(genericResponseDto);
        } catch (Exception e) {
            logger.error("Error creating store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = "Error creating store: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(genericResponseDto);
        }
    }

    @PutMapping("/{storeId}")
    @Operation(summary = "Update a store", 
               description = "Update an existing store. Requires appropriate permissions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Store or company not found")
    })
    public ResponseEntity<GenericResponse<StoreDto>> updateStore(@PathVariable Long storeId, @RequestBody StoreUpdateRequest request) {
        logger.info("PUT /api/v1/store/{} called - updating store", storeId);
        GenericResponse<StoreDto> genericResponseDto = new GenericResponse<>();
        
        try {
            StoreDto updatedStore = storeService.updateStore(storeId, request);
            genericResponseDto.data = updatedStore;
            genericResponseDto.message = "Store updated successfully";
            return ResponseEntity.ok(genericResponseDto);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = e.getMessage();
            return ResponseEntity.badRequest().body(genericResponseDto);
        } catch (Exception e) {
            logger.error("Error updating store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = "Error updating store: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(genericResponseDto);
        }
    }

    @DeleteMapping("/{storeId}")
    @Operation(summary = "Delete a store", 
               description = "Logically delete a store (set isActive to false). Requires appropriate permissions.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Store deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Store not found")
    })
    public ResponseEntity<GenericResponse<Void>> deleteStore(@PathVariable Long storeId) {
        logger.info("DELETE /api/v1/store/{} called - deleting store", storeId);
        GenericResponse<Void> genericResponseDto = new GenericResponse<>();
        
        try {
            storeService.deleteStore(storeId);
            genericResponseDto.message = "Store deleted successfully";
            return ResponseEntity.ok(genericResponseDto);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error deleting store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = e.getMessage();
            return ResponseEntity.badRequest().body(genericResponseDto);
        } catch (Exception e) {
            logger.error("Error deleting store: {}", e.getMessage());
            genericResponseDto.status = -1;
            genericResponseDto.message = "Error deleting store: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(genericResponseDto);
        }
    }

    // ==================== STORE USER ENDPOINTS ====================

    /**
     * Get transaction summary with RBAC support
     * - SUPERADMIN/ADMIN: All transactions across all stores
     * - COMPANY_USER: All transactions for all stores in their company
     * - STORE_USER: Transactions for their assigned store
     */
    @GetMapping("/transaction-summary")
    @Operation(summary = "Get transaction summary", 
               description = "Get transaction summary based on user role. SUPERADMIN sees all transactions, COMPANY_USER sees company transactions, STORE_USER sees store transactions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StoreTransactionSummary.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User role not authorized"),
        @ApiResponse(responseCode = "404", description = "Store/Company not found for user")
    })
    public ResponseEntity<GenericResponse<StoreTransactionSummary>> getTransactionSummary() {
        logger.info("GET /api/v1/store/transaction-summary called");

        GenericResponse<StoreTransactionSummary> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Authorization is handled by Spring Security in SecurityConfig
            StoreTransactionSummary summary = storeUserService.getTransactionSummary(currentUser);
            if (summary == null) {
                response.status = -1;
                response.message = "Transaction summary not available. Please ensure your user is properly configured with a store or company.";
                return ResponseEntity.notFound().build();
            }

            response.data = summary;
            response.message = "Transaction summary retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting transaction summary", e);
            response.status = -1;
            response.message = "Error retrieving transaction summary: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<GenericResponse<Store>> getStoreUserInfo() {
        logger.info("GET /api/v1/store/user/info called");

        GenericResponse<Store> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Store store = storeUserService.getStoreForUser(currentUser);
            if (store == null) {
                response.status = -1;
                response.message = "Store not found for user";
                return ResponseEntity.notFound().build();
            }

            response.data = store;
            response.message = "Store information retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting store information", e);
            response.status = -1;
            response.message = "Error retrieving store information: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<GenericResponse<List<GiftCardTransaction>>> getStoreUserGiftCardTransactions() {
        logger.info("GET /api/v1/store/user/gift-card-transactions called");

        GenericResponse<List<GiftCardTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<GiftCardTransaction> transactions = storeUserService.getGiftCardTransactionsForStore(currentUser);
            response.data = transactions;
            response.message = "Gift card transactions retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting gift card transactions", e);
            response.status = -1;
            response.message = "Error retrieving gift card transactions: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<GenericResponse<List<DiscountCodeTransaction>>> getStoreUserDiscountCodeTransactions() {
        logger.info("GET /api/v1/store/user/discount-code-transactions called");

        GenericResponse<List<DiscountCodeTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<DiscountCodeTransaction> transactions = storeUserService.getDiscountCodeTransactionsForStore(currentUser);
            response.data = transactions;
            response.message = "Discount code transactions retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting discount code transactions", e);
            response.status = -1;
            response.message = "Error retrieving discount code transactions: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<GenericResponse<List<GiftCardTransaction>>> searchStoreUserGiftCardTransactions(
            @Parameter(description = "Gift card serial number to search for", required = true)
            @RequestParam String serialNumber) {
        
        logger.info("GET /api/v1/store/user/gift-card-transactions/search called with serial: {}", serialNumber);

        GenericResponse<List<GiftCardTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<GiftCardTransaction> transactions = storeUserService.searchGiftCardTransactionsBySerial(currentUser, serialNumber);
            response.data = transactions;
            response.message = "Gift card transactions search completed successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching gift card transactions", e);
            response.status = -1;
            response.message = "Error searching gift card transactions: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
    public ResponseEntity<GenericResponse<List<DiscountCodeTransaction>>> searchStoreUserDiscountCodeTransactions(
            @Parameter(description = "Discount code to search for", required = true)
            @RequestParam String discountCode) {
        
        logger.info("GET /api/v1/store/user/discount-code-transactions/search called with code: {}", discountCode);

        GenericResponse<List<DiscountCodeTransaction>> response = new GenericResponse<>();

        try {
            User currentUser = securityContextService.getCurrentUser();
            if (currentUser == null) {
                response.status = -1;
                response.message = "User not authenticated";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (!storeUserService.isStoreUser(currentUser)) {
                response.status = -1;
                response.message = "Access denied. User must be a store user with an associated store.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<DiscountCodeTransaction> transactions = storeUserService.searchDiscountCodeTransactionsByCode(currentUser, discountCode);
            response.data = transactions;
            response.message = "Discount code transactions search completed successfully";
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching discount code transactions", e);
            response.status = -1;
            response.message = "Error searching discount code transactions: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
