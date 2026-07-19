package com.payneteasy.superfly.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeSubsystemTokenRequest implements Serializable {
    private String subsystemToken;
}
