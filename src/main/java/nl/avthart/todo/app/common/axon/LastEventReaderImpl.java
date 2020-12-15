package nl.avthart.todo.app.common.axon;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LastEventReaderImpl implements LastEventReader {
    private final LastEventSqlSupport sqlSupport;
    private final JdbcTemplate template;

    /**
     * Last Event for an Aggregate Type (null if none) - Dialect works for H2 & PostgreSQL
     *
     * @param aggregateType class of the Aggregate Type
     * @return null if no Last event for Aggregate Type
     */
    @SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
    @Override
    public LastEvent read( Class<? extends AggregateObject> aggregateType ) {
        String aggregateTypeString = sqlSupport.isFullyQualifiedAggregateType() ?
                                     aggregateType.getName() :
                                     aggregateType.getSimpleName();
        String sql = "SELECT " +
                     sqlSupport.getAggregateType() + ", " +
                     sqlSupport.getGlobalIndex() + ", " +
                     sqlSupport.getAggregateIdentifier() + ", " +
                     sqlSupport.getSequenceNumber() + ", " +
                     sqlSupport.getPayloadType() +
                     " FROM " + sqlSupport.getTableName() +
                     " WHERE " + sqlSupport.getAggregateType() + "=" + "'" + aggregateTypeString + "'" +
                     " ORDER BY " + sqlSupport.getGlobalIndex() +
                     " DESC LIMIT 1" +
                     ";";
        return template.query( sql, this::extractData );
    }

    private LastEvent extractData( ResultSet rs )
            throws SQLException, DataAccessException {
        if ( !rs.first() ) {
            return null;
        }
        return LastEvent.builder()
                .globalIndex( rs.getLong( sqlSupport.getGlobalIndex() ) )
                .aggregateIdentifier( rs.getString( sqlSupport.getAggregateIdentifier() ) )
                .sequenceNumber( rs.getLong( sqlSupport.getSequenceNumber() ) )
                .payloadType( rs.getString( sqlSupport.getPayloadType() ) )
                .build();
    }
}

