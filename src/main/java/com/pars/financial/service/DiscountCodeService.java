package com.pars.financial.service;

import com.pars.financial.dto.DiscountCodeDto;
import com.pars.financial.dto.DiscountCodeIssueRequest;
import com.pars.financial.entity.DiscountCode;
import com.pars.financial.mapper.DiscountCodeMapper;
import com.pars.financial.repository.DiscountCodeRepository;
import com.pars.financial.utils.RandomStringGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DiscountCodeService {

    private final DiscountCodeRepository codeRepository;
    private final DiscountCodeMapper mapper;
    private final Random random = new Random();

    public DiscountCodeService(DiscountCodeRepository codeRepository, DiscountCodeMapper mapper) {
        this.codeRepository = codeRepository;
        this.mapper = mapper;
    }

    private DiscountCode issueDiscountCode(int percentage, long validityPeriod, long maxDiscountAmount) {
        var code = new DiscountCode();
        code.setIssueDate(LocalDateTime.now());
        code.setExpiryDate(LocalDate.now().plusDays(validityPeriod));
        code.setCode(RandomStringGenerator.generateRandomUppercaseStringWithNumbers(8));
        code.setSerialNo(random.nextLong(100000000L));
        code.setPercentage(percentage);
        code.setMaxDiscountAmount(maxDiscountAmount);
        return code;
    }

    public DiscountCodeDto generate(DiscountCodeIssueRequest dto) {
        return mapper.getFrom(codeRepository.save(issueDiscountCode(dto.percentage, dto.remainingValidityPeriod, dto.maxDiscountAmount)));
    }

    public List<DiscountCodeDto> generateList(DiscountCodeIssueRequest request) {
        var ls = new ArrayList<DiscountCode>();
        for (var i = 0; i < request.count; i++) {
            ls.add(issueDiscountCode(request.percentage, request.remainingValidityPeriod, request.maxDiscountAmount));
        }
        codeRepository.saveAll(ls);
        return mapper.getFrom(ls);
    }

    public DiscountCodeDto getDiscountCode(String code) {
        return mapper.getFrom(codeRepository.findByCode(code));
    }

}
