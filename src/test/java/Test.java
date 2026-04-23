import com.artur114.bytecodegrab.util.StringUtils;

public class Test {
    public static void main(String[] args) {
        String k = StringUtils.class.getName();
        String name = k.substring(k.lastIndexOf("."));



        System.out.println(name);
    }
}
