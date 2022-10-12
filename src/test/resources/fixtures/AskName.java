package fixtures;

import java.util.Scanner;

public class AskName {

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);

        System.out.println("What is your name?");

        final String name = scanner.nextLine();

        System.out.println(String.format("Hello %s.", name));
    }
}
