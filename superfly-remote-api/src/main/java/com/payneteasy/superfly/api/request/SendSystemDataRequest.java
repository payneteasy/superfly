package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.ActionDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendSystemDataRequest implements Serializable {
    private String                  subsystemIdentifier;
    private List<ActionDescription> actionDescriptions;
}
