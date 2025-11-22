package com.pars.financial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.dto.CustomerUpdateRequest;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.PagedResponse;
import com.pars.financial.entity.Customer;
import com.pars.financial.exception.CustomerNotFoundException;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.service.CustomerService;
import com.pars.financial.service.SecurityContextService;
import com.pars.financial.utils.ApiUserUtil;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(name = "Customer Management", description = "APIs for managing customer information")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final SecurityContextService securityContextService;

    public CustomerController(CustomerService customerService, SecurityContextService securityContextService) {
        this.customerService = customerService;
        this.securityContextService = securityContextService;
    }

    @Operation(
        summary = "Get all customers (ADMIN/SUPERADMIN only)",
        description = "Retrieves a paginated list of all customers. Only accessible by ADMIN and SUPERADMIN roles."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customers retrieved successfully",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - authentication required",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        )
    })
    @GetMapping("/list")
    public ResponseEntity<GenericResponse<PagedResponse<CustomerDto>>> getAllCustomers(
        @Parameter(description = "Page number (0-indexed)", required = false)
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", required = false)
        @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("GET /api/v1/customer/list called with pagination - page: {}, size: {}", page, size);
        var response = new GenericResponse<PagedResponse<CustomerDto>>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            // RBAC check: Only ADMIN and SUPERADMIN can access this endpoint
            var currentUser = securityContextService.getCurrentUserOrThrow();
            String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : null;
            
            if (roleName == null || (!"ADMIN".equals(roleName) && !"SUPERADMIN".equals(roleName))) {
                logger.warn("User {} with role {} attempted to access customer list without permission", 
                           currentUser.getUsername(), roleName);
                response.status = -1;
                response.message = "Access denied. Only ADMIN and SUPERADMIN can access this endpoint.";
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            PagedResponse<CustomerDto> pagedCustomers = customerService.getAllCustomers(page, size);

            if (pagedCustomers.getContent() == null || pagedCustomers.getContent().isEmpty()) {
                logger.debug("No customers found");
                response.status = 0;
                response.message = "No customers found";
            } else {
                response.message = "Customers retrieved successfully";
            }
            response.data = pagedCustomers;
            response.status = 200;
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.warn("Authentication error fetching customers: {}", e.getMessage());
            response.status = 401;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Error fetching customers: {}", e.getMessage(), e);
            response.status = -1;
            response.message = "Error fetching customers: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Get customer by phone number",
        description = "Retrieves customer information using their primary phone number"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customer found",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        )
    })
    @GetMapping("/{primaryPhoneNumber}")
    public GenericResponse<Customer> getCustomer(
        @Parameter(description = "Primary phone number of the customer", required = true)
        @PathVariable String primaryPhoneNumber
    ) {
        GenericResponse<Customer> genericResponseDto = new GenericResponse<>();
        var ls = customerService.getCustomer(primaryPhoneNumber);
        if(ls == null){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Customer not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @Operation(
        summary = "Create new customer",
        description = "Creates a new customer with the provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customer created successfully",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        )
    })
    @PostMapping("/add")
    public GenericResponse<Customer> issueCustomer(
        @Parameter(description = "Customer information", required = true)
        @RequestBody CustomerDto dto
    ) {
        var genericResponseDto = new GenericResponse<Customer>();
        if(dto.phoneNumber == null){
            throw ValidationException.requiredFieldMissing("phoneNumber");
        }
        genericResponseDto.data = customerService.createCustomer(dto);
        return genericResponseDto;
    }

    @Operation(
        summary = "Get customer by ID",
        description = "Retrieves customer information using their ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customer found",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        )
    })
    @GetMapping("/id/{id}")
    public ResponseEntity<GenericResponse<Customer>> getCustomerById(
        @Parameter(description = "ID of the customer", required = true)
        @PathVariable Long id
    ) {
        logger.info("GET /api/v1/customer/id/{} called", id);
        var response = new GenericResponse<Customer>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            Customer customer = customerService.getCustomerById(id);
            response.data = customer;
            response.status = 200;
            response.message = "Customer retrieved successfully";
            return ResponseEntity.ok(response);

        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with ID: {}", id);
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Error fetching customer with ID {}: {}", id, e.getMessage(), e);
            response.status = -1;
            response.message = "Error fetching customer: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Update customer",
        description = "Updates an existing customer's information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Customer updated successfully",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Customer not found",
            content = @Content(schema = @Schema(implementation = GenericResponse.class))
        )
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<GenericResponse<Customer>> updateCustomer(
        @Parameter(description = "ID of the customer to update", required = true)
        @PathVariable Long id,
        @Parameter(description = "Customer update information", required = true)
        @RequestBody CustomerUpdateRequest request
    ) {
        logger.info("PUT /api/v1/customer/update/{} called", id);
        var response = new GenericResponse<Customer>();

        try {
            ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
            if (userResult.isError()) {
                response.message = userResult.errorMessage;
                response.status = 401;
                return ResponseEntity.status(userResult.httpStatus).body(response);
            }

            Customer updatedCustomer = customerService.updateCustomer(id, request);
            response.data = updatedCustomer;
            response.status = 200;
            response.message = "Customer updated successfully";
            return ResponseEntity.ok(response);

        } catch (CustomerNotFoundException e) {
            logger.warn("Customer not found with ID: {}", id);
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (ValidationException e) {
            logger.warn("Validation error updating customer {}: {}", id, e.getMessage());
            response.status = -1;
            response.message = e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error updating customer {}: {}", id, e.getMessage(), e);
            response.status = -1;
            response.message = "Error updating customer: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
