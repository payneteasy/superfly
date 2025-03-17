package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TouchSessionsRequest {
    private List<Long> sessionIds;
}
