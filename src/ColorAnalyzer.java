import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ColorAnalyzer {

    public static int[] rgbDifference(int[] c1, int[] c2) { return new int[] {c2[0] - c1[0], c2[1] - c1[1], c2[2] - c1[2]}; }

    private static final int MAIN_RATE = 4;
    private static final int FIR_RATE = 1;
    private static final int REF_RATE = 1;

    BufferedImage current;

    public ImageData analyse(BufferedImage image) {
        this.current = image;
        int[] main = areaColor(this.boxPos, MAIN_RATE);
        int[] fir = areaColor(this.firPos, FIR_RATE);
        int[] ref = areaColor(this.refPos, REF_RATE);
        int[][] box = x4Color(this.boxPos);
        int[][] border = x4Color(this.borderPos);
        return new ImageData(main, fir, ref, box, border);
    }

    private int[] getRGB(int[] pos) {
        int temp = this.current.getRGB(pos[0], pos[1]);
        return new int[] {(temp & 0xff0000) >> 16, (temp & 0xff00) >> 8, temp & 0xff};
    }

    private int[] areaColor(int[][] pos, int rate) {
        long r = 0, g = 0,b = 0;
        int count = 0;
        for(int i = pos[0][0]; i < pos[3][0]; i += rate) {
            for (int j = pos[0][1]; j < pos[3][1]; j += rate) {
                int temp = this.current.getRGB(i, j);
                r += (temp & 0xff0000) >> 16;
                g += (temp & 0xff00) >> 8;
                b += temp & 0xff;
                count++;
            }
        }
        r /= count;
        g /= count;
        b /= count;
        return new int[] {(int) r, (int) g, (int) b};
    }

    private int[][] x4Color(int[][] pos) { return new int[][] {getRGB(pos[0]), getRGB(pos[1]), getRGB(pos[2]), getRGB(pos[3])}; }

    int[][] firPos, refPos, boxPos, borderPos;

    public ColorAnalyzer() throws FileNotFoundException {
        this.firPos = catReaderMX(SettingsHandler.refReader(SettingsHandler.REF_CAT_FIR_MX));
        this.refPos = catReaderMX(SettingsHandler.refReader(SettingsHandler.REF_CAT_REF_MX));
        this.boxPos = catReaderMX(SettingsHandler.refReader(SettingsHandler.REF_CAT_BOX_MX));
        this.borderPos = catReaderMX(SettingsHandler.refReader(SettingsHandler.REF_CAT_BORDER_MX));
    }

    private int[][] catReaderMX(String reference) {
        Scanner scanner = new Scanner(reference);
        int x1 = scanner.nextInt();
        int x2 = scanner.nextInt();
        int y1 = scanner.nextInt();
        int y2 = scanner.nextInt();
        scanner.close();
        return new int[][] {{x1, y1}, {x2, y1}, {x1, y2}, {x2, y2}};
    }

    private int[][] catReaderPOS(String reference) {
        Scanner scanner = new Scanner(reference);
        int[] x1 = {scanner.nextInt(), scanner.nextInt()};
        int[] x2 = {scanner.nextInt(), scanner.nextInt()};
        int[] x3 = {scanner.nextInt(), scanner.nextInt()};
        int[] x4 = {scanner.nextInt(), scanner.nextInt()};
        scanner.close();
        return new int[][] {x1, x2, x3, x4};
    }

}