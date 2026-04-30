import com.artur114.bytecodegrab.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String k = StringUtils.class.getName();
        String[] name = k.split("\\.");
        System.out.println(formatPack(Arrays.asList(name)));
    }

    private static String formatPack(List<String> pack) {
        StringBuilder builder = new StringBuilder();
        for (String p : pack) {
            builder.append(p).append('.');
        }
        return builder.substring(0, builder.lastIndexOf("."));
    }

}
