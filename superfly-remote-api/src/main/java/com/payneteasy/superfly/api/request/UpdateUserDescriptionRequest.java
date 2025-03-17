package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.UserDescription;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserDescriptionRequest {
    private UserDescription userDescription;
}
