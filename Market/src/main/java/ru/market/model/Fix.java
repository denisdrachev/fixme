package ru.market.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.market.util.StringUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class Fix {

    private long id;
    private String brokerId;
    private String dealType;
    private String instrument;
    private String price;
    private Integer count;
    private String marketId;
    private String checkSum;
    private String input;
    private String side;
    private boolean status = false;
    private Date time = Calendar.getInstance().getTime();

    public Fix(String inputString) {
        int i = inputString.lastIndexOf("|");
        input = inputString.substring(0, i);
        String[] split = inputString.split("\\|");
        Map<String, String> collect = Arrays.stream(split)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        brokerId = collect.get(StringUtil.BROKER_ID);
        dealType = collect.get(StringUtil.DEAL_TYPE);
        instrument = collect.get(StringUtil.INSTRUMENT);
        price = collect.get(StringUtil.PRICE);
        setCount(collect.get(StringUtil.COUNT));
        marketId = collect.get(StringUtil.MARKET_ID);
        checkSum = collect.get(StringUtil.CHECK_SUM);
        id = Integer.parseInt(collect.get("id"));
    }

    private void setCount(String count) {
        this.count = Integer.parseInt(count);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(StringUtil.BROKER_ID).append("=").append(brokerId).append("|")
                .append(StringUtil.DEAL_TYPE).append("=").append(dealType).append("|")
                .append("id").append("=").append(id).append("|")
                .append(StringUtil.INSTRUMENT).append("=").append(instrument).append("|")
                .append(StringUtil.PRICE).append("=").append(price).append("|")
                .append(StringUtil.COUNT).append("=").append(count).append("|")
                .append(StringUtil.MARKET_ID).append("=").append(marketId).append("|")
                .append(StringUtil.CHECK_SUM).append("=").append(checkSum)
                .append(" ");
        return stringBuilder.toString();
    }
    public String toShortString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(StringUtil.BROKER_ID).append("=").append(brokerId).append("|")
                .append(StringUtil.DEAL_TYPE).append("=").append(dealType).append("|")
                .append("id").append("=").append(id).append("|")
                .append(StringUtil.INSTRUMENT).append("=").append(instrument).append("|")
                .append(StringUtil.PRICE).append("=").append(price).append("|")
                .append(StringUtil.COUNT).append("=").append(count).append("|")
                .append(StringUtil.MARKET_ID).append("=").append(marketId);
        return stringBuilder.toString();
    }
}
