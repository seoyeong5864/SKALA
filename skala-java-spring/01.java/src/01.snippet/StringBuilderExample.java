public class StringBuilderExample {
    public static void main(String[] args) {
        String name = "스칼라";
        int age = 25;

        StringBuilder sb1 = new StringBuilder();
        sb1.append("이름: ").append(name)
        .append(", 나이: ").append(age);

        System.out.println(sb1.toString());

        StringBuilder sb2 = new StringBuilder();
        sb2.append(String.format("%-10s", "스칼라"))
        .append(String.format("%5d", 25));

        System.out.println(sb2.toString());

    }
}
