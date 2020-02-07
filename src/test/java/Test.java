import ru.nanit.vasyascheduler.api.util.HashUtil;

import java.net.URL;
import java.util.Arrays;

public class Test {

    @org.junit.Test
    public void test(){
        String str = "сабанов";
        System.err.println(lastNameToUppercase(str));
    }

    private String lastNameToUppercase(String str){
        char[] arr = str.toCharArray();
        char firstChar = Character.toUpperCase(arr[0]);
        return firstChar + String.valueOf(Arrays.copyOfRange(arr, 1, str.length()));
    }
}