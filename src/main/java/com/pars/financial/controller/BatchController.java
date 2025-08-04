package com.pars.financial.controller;

import com.pars.financial.dto.BatchCreateRequest;
import com.pars.financial.dto.BatchDto;
import com.pars.financial.entity.Batch;
import com.pars.financial.service.BatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {
    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping("/list")
    public com.pars.financial.dto.GenericResponse<List<BatchDto>> getAllBatches() {
        logger.info("GET /api/v1/batches/list called");
        var response = new com.pars.financial.dto.GenericResponse<List<BatchDto>>();
        try {
            var batches = batchService.getAllBatches();
            response.data = batches;
        } catch (Exception e) {
            logger.error("Error fetching batches: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/{id}")
    public com.pars.financial.dto.GenericResponse<BatchDto> getBatchById(@PathVariable Long id) {
        logger.info("GET /api/v1/batches/{} called", id);
        var response = new com.pars.financial.dto.GenericResponse<BatchDto>();
        try {
            var batch = batchService.getBatchById(id);
            response.data = batch;
        } catch (Exception e) {
            logger.error("Error fetching batch with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/batch-number/{batchNumber}")
    public com.pars.financial.dto.GenericResponse<BatchDto> getBatchByBatchNumber(@PathVariable String batchNumber) {
        logger.info("GET /api/v1/batches/batch-number/{} called", batchNumber);
        var response = new com.pars.financial.dto.GenericResponse<BatchDto>();
        try {
            var batch = batchService.getBatchByBatchNumber(batchNumber);
            response.data = batch;
        } catch (Exception e) {
            logger.error("Error fetching batch with batch number {}: {}", batchNumber, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/create")
    public com.pars.financial.dto.GenericResponse<BatchDto> createBatch(@RequestBody BatchCreateRequest request) {
        logger.info("POST /api/v1/batches/create called with description: {}", request.getDescription());
        var response = new com.pars.financial.dto.GenericResponse<BatchDto>();
        try {
            var createdBatch = batchService.createBatch(request);
            response.data = createdBatch;
        } catch (Exception e) {
            logger.error("Error creating batch: {}", e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/{id}/status")
    public com.pars.financial.dto.GenericResponse<BatchDto> updateBatchStatus(@PathVariable Long id, @RequestBody Batch.BatchStatus status) {
        logger.info("POST /api/v1/batches/{}/status called with status: {}", id, status);
        var response = new com.pars.financial.dto.GenericResponse<BatchDto>();
        try {
            var updatedBatch = batchService.updateBatchStatus(id, status);
            response.data = updatedBatch;
        } catch (Exception e) {
            logger.error("Error updating batch status with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @PostMapping("/{id}/cancel")
    public com.pars.financial.dto.GenericResponse<Void> cancelBatch(@PathVariable Long id) {
        logger.info("POST /api/v1/batches/{}/cancel called", id);
        var response = new com.pars.financial.dto.GenericResponse<Void>();
        try {
            batchService.cancelBatch(id);
        } catch (Exception e) {
            logger.error("Error cancelling batch with id {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/type/{batchType}")
    public com.pars.financial.dto.GenericResponse<List<BatchDto>> getBatchesByType(@PathVariable Batch.BatchType batchType) {
        logger.info("GET /api/v1/batches/type/{} called", batchType);
        var response = new com.pars.financial.dto.GenericResponse<List<BatchDto>>();
        try {
            var batches = batchService.getBatchesByType(batchType);
            response.data = batches;
        } catch (Exception e) {
            logger.error("Error fetching batches by type {}: {}", batchType, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/status/{status}")
    public com.pars.financial.dto.GenericResponse<List<BatchDto>> getBatchesByStatus(@PathVariable Batch.BatchStatus status) {
        logger.info("GET /api/v1/batches/status/{} called", status);
        var response = new com.pars.financial.dto.GenericResponse<List<BatchDto>>();
        try {
            var batches = batchService.getBatchesByStatus(status);
            response.data = batches;
        } catch (Exception e) {
            logger.error("Error fetching batches by status {}: {}", status, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/company/{companyId}")
    public com.pars.financial.dto.GenericResponse<List<BatchDto>> getBatchesByCompany(@PathVariable Long companyId) {
        logger.info("GET /api/v1/batches/company/{} called", companyId);
        var response = new com.pars.financial.dto.GenericResponse<List<BatchDto>>();
        try {
            var batches = batchService.getBatchesByCompany(companyId);
            response.data = batches;
        } catch (Exception e) {
            logger.error("Error fetching batches by company {}: {}", companyId, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }

    @GetMapping("/user/{userId}")
    public com.pars.financial.dto.GenericResponse<List<BatchDto>> getBatchesByUser(@PathVariable Long userId) {
        logger.info("GET /api/v1/batches/user/{} called", userId);
        var response = new com.pars.financial.dto.GenericResponse<List<BatchDto>>();
        try {
            var batches = batchService.getBatchesByUser(userId);
            response.data = batches;
        } catch (Exception e) {
            logger.error("Error fetching batches by user {}: {}", userId, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
        }
        return response;
    }
} 