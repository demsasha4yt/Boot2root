import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        File dir = new File("C:\\ft_fun");
        File[] lst = Objects.requireNonNull(dir.listFiles());
        for (File f:lst) {
            try {
                FileReader fr = new FileReader(f);
                BufferedReader reader = new BufferedReader(fr);
                String line = reader.readLine();
                ArrayList<String> arrayList = new ArrayList<>();
                while (line != null) {
                    if (!line.contains("//file")) {
                        arrayList.add(line);
                    }
                    else {
                        line = line.replace("//file", "");
                        Files.write(Paths.get("C:\\ft_res\\" + line + ".txt"), arrayList, StandardOpenOption.CREATE);
                        arrayList.clear();
                    }
                    line = reader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        dir = new File("C:\\ft_res");
        lst = Objects.requireNonNull(dir.listFiles());
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = 1; i <= lst.length; i++) {
            File f;
            for (int j = 1; j <= lst.length; j++) {
                String s = lst[j - 1].getName().replace(".txt","");
                if (Integer.parseInt(s) == i) {
                    try {
                        FileReader fr = new FileReader(lst[j - 1]);
                        BufferedReader reader = new BufferedReader(fr);
                        String line = reader.readLine();
                        while (line != null) {
                            arrayList.add(line);
                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        Files.write(Paths.get("C:\\ft_res\\result.txt"), arrayList, StandardOpenOption.CREATE);
    }
}
