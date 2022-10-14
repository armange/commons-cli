package fixtures;

public class PrintHelloWithDelay {

    public static void main(String[] args) throws Exception {
        Thread.sleep(60000);
        System.out.println(String.format("Hello %s.", args[0]));
    }
}
