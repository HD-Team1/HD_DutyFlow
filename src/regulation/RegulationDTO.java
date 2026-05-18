package regulation;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RegulationDTO {

    private int regulationId;
    private int categoryId;

    private int limitCapacity;

    private LocalDate establishedDate;

    private int overageRate;
}