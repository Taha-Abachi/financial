package com.pars.financial.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.dto.GenericResponse;
import com.pars.financial.service.DiscountCodeService;

@RestController
@RequestMapping("/api/v1/discountcode")
public class DiscountCodeController {

    private final DiscountCodeService codeService;

    public DiscountCodeController(DiscountCodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("/{code}")
    public GenericResponse<DiscountCodeDto> discountCode(@PathVariable String code) {
        GenericResponse<DiscountCodeDto> response = new GenericResponse<>();
        var dto = codeService.getDiscountCode(code);
        if(dto == null){
            response.status = -1;
            response.message = "Discount code not found";
        }
        return response;
    }

    @PostMapping("/issue")
    public GenericResponse<DiscountCodeDto> issueDiscountCode(@RequestBody DiscountCodeIssueRequest discountCodeDto) {
        GenericResponse<DiscountCodeDto> response = new GenericResponse<>();
        var dto = codeService.generate(discountCodeDto);
        response.data = dto;
        return response;
    }

    @PostMapping("/issuelist")
    public GenericResponse<List<DiscountCodeDto>> redeemDiscountCode(@RequestBody DiscountCodeIssueRequest discountCodeDto) {
        GenericResponse<List<DiscountCodeDto>> response = new GenericResponse<>();
        response.data = codeService.generateList(discountCodeDto);
        return response;
    }

}
