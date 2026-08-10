public class FormatterExample {
    public static void main(String[] args) {
        String name = "스칼라";
        int age = 30;
        String formatted = String.format("이름: %s, 나이: %d", name, age);
        System.out.println(formatted);

        double pi = 3.141592;
        System.out.println(String.format("원주율: %.2f", pi));
        System.out.printf("|%10s|\n", "Java");
        System.out.printf("|%-10s|\n", "Java");

    }
}
