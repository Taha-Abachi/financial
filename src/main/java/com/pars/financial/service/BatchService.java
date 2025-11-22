package com.pars.financial.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.pars.financial.dto.BatchCreateRequest;
import com.pars.financial.dto.BatchDto;
import com.pars.financial.entity.Batch;
import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.BatchRepository;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.repository.GiftCardRepository;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.mapper.GiftCardMapper;
import com.pars.financial.mapper.DiscountCodeMapper;
import com.pars.financial.utils.RandomStringGenerator;
import com.pars.financial.entity.User;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.dto.BatchDetailDto;
import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.DiscountCodeDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class BatchService {
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);

    private final BatchRepository batchRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final DiscountCodeService discountCodeService;
    private final GiftCardService giftCardService;
    private final ObjectMapper objectMapper;
    private final SecurityContextService securityContextService;
    private final GiftCardRepository giftCardRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final GiftCardMapper giftCardMapper;
    private final DiscountCodeMapper discountCodeMapper;

    public BatchService(BatchRepository batchRepository, CompanyRepository companyRepository, UserRepository userRepository, DiscountCodeService discountCodeService, GiftCardService giftCardService, ObjectMapper objectMapper, SecurityContextService securityContextService, GiftCardRepository giftCardRepository, DiscountCodeRepository discountCodeRepository, GiftCardMapper giftCardMapper, DiscountCodeMapper discountCodeMapper) {
        this.batchRepository = batchRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.discountCodeService = discountCodeService;
        this.giftCardService = giftCardService;
        this.objectMapper = objectMapper;
        this.securityContextService = securityContextService;
        this.giftCardRepository = giftCardRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.giftCardMapper = giftCardMapper;
        this.discountCodeMapper = discountCodeMapper;
    }

    public List<BatchDto> getAllBatches() {
        logger.debug("Fetching all batches");
        return batchRepository.findAll().stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
    }

    public BatchDto getBatchById(Long id) {
        logger.debug("Fetching batch by id: {}", id);
        var batch = batchRepository.findById(id);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with id: {}", id);
            throw new ValidationException(ErrorCodes.BATCH_NOT_FOUND);
        }
        return BatchDto.fromEntity(batch.get());
    }

    public BatchDto getBatchByBatchNumber(String batchNumber) {
        logger.debug("Fetching batch by batch number: {}", batchNumber);
        var batch = batchRepository.findByBatchNumber(batchNumber);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with batch number: {}", batchNumber);
            throw new ValidationException(ErrorCodes.BATCH_NOT_FOUND);
        }
        return BatchDto.fromEntity(batch.get());
    }

    @Transactional(readOnly = true)
    public BatchDetailDto getBatchDetailsByBatchNumber(String batchNumber) {
        logger.info("Fetching batch details by batch number: {}", batchNumber);
        
        // Get the batch
        var batchOptional = batchRepository.findByBatchNumber(batchNumber);
        if (batchOptional.isEmpty()) {
            logger.warn("Batch not found with batch number: {}", batchNumber);
            throw new ValidationException(ErrorCodes.BATCH_NOT_FOUND);
        }
        
        Batch batch = batchOptional.get();
        logger.debug("Found batch with id: {}, type: {}", batch.getId(), batch.getBatchType());
        
        BatchDto batchDto = BatchDto.fromEntity(batch);
        
        List<GiftCardDto> giftCards = null;
        List<DiscountCodeDto> discountCodes = null;
        
        // Fetch gift cards or discount codes based on batch type
        if (batch.getBatchType() == Batch.BatchType.GIFT_CARD) {
            logger.debug("Fetching gift cards for batch id: {}", batch.getId());
            var giftCardEntities = giftCardRepository.findByBatchId(batch.getId());
            giftCards = giftCardMapper.getFrom(giftCardEntities);
            logger.debug("Found {} gift cards for batch {}", giftCards != null ? giftCards.size() : 0, batchNumber);
        } else if (batch.getBatchType() == Batch.BatchType.DISCOUNT_CODE) {
            logger.debug("Fetching discount codes for batch id: {}", batch.getId());
            var discountCodeEntities = discountCodeRepository.findByBatchId(batch.getId());
            discountCodes = discountCodeMapper.getFrom(discountCodeEntities);
            logger.debug("Found {} discount codes for batch {}", discountCodes != null ? discountCodes.size() : 0, batchNumber);
        }
        
        return new BatchDetailDto(batchDto, giftCards, discountCodes);
    }

    @Transactional
    public BatchDto createBatch(BatchCreateRequest request) {
        logger.info("Creating new batch: {}", request.getDescription());

        // Check if batch is empty
        if (request.getTotalCount() <= 0) {
            logger.warn("Batch total count is 0 or negative, doing nothing");
            return null;
        }

        // Validate company exists
        var company = companyRepository.findById(request.getCompanyId());
        if (company.isEmpty()) {
            logger.warn("Company not found with id: {}", request.getCompanyId());
            throw new ValidationException(ErrorCodes.COMPANY_NOT_FOUND);
        }

        // Validate user exists
        var user = userRepository.findById(request.getRequestUserId());
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", request.getRequestUserId());
            throw new ValidationException(ErrorCodes.USER_NOT_FOUND);
        }

        // Generate unique batch number
        String batchNumber = generateBatchNumber();

        // Create batch
        var batch = new Batch(batchNumber, request.getBatchType(), request.getDescription(), 
                             request.getTotalCount(), user.get(), company.get());
        
        // Set request details for persistence
        setRequestDetails(batch, request);
        
        var savedBatch = batchRepository.save(batch);
        logger.info("Created batch with id: {} and batch number: {}", savedBatch.getId(), savedBatch.getBatchNumber());

        // Process batch asynchronously
        processBatchAsync(savedBatch, request);

        return BatchDto.fromEntity(savedBatch);
    }

    @Async
    @Transactional
    protected void processBatchAsync(Batch batch, BatchCreateRequest request) {
        logger.info("Starting async processing for batch: {}", batch.getBatchNumber());
        
        try {
            batch.setStatus(Batch.BatchStatus.PROCESSING);
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);

            if (batch.getBatchType() == Batch.BatchType.DISCOUNT_CODE) {
                processDiscountCodeBatch(batch, request);
            } else if (batch.getBatchType() == Batch.BatchType.GIFT_CARD) {
                processGiftCardBatch(batch, request);
            }

            batch.setStatus(Batch.BatchStatus.COMPLETED);
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);
            
            logger.info("Completed processing for batch: {}", batch.getBatchNumber());
        } catch (Exception e) {
            logger.error("Error processing batch {}: {}", batch.getBatchNumber(), e.getMessage());
            batch.setStatus(Batch.BatchStatus.FAILED);
            batch.setErrorMessage("Batch processing failed: " + e.getMessage());
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);
            // Re-throw the exception to trigger transaction rollback
            throw e;
        }
    }

    @Transactional
    private void processDiscountCodeBatch(Batch batch, BatchCreateRequest request) {
        logger.info("Processing discount code batch: {}", batch.getBatchNumber());
        
        int processed = 0;

        if (request.getDiscountCodeRequests() != null && !request.getDiscountCodeRequests().isEmpty()) {
            for (int i = 0; i < request.getDiscountCodeRequests().size(); i++) {
                var discountCodeRequest = request.getDiscountCodeRequests().get(i);
                try {
                    discountCodeService.generateList(discountCodeRequest, batch);
                    processed += discountCodeRequest.count;
                } catch (Exception e) {
                    logger.error("Error processing discount code {} in batch {}: {}", i + 1, batch.getBatchNumber(), e.getMessage());
                    // Throw exception to trigger rollback of entire batch
                    throw new RuntimeException("Failed to process discount code item " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        } else {
            // Generate single type discount codes
            var company = batch.getCompany();
            for (int i = 0; i < batch.getTotalCount(); i++) {
                try {
                    // Create a default discount code request
                    var defaultRequest = new com.pars.financial.dto.DiscountCodeIssueRequest();
                    defaultRequest.companyId = company.getId();
                    defaultRequest.remainingValidityPeriod = 30L;
                    defaultRequest.count = 1;
                    
                    discountCodeService.generateList(defaultRequest, batch);
                    processed++;
                } catch (Exception e) {
                    logger.error("Error processing discount code {} in batch {}: {}", i + 1, batch.getBatchNumber(), e.getMessage());
                    // Throw exception to trigger rollback of entire batch
                    throw new RuntimeException("Failed to process discount code item " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        }

        batch.setProcessedCount(processed);
        batch.setFailedCount(0); // No failures since any failure would have triggered rollback
        batchRepository.save(batch);
    }

    @Transactional
    private void processGiftCardBatch(Batch batch, BatchCreateRequest request) {
        logger.info("Processing gift card batch: {}", batch.getBatchNumber());
        
        int processed = 0;

        if (request.getGiftCardRequests() != null && !request.getGiftCardRequests().isEmpty()) {
            for (int i = 0; i < request.getGiftCardRequests().size(); i++) {
                var giftCardRequest = request.getGiftCardRequests().get(i);
                
                // Validate that realAmount is provided for gift card requests
                if (giftCardRequest.getRealAmount() <= 0) {
                    logger.error("Gift card request {} in batch {} is missing realAmount or has invalid value", 
                                i + 1, batch.getBatchNumber());
                    throw new ValidationException(ErrorCodes.INVALID_REQUEST, 
                        "Real amount is required for gift card requests. Gift card request at index " + (i + 1) + " is missing realAmount.");
                }
                
                try {
                    giftCardService.generateGiftCards(giftCardRequest, batch);
                    processed += giftCardRequest.getCount();
                } catch (Exception e) {
                    logger.error("Error processing gift card {} in batch {}: {}", i + 1, batch.getBatchNumber(), e.getMessage());
                    // Throw exception to trigger rollback of entire batch
                    throw new RuntimeException("Failed to process gift card item " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        } else {
            // Generate single type gift cards
            var company = batch.getCompany();
            for (int i = 0; i < batch.getTotalCount(); i++) {
                try {
                    // Create a default gift card request
                    var defaultRequest = new com.pars.financial.dto.GiftCardIssueRequest();
                    defaultRequest.setCompanyId(company.getId());
                    defaultRequest.setBalance(1000L);
                    defaultRequest.setRealAmount(1000L); // Set realAmount to match balance for default requests
                    defaultRequest.setRemainingValidityPeriod(30L);
                    defaultRequest.setCount(1);
                    
                    giftCardService.generateGiftCards(defaultRequest, batch);
                    processed++;
                } catch (Exception e) {
                    logger.error("Error processing gift card {} in batch {}: {}", i + 1, batch.getBatchNumber(), e.getMessage());
                    // Throw exception to trigger rollback of entire batch
                    throw new RuntimeException("Failed to process gift card item " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
        }

        batch.setProcessedCount(processed);
        batch.setFailedCount(0); // No failures since any failure would have triggered rollback
        batchRepository.save(batch);
    }

    private String generateBatchNumber() {
        String batchNumber;
        do {
            batchNumber = "BATCH" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(8);
        } while (batchRepository.existsByBatchNumber(batchNumber));
        return batchNumber;
    }

    private void setRequestDetails(Batch batch, BatchCreateRequest request) {
        try {
            if (request.getGiftCardRequests() != null && !request.getGiftCardRequests().isEmpty()) {
                batch.setGiftCardRequestsJson(objectMapper.writeValueAsString(request.getGiftCardRequests()));
            }
            if (request.getDiscountCodeRequests() != null && !request.getDiscountCodeRequests().isEmpty()) {
                batch.setDiscountCodeRequestsJson(objectMapper.writeValueAsString(request.getDiscountCodeRequests()));
            }
        } catch (JsonProcessingException e) {
            logger.error("Error serializing request details for batch {}: {}", batch.getBatchNumber(), e.getMessage());
            // Don't throw exception here as it's not critical for batch processing
        }
    }

    @Transactional
    public BatchDto updateBatchStatus(Long id, Batch.BatchStatus status) {
        logger.info("Updating batch {} status to: {}", id, status);
        
        var batch = batchRepository.findById(id);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with id: {}", id);
            throw new ValidationException(ErrorCodes.BATCH_NOT_FOUND);
        }

        var batchEntity = batch.get();
        batchEntity.setStatus(status);
        batchEntity.setUpdatedAt(LocalDateTime.now());
        
        var savedBatch = batchRepository.save(batchEntity);
        logger.info("Updated batch {} status to: {}", id, status);
        
        return BatchDto.fromEntity(savedBatch);
    }

    @Transactional
    public void cancelBatch(Long id) {
        logger.info("Cancelling batch: {}", id);
        
        var batch = batchRepository.findById(id);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with id: {}", id);
            throw new ValidationException(ErrorCodes.BATCH_NOT_FOUND);
        }

        var batchEntity = batch.get();
        if (batchEntity.getStatus() == Batch.BatchStatus.COMPLETED || 
            batchEntity.getStatus() == Batch.BatchStatus.FAILED) {
            logger.warn("Cannot cancel batch {} with status: {}", id, batchEntity.getStatus());
            throw new ValidationException(ErrorCodes.BATCH_CANNOT_BE_CANCELLED);
        }

        batchEntity.setStatus(Batch.BatchStatus.CANCELLED);
        batchEntity.setUpdatedAt(LocalDateTime.now());
        batchRepository.save(batchEntity);
        
        logger.info("Cancelled batch: {}", id);
    }

    public List<BatchDto> getBatchesByType(Batch.BatchType batchType) {
        logger.debug("Fetching batches by type: {}", batchType);
        return batchRepository.findByBatchType(batchType).stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BatchDto> getBatchesByStatus(Batch.BatchStatus status) {
        logger.debug("Fetching batches by status: {}", status);
        return batchRepository.findByStatus(status).stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BatchDto> getBatchesByCompany(Long companyId) {
        logger.debug("Fetching batches by company: {}", companyId);
        return batchRepository.findByCompanyId(companyId).stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BatchDto> getBatchesByUser(Long userId) {
        logger.debug("Fetching batches by user: {}", userId);
        return batchRepository.findByRequestUserId(userId).stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get batches based on current user's role and permissions
     * - SUPERADMIN/ADMIN: All batches
     * - COMPANY_USER: Batches of their company
     * - STORE_USER: Batches of their store's company
     * - API_USER: All batches (if system-wide access needed)
     */
    @Transactional(readOnly = true)
    public PagedResponse<BatchDto> getBatchesForCurrentUser(int page, int size) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }
        
        return getBatchesForUser(currentUser, page, size);
    }

    /**
     * Get batches with optional company filter based on current user's permissions
     */
    @Transactional(readOnly = true)
    public PagedResponse<BatchDto> getBatchesForCurrentUser(int page, int size, Long companyId) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }

        // If companyId is provided, validate user has permission to filter by it
        if (companyId != null) {
            if (!canUserFilterByCompany(currentUser, companyId)) {
                logger.warn("User {} attempted to filter by company {} without permission", 
                           currentUser.getUsername(), companyId);
                return new PagedResponse<>(List.of(), 0, size, 0, 0);
            }
            return getBatchesByCompany(companyId, page, size);
        } else {
            return getBatchesForUser(currentUser, page, size);
        }
    }

    /**
     * Get a specific batch with access control
     */
    @Transactional(readOnly = true)
    public BatchDto getBatchForCurrentUser(Long batchId) {
        User currentUser = securityContextService.getCurrentUser();
        if (currentUser == null) {
            logger.warn("No authenticated user found");
            return null;
        }

        BatchDto batch = getBatchById(batchId);
        if (batch == null) {
            return null;
        }

        // Check if user has access to this specific batch
        if (!hasAccessToBatch(currentUser, batch)) {
            logger.warn("User {} attempted to access batch {} without permission", 
                       currentUser.getUsername(), batchId);
            return null;
        }

        return batch;
    }

    /**
     * Get batches for a specific user with role-based filtering
     */
    @Transactional(readOnly = true)
    public PagedResponse<BatchDto> getBatchesForUser(User user, int page, int size) {
        // Validate user and role
        if (user == null) {
            logger.warn("User is null - returning empty result");
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }
        
        if (user.getRole() == null) {
            logger.warn("User {} has no role assigned - returning empty result", user.getUsername());
            return new PagedResponse<>(List.of(), 0, size, 0, 0);
        }
        
        logger.debug("Fetching batches for user: {} with role: {} - page: {}, size: {}", 
                    user.getUsername(), user.getRole().getName(), page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10; // Default page size
        }
        if (size > 100) {
            size = 100; // Maximum page size
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Batch> batchPage;
        
        // Role-based data filtering
        switch (user.getRole().getName()) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                // Return all batches
                logger.debug("User {} has admin access - returning all batches", user.getUsername());
                batchPage = batchRepository.findAll(pageable);
                break;
                
            case "COMPANY_USER":
                // Return batches of user's company
                if (user.getCompany() == null) {
                    logger.warn("COMPANY_USER {} has no company assigned", user.getUsername());
                    batchPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has company access - returning batches for company: {}", 
                               user.getUsername(), user.getCompany().getId());
                    batchPage = batchRepository.findByCompanyId(user.getCompany().getId(), pageable);
                }
                break;
                
            case "STORE_USER":
                // Return batches of user's store's company
                if (user.getStore() == null || user.getStore().getCompany() == null) {
                    logger.warn("STORE_USER {} has no store or company assigned", user.getUsername());
                    batchPage = Page.empty(pageable);
                } else {
                    logger.debug("User {} has store access - returning batches for company: {}", 
                               user.getUsername(), user.getStore().getCompany().getId());
                    batchPage = batchRepository.findByCompanyId(user.getStore().getCompany().getId(), pageable);
                }
                break;
                
            default:
                logger.warn("Unknown role {} for user {} - returning empty result", 
                           user.getRole().getName(), user.getUsername());
                batchPage = Page.empty(pageable);
                break;
        }
        
        List<BatchDto> batches = batchPage.getContent().stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            batches,
            batchPage.getNumber(),
            batchPage.getSize(),
            batchPage.getTotalElements(),
            batchPage.getTotalPages()
        );
    }

    /**
     * Get batches by company with pagination
     */
    @Transactional(readOnly = true)
    public PagedResponse<BatchDto> getBatchesByCompany(Long companyId, int page, int size) {
        logger.debug("Fetching batches by company: {} - page: {}, size: {}", companyId, page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 10; // Default page size
        }
        if (size > 100) {
            size = 100; // Maximum page size
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Batch> batchPage = batchRepository.findByCompanyId(companyId, pageable);
        
        List<BatchDto> batches = batchPage.getContent().stream()
                .map(BatchDto::fromEntity)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
            batches,
            batchPage.getNumber(),
            batchPage.getSize(),
            batchPage.getTotalElements(),
            batchPage.getTotalPages()
        );
    }

    /**
     * Check if user can filter by a specific company
     */
    private boolean canUserFilterByCompany(User user, Long companyId) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;
                
            case "COMPANY_USER":
                return user.getCompany() != null && user.getCompany().getId().equals(companyId);
                
            case "STORE_USER":
                return user.getStore() != null && 
                       user.getStore().getCompany() != null && 
                       user.getStore().getCompany().getId().equals(companyId);
                       
            default:
                return false;
        }
    }

    /**
     * Check if user has access to a specific batch
     */
    private boolean hasAccessToBatch(User user, BatchDto batch) {
        if (user == null || user.getRole() == null || batch == null) {
            return false;
        }
        String roleName = user.getRole().getName();
        
        switch (roleName) {
            case "SUPERADMIN":
            case "ADMIN":
            case "API_USER":
                return true;
                
            case "COMPANY_USER":
                return user.getCompany() != null && 
                       batch.getCompany() != null && 
                       user.getCompany().getId().equals(batch.getCompany().getId());
                       
            case "STORE_USER":
                return user.getStore() != null && 
                       user.getStore().getCompany() != null &&
                       batch.getCompany() != null &&
                       user.getStore().getCompany().getId().equals(batch.getCompany().getId());
                       
            default:
                return false;
        }
    }
} 