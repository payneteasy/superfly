package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.ActionDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SendSystemDataRequest {
    private String                  subsystemIdentifier;
    private List<ActionDescription> actionDescriptions;
}
