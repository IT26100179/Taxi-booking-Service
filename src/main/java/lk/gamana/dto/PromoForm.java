package lk.gamana.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PromoForm {
    @NotBlank(message = "Promo code is required")
    private String code;

    @Min(value = 1, message = "Discount percentage must be at least 1%")
    @Max(value = 99, message = "Discount percentage cannot exceed 99%")
    private int discountPct;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate validTo;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private int usageLimit;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscountPct() { return discountPct; }
    public void setDiscountPct(int discountPct) { this.discountPct = discountPct; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
}
