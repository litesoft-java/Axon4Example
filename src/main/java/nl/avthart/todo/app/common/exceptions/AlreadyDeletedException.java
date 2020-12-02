package nl.avthart.todo.app.common.exceptions;

public class AlreadyDeletedException extends BadRequestException {

	private static final long serialVersionUID = 1518440584190922771L;

	public AlreadyDeletedException( String message) {
		super(message);
	}
}
