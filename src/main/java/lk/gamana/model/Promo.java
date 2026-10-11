package lk.gamana.model;

import java.time.LocalDate;

public class Promo {
    private String code;
    private int discountPct;
    private LocalDate validTo;
    private int usageLimit;
    private int usedCount;

    public Promo() {}

    public Promo(String code, int discountPct, LocalDate validTo, int usageLimit, int usedCount) {
        this.code = code;
        this.discountPct = discountPct;
        this.validTo = validTo;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
    }

    public static Promo fromRow(String[] row) {
        if (row.length < 5) {
            throw new IllegalArgumentException("Invalid promo row length: " + row.length);
        }
        return new Promo(
                row[0],
                Integer.parseInt(row[1]),
                LocalDate.parse(row[2]),
                Integer.parseInt(row[3]),
                Integer.parseInt(row[4])
        );
    }

    public String[] toRow() {
        return new String[]{
                code != null ? code : "",
                String.valueOf(discountPct),
                validTo != null ? validTo.toString() : LocalDate.now().toString(),
                String.valueOf(usageLimit),
                String.valueOf(usedCount)
        };
    }

    public boolean isValid() {
        return (validTo == null || !validTo.isBefore(LocalDate.now())) && usedCount < usageLimit;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getDiscountPct() { return discountPct; }
    public void setDiscountPct(int discountPct) { this.discountPct = discountPct; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
}
