package com.pars.financial.controller;

import com.pars.financial.dto.GenericResponse;
import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.dto.GiftCardIssueRequest;
import com.pars.financial.dto.StoreLimitationRequest;
import com.pars.financial.entity.GiftCard;
import com.pars.financial.service.GiftCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/giftcard")
public class GiftCardController {

    private static final Logger logger = LoggerFactory.getLogger(GiftCardController.class);

    final GiftCardService giftCardService;

    public GiftCardController(GiftCardService giftCardService){
        this.giftCardService = giftCardService;
    }

    @GetMapping("/all")
    public GenericResponse<List<GiftCardDto>> getGiftCards(){
        logger.info("GET /api/v1/giftcard/all called");
        GenericResponse<List<GiftCardDto>> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCards();
        if((ls == null)|| (ls.isEmpty())){
            logger.warn("Gift card list not found");
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card list not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @GetMapping("/identifier/{identifier}")
    public GenericResponse<GiftCardDto> getGiftCards(@PathVariable Long identifier){
        logger.info("GET /api/v1/giftcard/identifier/{} called", identifier);
        GenericResponse<GiftCardDto> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCard(identifier);
        if(ls == null){
            logger.warn("Gift card not found for identifier {}", identifier);
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @GetMapping("/{serialNo}")
    public GenericResponse<GiftCardDto> getGiftCards(@PathVariable String serialNo){
        logger.info("GET /api/v1/giftcard/{} called", serialNo);
        GenericResponse<GiftCardDto> genericResponseDto = new GenericResponse<>();
        var ls = giftCardService.getGiftCard(serialNo);
        if(ls == null){
            logger.warn("Gift card not found for serialNo {}", serialNo);
            genericResponseDto.status = -1;
            genericResponseDto.message = "Gift card not found";
        }
        genericResponseDto.data = ls;
        return genericResponseDto;
    }

    @PostMapping("/issue")
    public GenericResponse<GiftCardDto> issueGiftCard(@RequestBody GiftCardIssueRequest dto){
        logger.info("POST /api/v1/giftcard/issue called with request: {}", dto);
        var genericResponseDto = new GenericResponse<GiftCardDto>();
        var gc = giftCardService.generateGiftCard(dto.getRealAmount(), dto.getBalance(), dto.getRemainingValidityPeriod());
        genericResponseDto.data = gc ;
        return genericResponseDto;
    }

    @PostMapping("/issuelist")
    public GenericResponse<List<GiftCardDto>> issueGiftCards(@RequestBody GiftCardIssueRequest dto){
        logger.info("POST /api/v1/giftcard/issuelist called with request: {}", dto);
        var genericResponseDto = new GenericResponse<List<GiftCardDto>>();
        var gcl = giftCardService.generateGiftCards(dto.getRealAmount(), dto.getBalance(), dto.getRemainingValidityPeriod(), dto.getCount());
        genericResponseDto.data = gcl ;
        return genericResponseDto;
    }

    @PostMapping("/{serialNo}/limit-stores")
    public GenericResponse<Void> limitToStores(@PathVariable String serialNo, @RequestBody StoreLimitationRequest request) {
        logger.info("POST /api/v1/giftcard/{}/limit-stores called with storeIds: {}", serialNo, request.storeIds);
        var response = new GenericResponse<Void>();
        giftCardService.limitToStores(serialNo, request.storeIds);
        return response;
    }

    @PostMapping("/{serialNo}/remove-store-limitation")
    public GenericResponse<Void> removeStoreLimitation(@PathVariable String serialNo) {
        logger.info("POST /api/v1/giftcard/{}/remove-store-limitation called", serialNo);
        var response = new GenericResponse<Void>();
        giftCardService.removeStoreLimitation(serialNo);
        return response;
    }
}
