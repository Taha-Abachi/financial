package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.StoreDto;
import com.pars.financial.service.StoreService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
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
}
