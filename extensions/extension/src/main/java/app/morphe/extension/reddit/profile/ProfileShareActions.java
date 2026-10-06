package app.morphe.extension.reddit.profile;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileShareActions {

    private static final Pattern PROFILE_LINK =
            Pattern.compile("^https?://(www\\.|old\\.)?reddit\\.com/(?:user|u)/([^/?#]+).*", Pattern.CASE_INSENSITIVE);

    public static String extractUsername(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Matcher matcher = PROFILE_LINK.matcher(url.trim());
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    public static String ghostdditUrl(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        return "https://ghostddit.aeddit.com/user/" + encodeUsername(username) + "/";
    }

    // Same output as android.net.Uri.encode(username), which is unavailable in
    // unit tests (android.jar stub throws). Leaves A-Z a-z 0-9 and -_.!~'()*
    // unescaped, percent-encodes everything else as UTF-8 with uppercase hex.
    private static String encodeUsername(String username) {
        byte[] bytes = username.getBytes(StandardCharsets.UTF_8);
        StringBuilder encoded = new StringBuilder(bytes.length);
        for (byte b : bytes) {
            int c = b & 0xFF;
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || "-_.!~'()*".indexOf(c) >= 0) {
                encoded.append((char) c);
            } else {
                encoded.append('%');
                encoded.append(Character.toUpperCase(Character.forDigit(c >>> 4, 16)));
                encoded.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
            }
        }
        return encoded.toString();
    }
}
