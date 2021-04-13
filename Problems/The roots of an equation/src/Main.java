import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        int c = scanner.nextInt();
        int d = scanner.nextInt();

//        int x1 = Integer.MIN_VALUE;
//        int x2 = Integer.MIN_VALUE;
//        int x3 = Integer.MIN_VALUE;

        List<Integer> results = new ArrayList<>(3);

        for (int i = 0; i <= 1000; i++) {
            if (isEqual(a, b, c, d, i)) {
                results.add(i);
//                if (x1 == Integer.MIN_VALUE) {
//                    x1 = i;
//                } else if (x2 == Integer.MIN_VALUE) {
//                    x2 = i;
//                } else {
//                    x3 = i;
//                }
            }
        }

        results.forEach(System.out::println);

//        printRoot(x1);
//        printRoot(x2);
//        printRoot(x3);

    }

//    private static void printRoot(int x) {
//        if (x != Integer.MIN_VALUE) {
//            System.out.println(x);
//        }
//
//    }

    private static boolean isEqual(int a, int b, int c, int d, int x) {
        return a * (x * x * x) + b * (x * x) + c * x + d == 0;
    }
}