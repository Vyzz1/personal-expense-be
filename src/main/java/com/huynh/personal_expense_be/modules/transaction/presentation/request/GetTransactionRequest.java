
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GetTransactionRequest {
    private int page = 0;
    private int size = 10;
    private String sortBy = "occurredAt";
    private String sortOrder = "desc";
    private String description;
    private List<UUID> categoryIds;
    private String type;
    private String fromDate;
    private String toDate;
}
