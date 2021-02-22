package nl.avthart.todo.app.dto;

import java.time.Instant;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Optionals {
    protected Optional<String> title;
    protected Optional<Boolean> completed;
    protected Optional<Long> version;
    protected Optional<Instant> lastModified;
}
