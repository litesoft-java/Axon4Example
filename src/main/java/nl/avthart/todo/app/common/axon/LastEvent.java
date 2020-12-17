package nl.avthart.todo.app.common.axon;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LastEvent {
    long globalIndex;
    String aggregateIdentifier;
    long sequenceNumber;
    String payloadType;
}
