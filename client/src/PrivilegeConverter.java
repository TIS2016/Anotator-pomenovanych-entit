import javafx.util.converter.ByteStringConverter;

/**
 * Created by michal on 12/28/16.
 */
public final class PrivilegeConverter {

    private static final char[] CHARS = new char[]{'e', 'a', 'r'};
    private static final char EMPTY_CHAR = '-';

    private PrivilegeConverter() {}

    private static final ByteStringConverter CONVERTER = new ByteStringConverter() {

        //111 <-> rae
        //000 <-> ---
        @Override
        public String toString(Byte object) {
            if (object != null) {
                String res = "";
                for (int i = CHARS.length - 1; i >= 0; i--) {
                    if ((object & (1 << i)) != 0) {
                        res += CHARS[i];
                    } else {
                        res += EMPTY_CHAR;
                    }
                }
                return res;
            }
            return null;
        }

        @Override
        public Byte fromString(String object) throws IllegalArgumentException {
            if (object != null) {
                if (object.length() != CHARS.length) {
                    throw new IllegalArgumentException(String.format("Expected object size: %d, found: %d",
                            CHARS.length, object.length()));
                }
                byte res = 0;
                for (int i = CHARS.length - 1; i >= 0; i--) {
                    char c = object.charAt(CHARS.length - i - 1);
                    if (c == CHARS[i]) {
                        res |= (1 << i);
                    } else if (c != EMPTY_CHAR) {
                        throw new IllegalArgumentException(String.format("Unknown character: %c", c));
                    }
                }
                return res;
            }
            return null;
        }
    };

    public static String toString(Byte object) {
        return CONVERTER.toString(object);
    }

    public static byte fromString(String object) throws IllegalArgumentException {
        return CONVERTER.fromString(object);
    }
}
