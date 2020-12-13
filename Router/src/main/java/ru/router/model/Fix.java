package ru.router.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.router.utils.StringUtil;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
public class Fix {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull(message = "Значение поля 'brokerId' не может быть пустым.")
    @Length(min = 6, max = 6, message = "Допустимая длина brokerId - 6 символов")
    private String brokerId;
    @NotNull(message = "Значение поля 'dealType' не может быть пустым.")
    @Length(min = 1, max = 1, message = "Допустимая длина dealType - 1 символ")
    private String dealType;
    @NotNull(message = "Значение поля 'instrument' не может быть пустым.")
    @Length(min = 3, max = 3, message = "Допустимая длина instrument - 3 символа")
    private String instrument;
    @NotNull(message = "Значение поля 'price' не может быть пустым.")
    @Length(min = 1, message = "Минимальная длина price - 1 символ")
    private String price;
    @NotNull(message = "Значение поля 'count' не может быть пустым.")
    @Length(min = 1, max = 10, message = "Допустимая длина count от 1 до 10 символов символов")
    private String count;
    @NotNull(message = "Значение поля 'marketId' не может быть пустым.")
    @Length(min = 6, max = 6, message = "Допустимая длина marketId - 6 символов")
    private String marketId;
    @NotNull(message = "Значение поля 'checkSum' не может быть пустым.")
    @Length(min = 1, max = 19, message = "Допустимая длина checkSum: от 1 до 19 символов")
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
        count = collect.get(StringUtil.COUNT);
        marketId = collect.get(StringUtil.MARKET_ID);
        checkSum = collect.get(StringUtil.CHECK_SUM).trim();
        if (collect.containsKey("id")) {
            this.id = Long.parseLong(collect.get("id"));
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(StringUtil.BROKER_ID).append("=").append(brokerId).append("|")
                .append("id").append("=").append(id).append("|")
                .append(StringUtil.DEAL_TYPE).append("=").append(dealType).append("|")
                .append(StringUtil.INSTRUMENT).append("=").append(instrument).append("|")
                .append(StringUtil.PRICE).append("=").append(price).append("|")
                .append(StringUtil.COUNT).append("=").append(count).append("|")
                .append(StringUtil.MARKET_ID).append("=").append(marketId).append("|")
                .append(StringUtil.CHECK_SUM).append("=").append(checkSum)
                .append(" ");
        return stringBuilder.toString();
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }
}
