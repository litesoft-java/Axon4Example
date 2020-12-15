package nl.avthart.todo.app.common.axon;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LastEventSqlSupport {
    String tableName;
    boolean fullyQualifiedAggregateType;
    String aggregateType;
    String globalIndex;
    String aggregateIdentifier;
    String sequenceNumber;
    String payloadType;
}
