package com.payneteasy.superfly.api.request;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GetUserStatusesRequest {
    private List<String> userNames;
}
