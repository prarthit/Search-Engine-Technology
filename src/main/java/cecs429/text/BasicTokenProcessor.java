package cecs429.text;

import java.util.Arrays;
import java.util.List;

public class BasicTokenProcessor implements TokenProcessor {
    @Override
    public List<String> processToken(String token) {
        return Arrays.asList(token.replaceAll("\\W", "").toLowerCase());
    }

    @Override
    public String processQuery(String query) {
        return query.replaceAll("\\W", "").toLowerCase();
    }

}
