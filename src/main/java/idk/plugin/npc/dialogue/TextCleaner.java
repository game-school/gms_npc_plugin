package idk.plugin.npc.dialogue;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TextCleaner {

    private final String sourceText;
    private final String cleanedText;

    // Watch the order
    private final Map<String, String> substitutions = Stream.of(new String[][] {
            {"¬", ","}, //Comma - contents creators use the NOT SIGN as an escape character for commas, due to their role as delimiters in CSVs (Unicode: U+002C, ASCII: 44, hex: 0x2C)
            {"¦", "\n"}, //New Line, the Carriage Return of which is later removed (Unicode: (U+000D, U+000A), ASCII: (13, 10), hex: (0x0D, 0x0A) (two values - one for Line Feed, the other for Carriage Return))
            {"\r", ""}, // Carriage Return (Unicode: U+000D, ASCII: 13, hex: 0x0d)
            {"’", "'"}, // Right single quotation marks (Unicode: U+2019, ASCII: N/A, hex: 0xE2 0x80 0x99 (e28099))
            {"…", "..."}, // Horizontal ellipsis (Unicode: U+2026, ASCII: N/A, hex: 0xE2 0x80 0xA6 (e280a6))
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public TextCleaner(String sourceText){
        this.sourceText = sourceText;
        this.cleanedText = cleanText(sourceText);
    }

    private String cleanText(String sourceText){
        String cleanText = sourceText;

        if (sourceText.contains("\"")) cleanText = fixDoubleQuotes(sourceText);

        for (Map.Entry<String, String> e : substitutions.entrySet()) {
            if (cleanText.contains(e.getKey())) cleanText = cleanText.replaceAll(e.getKey(), e.getValue());
        }

        return utf8Encoder(cleanText);

    }

    private String fixDoubleQuotes(String sourceText) {
        String newText = sourceText;
        StringBuilder sb = new StringBuilder(sourceText);
        if (sourceText.contains("\"")) {
            if (sourceText.charAt(0) ==  '\"' && sourceText.charAt(sourceText.length()-1) ==  '\"') {
                newText = sb.deleteCharAt(0).deleteCharAt(sourceText.length()-1).toString();
                newText = newText.replaceAll("\"\"", "\"");
            }
        }
        return newText;
    }

    private String utf8Encoder(String rawString){
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(rawString);
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getCleanedText() {
        return cleanedText;
    }
}

