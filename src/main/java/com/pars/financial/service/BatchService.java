package com.pars.financial.service;

import com.pars.financial.dto.BatchCreateRequest;
import com.pars.financial.dto.BatchDto;
import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.entity.Batch;
import com.pars.financial.entity.Company;
import com.pars.financial.entity.User;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.repository.BatchRepository;
import com.pars.financial.repository.CompanyRepository;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.utils.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BatchService {
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);

    private final BatchRepository batchRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final DiscountCodeService discountCodeService;
    private final GiftCardService giftCardService;

    public BatchService(BatchRepository batchRepository, CompanyRepository companyRepository, UserRepository userRepository, DiscountCodeService discountCodeService, GiftCardService giftCardService) {
        this.batchRepository = batchRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.discountCodeService = discountCodeService;
        this.giftCardService = giftCardService;
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
            throw new ValidationException("Batch not found", null, -126);
        }
        return BatchDto.fromEntity(batch.get());
    }

    public BatchDto getBatchByBatchNumber(String batchNumber) {
        logger.debug("Fetching batch by batch number: {}", batchNumber);
        var batch = batchRepository.findByBatchNumber(batchNumber);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with batch number: {}", batchNumber);
            throw new ValidationException("Batch not found", null, -126);
        }
        return BatchDto.fromEntity(batch.get());
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
            throw new ValidationException("Company not found", null, -134);
        }

        // Validate user exists
        var user = userRepository.findById(request.getRequestUserId());
        if (user.isEmpty()) {
            logger.warn("User not found with id: {}", request.getRequestUserId());
            throw new ValidationException("User not found", null, -121);
        }

        // Generate unique batch number
        String batchNumber = generateBatchNumber();

        // Create batch
        var batch = new Batch(batchNumber, request.getBatchType(), request.getDescription(), 
                             request.getTotalCount(), user.get(), company.get());
        
        var savedBatch = batchRepository.save(batch);
        logger.info("Created batch with id: {} and batch number: {}", savedBatch.getId(), savedBatch.getBatchNumber());

        // Process batch asynchronously
        processBatchAsync(savedBatch, request);

        return BatchDto.fromEntity(savedBatch);
    }

    @Async
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
            batch.setUpdatedAt(LocalDateTime.now());
            batchRepository.save(batch);
        }
    }

    private void processDiscountCodeBatch(Batch batch, BatchCreateRequest request) {
        logger.info("Processing discount code batch: {}", batch.getBatchNumber());
        
        int processed = 0;
        int failed = 0;

        if (request.getDiscountCodeRequests() != null && !request.getDiscountCodeRequests().isEmpty()) {
            for (var discountCodeRequest : request.getDiscountCodeRequests()) {
                try {
                    discountCodeService.generate(discountCodeRequest);
                    processed++;
                } catch (Exception e) {
                    logger.error("Error processing discount code in batch {}: {}", batch.getBatchNumber(), e.getMessage());
                    failed++;
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
                    
                    discountCodeService.generate(defaultRequest);
                    processed++;
                } catch (Exception e) {
                    logger.error("Error processing discount code in batch {}: {}", batch.getBatchNumber(), e.getMessage());
                    failed++;
                }
            }
        }

        batch.setProcessedCount(processed);
        batch.setFailedCount(failed);
        batchRepository.save(batch);
    }

    private void processGiftCardBatch(Batch batch, BatchCreateRequest request) {
        logger.info("Processing gift card batch: {}", batch.getBatchNumber());
        
        int processed = 0;
        int failed = 0;

        if (request.getGiftCardRequests() != null && !request.getGiftCardRequests().isEmpty()) {
            for (var giftCardRequest : request.getGiftCardRequests()) {
                try {
                    giftCardService.generateGiftCards(giftCardRequest);
                    processed += giftCardRequest.getCount();
                } catch (Exception e) {
                    logger.error("Error processing gift card in batch {}: {}", batch.getBatchNumber(), e.getMessage());
                    failed += giftCardRequest.getCount();
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
                    defaultRequest.setRemainingValidityPeriod(30L);
                    defaultRequest.setCount(1);
                    
                    giftCardService.generateGiftCards(defaultRequest);
                    processed++;
                } catch (Exception e) {
                    logger.error("Error processing gift card in batch {}: {}", batch.getBatchNumber(), e.getMessage());
                    failed++;
                }
            }
        }

        batch.setProcessedCount(processed);
        batch.setFailedCount(failed);
        batchRepository.save(batch);
    }

    private String generateBatchNumber() {
        String batchNumber;
        do {
            batchNumber = "BATCH" + RandomStringGenerator.generateRandomUppercaseStringWithNumbers(8);
        } while (batchRepository.existsByBatchNumber(batchNumber));
        return batchNumber;
    }

    @Transactional
    public BatchDto updateBatchStatus(Long id, Batch.BatchStatus status) {
        logger.info("Updating batch {} status to: {}", id, status);
        
        var batch = batchRepository.findById(id);
        if (batch.isEmpty()) {
            logger.warn("Batch not found with id: {}", id);
            throw new ValidationException("Batch not found", null, -126);
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
            throw new ValidationException("Batch not found", null, -126);
        }

        var batchEntity = batch.get();
        if (batchEntity.getStatus() == Batch.BatchStatus.COMPLETED || 
            batchEntity.getStatus() == Batch.BatchStatus.FAILED) {
            logger.warn("Cannot cancel batch {} with status: {}", id, batchEntity.getStatus());
            throw new ValidationException("Cannot cancel batch with current status", null, -127);
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
} 