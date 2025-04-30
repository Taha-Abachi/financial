package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.GiftCardIssueRequest;
import com.pars.financial.dto.StoreLimitationRequest;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.service.GiftCardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giftcard")
public class GiftCardController {

    final GiftCardService giftCardService;

    public GiftCardController(GiftCardService giftCardService){
        this.giftCardService = giftCardService;
    }

    @GetMapping("/all")
    public GenericResponse<List<GiftCardDto>> getGiftCards(){
        GenericResponse<List<GiftCardDto>> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCards();
        if((ls == null)|| (ls.isEmpty())){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card list not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @GetMapping("/identifier/{identifier}")
    public GenericResponse<GiftCard> getGiftCards(@PathVariable Long identifier){
        GenericResponse<GiftCard> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCard(identifier);
        if(ls == null){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @GetMapping("/{serialNo}")
    public GenericResponse<GiftCard> getGiftCards(@PathVariable String serialNo){
        GenericResponse<GiftCard> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCard(serialNo);
        if(ls == null){
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @PostMapping("/issue")
    public GenericResponse<GiftCardDto> issueGiftCard(@RequestBody GiftCardIssueRequest dto){
        var genericResponseDto = new GenericResponse<GiftCardDto>();
        var gc = giftCardService.generateGiftCard(dto.getRealAmount(), dto.getBalance(), dto.getRemainingValidityPeriod());
        genericResponseDto.data = gc ;
        return genericResponseDto;
    }

    @PostMapping("/issuelist")
    public GenericResponse<List<GiftCardDto>> issueGiftCards(@RequestBody GiftCardIssueRequest dto){
        var genericResponseDto = new GenericResponse<List<GiftCardDto>>();
        var gcl = giftCardService.generateGiftCards(dto.getRealAmount(), dto.getBalance(), dto.getRemainingValidityPeriod(), dto.getCount());
        genericResponseDto.data = gcl ;
        return genericResponseDto;
    }

    @PostMapping("/{serialNo}/limit-stores")
    public GenericResponse<Void> limitToStores(@PathVariable String serialNo, @RequestBody StoreLimitationRequest request) {
        var response = new GenericResponse<Void>();
        giftCardService.limitToStores(serialNo, request.storeIds);
        return response;
    }

    @PostMapping("/{serialNo}/remove-store-limitation")
    public GenericResponse<Void> removeStoreLimitation(@PathVariable String serialNo) {
        var response = new GenericResponse<Void>();
        giftCardService.removeStoreLimitation(serialNo);
        return response;
    }
}
