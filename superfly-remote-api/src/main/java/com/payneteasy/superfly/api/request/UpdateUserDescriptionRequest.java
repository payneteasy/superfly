package com.payneteasy.superfly.api.request;

import com.payneteasy.superfly.api.UserDescription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDescriptionRequest {
    private UserDescription userDescription;
}
