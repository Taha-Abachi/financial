package com.pars.financial.controller;

import com.pars.financial.dto.BatchCreateRequest;
import com.pars.financial.dto.BatchDto;
import com.pars.financial.dto.BatchReportDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.entity.Batch;
import com.pars.financial.service.BatchService;
import com.pars.financial.service.BatchReportService;
import com.pars.financial.utils.ApiUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {
    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    private final BatchService batchService;
    private final BatchReportService batchReportService;

    public BatchController(BatchService batchService, BatchReportService batchReportService) {
        this.batchService = batchService;
        this.batchReportService = batchReportService;
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

    // ===== BATCH REPORT ENDPOINTS =====

    @GetMapping("/{batchId}/report")
    public ResponseEntity<GenericResponse<BatchReportDto>> getBatchReport(@PathVariable Long batchId) {
        logger.info("GET /api/v1/batches/{}/report called", batchId);
        var response = new GenericResponse<BatchReportDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            response.message = userResult.errorMessage;
            response.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(response);
        }

        try {
            BatchReportDto report = batchReportService.generateBatchReport(batchId);
            if (report == null) {
                response.message = "Batch not found";
                response.status = 404;
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.data = report;
            response.message = "Batch report generated successfully";
            response.status = 200;
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating batch report for batch {}: {}", batchId, e.getMessage());
            response.message = "Error generating batch report: " + e.getMessage();
            response.status = 500;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/reports/all")
    public ResponseEntity<GenericResponse<List<BatchReportDto>>> getAllBatchesReport() {
        logger.info("GET /api/v1/batches/reports/all called");
        var response = new GenericResponse<List<BatchReportDto>>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            response.message = userResult.errorMessage;
            response.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(response);
        }

        try {
            List<BatchReportDto> reports = batchReportService.generateAllBatchesReport();
            response.data = reports;
            response.message = "All batches report generated successfully";
            response.status = 200;
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating all batches report: {}", e.getMessage());
            response.message = "Error generating all batches report: " + e.getMessage();
            response.status = 500;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/reports/company/{companyId}")
    public ResponseEntity<GenericResponse<List<BatchReportDto>>> getBatchesReportByCompany(@PathVariable Long companyId) {
        logger.info("GET /api/v1/batches/reports/company/{} called", companyId);
        var response = new GenericResponse<List<BatchReportDto>>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            response.message = userResult.errorMessage;
            response.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(response);
        }

        try {
            List<BatchReportDto> reports = batchReportService.generateBatchesReportByCompany(companyId);
            response.data = reports;
            response.message = "Company batches report generated successfully";
            response.status = 200;
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating company batches report for company {}: {}", companyId, e.getMessage());
            response.message = "Error generating company batches report: " + e.getMessage();
            response.status = 500;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{batchId}/summary")
    public ResponseEntity<GenericResponse<BatchReportDto>> getBatchSummary(@PathVariable Long batchId) {
        logger.info("GET /api/v1/batches/{}/summary called", batchId);
        var response = new GenericResponse<BatchReportDto>();
        
        ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
        if (userResult.isError()) {
            response.message = userResult.errorMessage;
            response.status = 401;
            return ResponseEntity.status(userResult.httpStatus).body(response);
        }

        try {
            BatchReportDto report = batchReportService.generateBatchReport(batchId);
            if (report == null) {
                response.message = "Batch not found";
                response.status = 404;
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.data = report;
            response.message = "Batch summary generated successfully";
            response.status = 200;
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error generating batch summary for batch {}: {}", batchId, e.getMessage());
            response.message = "Error generating batch summary: " + e.getMessage();
            response.status = 500;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 