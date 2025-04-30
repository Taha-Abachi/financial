package com.pars.financial.mapper;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.entity.DiscountCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiscountCodeMapper {
    public DiscountCodeDto getFrom(DiscountCode code) {
        if(code == null) {
            return null;
        }
        DiscountCodeDto dto = new DiscountCodeDto();
        dto.code = code.getCode();
        dto.serialNo = code.getSerialNo();
        var valDays = code.getExpiryDate().toEpochDay() - code.getIssueDate().toLocalDate().toEpochDay();
        dto.remainingValidityPeriod = valDays >= 0 ? valDays : 0;
        dto.maxDiscountAmount = code.getMaxDiscountAmount();
        dto.percentage = code.getPercentage();
        dto.used = code.isUsed();
        dto.active = code.isActive();
        dto.issueDate = code.getIssueDate();
        dto.redeemDate = code.getRedeemDate();
        return dto;
    }

    public List<DiscountCodeDto> getFrom(List<DiscountCode> codes) {
        if(codes == null) {
            return null;
        }
        List<DiscountCodeDto> dtos = new ArrayList<>();
        for (DiscountCode code : codes) {
            dtos.add(getFrom(code));
        }
        return dtos;
    }
}
