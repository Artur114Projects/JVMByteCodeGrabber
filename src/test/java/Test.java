import com.artur114.bytecodegrab.util.StringUtils;

public class Test {
    public static void main(String[] args) {
        String m = "AAAAAAAA SIZE:  [ "+ 2147483647 +" ]";



        System.out.println(StringUtils.intPropFromMessage(m, "SIZE"));
    }
}
