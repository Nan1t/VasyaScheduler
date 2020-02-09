import java.util.Arrays;

public class Test {

    @org.junit.Test
    public void test(){
        int count = 53;
        int onPage = 15;
        int pages = (int)Math.ceil(((float)count / onPage));

        System.out.println(pages);
    }
}