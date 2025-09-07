package com.pars.financial.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.entity.Customer;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.service.CustomerService;

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

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
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
            throw new ValidationException("Phone number is required", null, -103);
        }
        genericResponseDto.data = customerService.createCustomer(dto);
        return genericResponseDto;
    }
}
