package app.school.administration.auth.infrastructure.persistence.entity.embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantId implements Serializable {

    private UUID id;
    private Instant openedDate;

}
