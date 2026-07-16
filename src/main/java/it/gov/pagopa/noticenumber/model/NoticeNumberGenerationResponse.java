package it.gov.pagopa.noticenumber.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeNumberGenerationResponse {

    @NotNull
    private String noticeNumber;
}
