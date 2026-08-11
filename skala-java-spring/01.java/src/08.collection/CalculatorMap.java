import java.util.*;

public class CalculatorMap {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // List<String> history = new ArrayList<>();
        Map<Integer, String> historyMap = new HashMap<>();

        boolean isRun = true;
        while(isRun) {
            // 첫 번째 숫자 입력
            System.out.print("첫 번째 숫자: ");
            int firstNumber = scanner.nextInt();

            // 연산자 입력
            System.out.print("연산자(+ - * /): ");
            String operator = scanner.next().trim();

            // 두 번째 숫자 입력
            System.out.print("두 번째 숫자: ");
            int secondNumber = scanner.nextInt();

            // 0으로 나누기 예외 처리
            try {
                if (operator.equals("/") && secondNumber == 0) {
                    throw new ArithmeticException("0으로 나눌 수 없습니다.");
                }
            } catch (ArithmeticException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                continue;
            }

            // switch문으로 계산 (잘못된 연산자는 default에서 처리)
            double result;
            switch (operator) {
                case "+" -> result = firstNumber + secondNumber;
                case "-" -> result = firstNumber - secondNumber;
                case "*" -> result = firstNumber * secondNumber;
                case "/" -> result = (double) firstNumber / secondNumber;
                default -> {
                    System.out.println("잘못된 연산자입니다.");
                    continue;
                }
            }

            System.out.println("결과: " + result);
            
            String record = firstNumber + " " + operator + " " + secondNumber + " = " + result;

            // history.add(record);
            historyMap.put(record.hashCode(), record);

            // 계속 여부 입력
            System.out.print("계속하려면 c(continue) / 종료하려면 q(quit) 입력: ");
            String choice = scanner.next().trim().toLowerCase();

            if (choice.equals("q") || choice.equals("quit")) {
                isRun = false;
            }
        }

        // Iterator<String> iterator = history.iterator();
        System.out.println("\n===계산 기록===");
        // while(iterator.hasNext()){
        //     System.out.println(iterator.next());
        // }
        // Map의 entrySet()을 이용하여 기록 출력
        for(Map.Entry<Integer, String> entry : historyMap.entrySet()){
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        scanner.close();
    }
}
