public class SynchronizedExample {
    public static void main(String[] args) {
        BankAccount account = new BankAccount();

        Thread t1 = new Thread(() -> account.withdraw(800), "스레드-1");
        Thread t2 = new Thread(() -> account.withdraw(800), "스레드-2");

        t1.start();
        t2.start();
    }
}

class BankAccount {
    private int balance = 1000;

    public synchronized void withdraw(int amount){
        if(balance >= amount){
            System.out.println(Thread.currentThread().getName() + "출금시도: " + amount);
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            balance -= amount;
            System.out.println(Thread.currentThread().getName() + "출금 완료. 남은 잔액: " + balance);
        } else {
            System.out.println(Thread.currentThread().getName() + "출금 실패. 잔액 부족");
        }
    }
}
