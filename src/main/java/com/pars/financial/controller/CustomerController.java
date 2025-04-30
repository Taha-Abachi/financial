package com.pars.financial.controller;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.entity.Customer;
import com.pars.financial.exception.ValidationException;
import com.pars.financial.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{primaryPhoneNumber}")
    public GenericResponse<Customer> getCustomer(@PathVariable String primaryPhoneNumber) {
        GenericResponse<Customer> genericResponseDto = new GenericResponse<>();
        var ls = customerService.getCustomer(primaryPhoneNumber);
        if(ls == null){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Customer not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @PostMapping("/add")
    public GenericResponse<Customer> issueCustomer(@RequestBody CustomerDto dto){
        var genericResponseDto = new GenericResponse<Customer>();
        if(dto.phoneNumber == null){
            throw new ValidationException("Phone number is required");
        }
        genericResponseDto.data = customerService.createCustomer(dto);
        return genericResponseDto;
    }
}
