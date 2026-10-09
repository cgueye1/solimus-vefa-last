package com.wakana.vefa.util;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.jsoup.parser.Parser;

public class XssSanitizer {

    private static final Safelist SAFE_SIMPLE_HTML = Safelist.basic()
        .addTags("u") 
        .addTags("p")  
        .addTags("br");

    private XssSanitizer() {}

    public static String clean(String input) {
        if (input == null) return null;

        String decodedInput = Parser.unescapeEntities(input, true);

        return Jsoup.clean(decodedInput, SAFE_SIMPLE_HTML);
    }
}
