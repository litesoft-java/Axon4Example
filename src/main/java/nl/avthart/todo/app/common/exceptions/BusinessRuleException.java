package nl.avthart.todo.app.common.exceptions;

public class BusinessRuleException extends BadRequestException {
    public BusinessRuleException( String message, Throwable... cause ) {
        super( message, cause );
	}
}
