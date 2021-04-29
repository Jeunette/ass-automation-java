import java.util.Scanner;

public class ImageData {

    public int[][] box, border;
    public int[] main, fir, ref;

    public ImageData(int[] main, int[] fir, int[] ref,  int[][] box, int[][] border) {
        this.main = main;
        this.fir = fir;
        this.ref = ref;
        this.box = box;
        this.border = border;
    }

    public ImageData(Scanner scanner) {
        this.main = areaReader(scanner.nextLine());
        this.fir = areaReader(scanner.nextLine());
        this.ref = areaReader(scanner.nextLine());
        this.box = x4Reader(scanner.nextLine());
        this.border = x4Reader(scanner.nextLine());
    }

    private int[] areaReader(String str) {
        return new int[] {Integer.parseInt(str.substring(7, 10)), Integer.parseInt(str.substring(12, 15)), Integer.parseInt(str.substring(17, 20))};
    }

    private int[][] x4Reader(String str) {
        int[] x1 = new int[] {Integer.parseInt(str.substring(7, 10)), Integer.parseInt(str.substring(12, 15)), Integer.parseInt(str.substring(17, 20))};
        int[] x2 = new int[] {Integer.parseInt(str.substring(24, 27)), Integer.parseInt(str.substring(29, 32)), Integer.parseInt(str.substring(34, 37))};
        int[] x3 = new int[] {Integer.parseInt(str.substring(41, 44)), Integer.parseInt(str.substring(46, 49)), Integer.parseInt(str.substring(51, 54))};
        int[] x4 = new int[] {Integer.parseInt(str.substring(58, 61)), Integer.parseInt(str.substring(63, 66)), Integer.parseInt(str.substring(68, 71))};
        return new int[][] {x1, x2, x3, x4};
    }


    public String toString() {
        StringBuilder temp = new StringBuilder();
        temp.append("MAIN: ");
        temp.append(String.format("(%03d, %03d, %03d)", main[0], main[1], main[2]));
        temp.append("\n");
        temp.append("FIR-: ");
        temp.append(String.format("(%03d, %03d, %03d)", fir[0], fir[1], fir[2]));
        temp.append("\n");
        temp.append("REF-: ");
        temp.append(String.format("(%03d, %03d, %03d)", ref[0], ref[1], ref[2]));
        temp.append("\n");
        temp.append("BOX-: ");
        temp.append(String.format("(%03d, %03d, %03d)", box[0][0], box[0][1], box[0][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", box[1][0], box[1][1], box[1][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", box[2][0], box[2][1], box[2][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", box[3][0], box[3][1], box[3][2]));
        temp.append("\n");
        temp.append("BORD: ");
        temp.append(String.format("(%03d, %03d, %03d)", border[0][0], border[0][1], border[0][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", border[1][0], border[1][1], border[1][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", border[2][0], border[2][1], border[2][2])).append(", ");
        temp.append(String.format("(%03d, %03d, %03d)", border[3][0], border[3][1], border[3][2]));
        temp.append("\n");
        return temp.toString();
    }

    public void print() { System.out.println(toString()); }

}
