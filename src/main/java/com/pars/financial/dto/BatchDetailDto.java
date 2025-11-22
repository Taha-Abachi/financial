package com.pars.financial.dto;

import java.util.List;

/**
 * DTO for batch details including the batch information
 * and the list of gift cards or discount codes created in the batch
 */
public class BatchDetailDto {
    private BatchDto batch;
    private List<GiftCardDto> giftCards;
    private List<DiscountCodeDto> discountCodes;

    public BatchDetailDto() {}

    public BatchDetailDto(BatchDto batch, List<GiftCardDto> giftCards, List<DiscountCodeDto> discountCodes) {
        this.batch = batch;
        this.giftCards = giftCards;
        this.discountCodes = discountCodes;
    }

    public BatchDto getBatch() {
        return batch;
    }

    public void setBatch(BatchDto batch) {
        this.batch = batch;
    }

    public List<GiftCardDto> getGiftCards() {
        return giftCards;
    }

    public void setGiftCards(List<GiftCardDto> giftCards) {
        this.giftCards = giftCards;
    }

    public List<DiscountCodeDto> getDiscountCodes() {
        return discountCodes;
    }

    public void setDiscountCodes(List<DiscountCodeDto> discountCodes) {
        this.discountCodes = discountCodes;
    }
}

