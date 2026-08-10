public class StaticTest {
    
    static class Example{
        public static final int INITIAL_COUNT = 10;
        private static int count = 0;
        private int instanceId;

        Example(){
            count++;
            instanceId = count + INITIAL_COUNT;
        }

        public static int getLastInstanceId(){
            return count + INITIAL_COUNT;
        }

        public static int getCount(){
            return count;
        }

        public int getInstanceId(){
            return instanceId;
        }
    }

    public static void main(String[] args) {
        Example e1 = new Example();
        Example e2 = new Example();
        Example e3 = new Example();

        System.out.println("총 생성된 객체 수: " + Example.getCount());
        System.out.println("마지막 생성된 instanceId: " + Example.getLastInstanceId());

        System.out.println("e1의 instanceId: " + e1.getInstanceId());
        System.out.println("e2의 instanceId: " + e2.getInstanceId());
        System.out.println("e3의 instanceId: " + e3.getInstanceId());
    }
}
