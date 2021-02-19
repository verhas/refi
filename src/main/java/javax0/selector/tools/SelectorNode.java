package javax0.selector.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SelectorNode {

    public static class Or extends SelectorNode {
        public final Set<SelectorNode> subNodes = new HashSet<>();
    }

    public static class And extends SelectorNode {
        public final Set<SelectorNode> subNodes = new HashSet<>();
    }

    public static class Not extends SelectorNode {
        public final SelectorNode subNode;

        Not(SelectorNode subNode) {
            this.subNode = subNode;
        }
    }

    public static class Converted extends SelectorNode {
        public final SelectorNode subNode;
        public final String converter;

        Converted(SelectorNode subNode, String converter) {
            this.converter = converter;
            this.subNode = subNode;
        }
    }


    public static class Terminal extends SelectorNode {
        public final String terminal;

        Terminal(String terminal) {
            this.terminal = terminal;
        }
    }

    public static class Regex extends SelectorNode {
        public final Pattern regex;
        public final String name;

        Regex(String regex, String name) {
            this.regex = Pattern.compile(regex);
            this.name = name;
        }
    }
}
