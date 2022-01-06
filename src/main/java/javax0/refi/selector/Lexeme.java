package javax0.refi.selector;

/**
 * A lexeme represents a single lexical unit, like an identifier, a string, a number, a single or multi character
 * operator. A lexeme has a string, which is the original representation of the lexical unit in the source and a
 * {@link Type type}.
 */
public class Lexeme {
    public final String string;
    public final Type type;

    public Lexeme(String string, Type type) {
        this.string = string;
        this.type = type;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * The type of the lexeme that can be
     *
     * * a word, some identifier,
     *
     * * a symbol, like `=` or `~`
     *
     * *  
     */
    public enum Type {
        WORD, SYMBOL,REGEX, EOF
    }

}
