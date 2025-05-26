package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.GiftCardDto;
import com.pars.financial.entity.GiftCard;

@Component
public class GiftCardMapper {
    public GiftCardDto getFrom(GiftCard gc) {
        if(gc == null) {
            return null;
        }
        GiftCardDto dto = new GiftCardDto();
        dto.serialNo = gc.getSerialNo();
        dto.balance = gc.getBalance();
        var valDays = gc.getExpiryDate().toEpochDay() - gc.getIssueDate().toEpochDay();
        dto.remainingValidityPeriod = valDays <=0 ? 0 : valDays;
        dto.expiryDate = gc.getExpiryDate();
        dto.realAmount = gc.getRealAmount();
        dto.isActive = gc.isActive();
        dto.isBlocked = gc.isBlocked();
        dto.isAnonymous = (gc.getCustomer() == null);
        return dto;
    }

    public List<GiftCardDto> getFrom(List<GiftCard> gcList) {
        List<GiftCardDto> dtos = new ArrayList<>();
        for (var i = 0; i < gcList.size(); i++) {
            dtos.add(getFrom(gcList.get(i)));
        }
        return dtos;
    }
}
