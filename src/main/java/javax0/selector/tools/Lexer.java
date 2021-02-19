package javax0.selector.tools;

import javax0.selector.Selector;

/**
 * Lexical analyzer used to analyze strings in selector expressions (see {@link Selector}.
 * <p>
 * The selector expression does not need spaces. If there is a space it is simply terminating the previous lexical
 * element. For example {@code private | public} is just the same as {@code private|public}.
 * <p>
 * The lexical analyzer works from a String that is specified for the constructor and the individual lexemes can be
 * fetched calling {@link #get()}. It is also possible to peek ahead calling {@link #peek()} and to get the rest of the
 * string that was not consumed by the analysis calling {@link #rest()}. This method is usually used by error reporting
 * and is not needed for the analysis.
 * <p>
 * Lexemes are returned as instances of {@link Lexeme}.
 */
public class Lexer {
    private static final Lexeme EOF = new Lexeme("", Lexeme.Type.EOF);
    private final StringBuilder input;
    private Lexeme lookAhead = null;

    /**
     * Create a new lexical analyzer.
     *
     * @param input the string containing the expression to be analyzed
     */
    public Lexer(final String input) {
        this.input = new StringBuilder(input);
    }

    /**
     * A simple string conversion that shows the characters that are not yet processed. This is mainly used for
     * debugging.
     *
     * @return the characters that were not processed yet enclosed between " characters
     */
    @Override
    public String toString() {
        return "\"" + rest() + "\"";
    }

    /**
     * The characters that were not processed yet. This method is used to ease error reporting. When there is a syntax
     * error the error reporting code may invoke this method to give a hint to the user where the syntax analysis got
     * stopped. This, of course, also assumes that the syntax analysis does not read many lexemes ahead.
     *
     * @return the characters that were not processed yet.
     */
    public String rest() {
        if (lookAhead == null) {
            return input.toString();
        } else {
            return lookAhead + input.toString();
        }
    }

    /**
     * Get the next lexeme from the input and consume it. Consecutive calls to {@code get()} will get the lexemes one
     * after the other.
     *
     * @return the next lexeme ot the EOF lexeme in case there are no more characters on the input
     */
    public Lexeme get() {
        final var ret = peek();
        lookAhead = next();
        return ret;
    }

    /**
     * Get the next lexeme from the input but as opposed to {@link #get()} this method does not consume the lexeme. A
     * consecutive call to {@code  #peek()} and then to {@link #get()} will return the same lexeme.
     *
     * @return the next lexeme
     */
    public Lexeme peek() {
        if (lookAhead == null) {
            lookAhead = next();
        }
        return lookAhead;
    }

    /**
     * Get the next lexeme from the input. This method does not look into the look-ahead buffer. This method works
     * directly on the input. This method is called by {@link #get()} and {@link #peek()}, which manage the look-ahead
     * buffer.
     * <p>
     *
     * @return the next lexeme from the input or EOF lexeme in case there are no more characters on the input
     */
    private Lexeme next() {

        skipSpaces();

        if (input.length() == 0) {
            return EOF;
        }

        if (isJavaIdentifierStart()) {
            final var word = new StringBuilder();
            while (isJavaIdentifierPart()) {
                word.append(nextChar());
            }
            return new Lexeme(word.toString(), Lexeme.Type.WORD);
        }

        if (isRegexStart()) {
            final var regex = new StringBuilder();
            step();
            while (!isRegexEnd()) {
                if (isEscapedRegexDelimiter()) {
                    step();
                }
                regex.append(nextChar());
            }
            if (input.length() > 0) {
                step();
            } else {
                throw new IllegalArgumentException("Regular expression is not terminated with closing '/' character.");
            }
            return new Lexeme(regex.toString(), Lexeme.Type.REGEX);
        }
        return new Lexeme("" + nextChar(), Lexeme.Type.SYMBOL);
    }

    private char nextChar() {
        char c = input.charAt(0);
        step();
        return c;
    }

    private void step() {
        input.delete(0, 1);
    }

    private boolean isEscapedRegexDelimiter() {
        return input.length() > 1 && input.charAt(0) == '\\' && input.charAt(1) == '/';
    }

    private boolean isRegexEnd() {
        return input.length() == 0 || input.charAt(0) == '/';
    }

    private boolean isRegexStart() {
        return input.length() > 0 && input.charAt(0) == '/';
    }

    private boolean isJavaIdentifierPart() {
        return input.length() > 0 && Character.isJavaIdentifierPart(input.charAt(0));
    }

    private boolean isJavaIdentifierStart() {
        return input.length() > 0 && Character.isJavaIdentifierStart(input.charAt(0));
    }

    private void skipSpaces() {
        while (isSpace()) {
            step();
        }
    }

    private boolean isSpace() {
        return input.length() > 0 && Character.isWhitespace(input.charAt(0));
    }
}
