package cn.ecosync.ibms.query;

import cn.ecosync.ibms.model.BacnetReadPropertyMultipleService;
import cn.ecosync.ibms.model.ReadPropertyMultipleAck;
import cn.ecosync.iframework.query.Query;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class BacnetReadPropertyMultipleBatchQuery implements Query<List<ReadPropertyMultipleAck>> {
    @Valid
    @NotEmpty
    @JsonUnwrapped
    private List<BacnetReadPropertyMultipleService> services;

    protected BacnetReadPropertyMultipleBatchQuery() {
    }

    public BacnetReadPropertyMultipleBatchQuery(List<BacnetReadPropertyMultipleService> services) {
        this.services = services;
    }
}
