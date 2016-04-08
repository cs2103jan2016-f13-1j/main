//@@author A0126297X

package main.parser.exceptions;

/**
 * This exception is thrown when there is nothing after a #.
 * Nothing can be extracted by the method getFirstWord() in CommandParser.
 */
@SuppressWarnings("serial")
public class InvalidLabelFormat extends Exception {
    public InvalidLabelFormat(String message) {
        super(message);
    }
}