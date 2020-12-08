package ru.router.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.router.StringUtil;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
public class Fix {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String brokerId;
    private String dealType;
    private String instrument;
    private String price;
    private String count;
    private String marketId;
    private String checkSum;
    private String input;
    private String side;

    public Fix(byte[] bytesInput, int bytesCount) {
        byte[] dataCopy = new byte[bytesCount];
        System.arraycopy(bytesInput, 0, dataCopy, 0, bytesCount);
        String s1 = new String(dataCopy);
        int i = s1.lastIndexOf("|");
        input = s1.substring(0, i);
        String[] split = s1.split("\\|");
        Map<String, String> collect = Arrays.stream(split)
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
        brokerId = collect.get(StringUtil.BROKER_ID);
        dealType = collect.get(StringUtil.DEAL_TYPE);
        instrument = collect.get(StringUtil.INSTRUMENT);
        price = collect.get(StringUtil.PRICE);
        count = collect.get(StringUtil.COUNT);
        marketId = collect.get(StringUtil.MARKET_ID);
        checkSum = collect.get(StringUtil.CHECK_SUM);
    }


    //        49 - идентификатор брокера
//    54 - тип сделки
//        :1 - покупка
//        :2 - продажа
//
//    1  - инструмент (имя его)
//    15 - цена
//    38 - количество лотов
//    56 - идентификатор рынка
//    10 - контрольная сумма


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(StringUtil.BROKER_ID).append("=").append(brokerId).append("|")
                .append(StringUtil.DEAL_TYPE).append("=").append(dealType).append("|")
                .append(StringUtil.INSTRUMENT).append("=").append(instrument).append("|")
                .append(StringUtil.PRICE).append("=").append(price).append("|")
                .append(StringUtil.COUNT).append("=").append(count).append("|")
                .append(StringUtil.MARKET_ID).append("=").append(marketId).append("|")
                .append(StringUtil.CHECK_SUM).append("=").append(checkSum);
        return stringBuilder.toString();
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }
}
