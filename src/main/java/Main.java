import com.opencsv.CSVReader;


import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException, URISyntaxException{


        grouping();
        GroupSizeSort sort = new GroupSizeSort();
        Collections.sort(allGroup, sort);
        writeToFile();

    }

    //Двойной список строк, который повторяет структуру ячеек изначального csv файла
    static ArrayList<String[]> strings;

    static {
        try {
            strings = spl(oneByOne());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Список со всеми группами
    static ArrayList<ArrayList<String[]>> allGroup = new ArrayList<ArrayList<String[]>>(13000);
    //Номер группы, которая сейчас будет собрана (изначально 1)
    static int groupNumber = 1;
    //Количество групп с более чем одни элементом
    static int count = 0;

    //Запись получившихся групп в файл
    private static void writeToFile() throws IOException, URISyntaxException {

        BufferedWriter writer = Files.newBufferedWriter(
                Path.of(ClassLoader.getSystemResource("txt/test.txt").toURI()));

        writer.write("Количество групп с более чем одним элементом: " + count + "\n");
        for (int i = 0; i < allGroup.size(); i++) {
            for (int j = 0; j < allGroup.get(i).size(); j++) {
                writer.write(allGroup.get(i).get(j)[0] + " " + allGroup.get(i).get(j)[1] + " " + allGroup.get(i).get(j)[2] + " " + "\n");
            }
        }

        writer.close();
    }

    //Чтение данного csv файла (метод читаем по строкам, чтобы сразу проверять является ли строка валидной)
    public static List<String[]> oneByOne() throws Exception {
        Reader reader = Files.newBufferedReader(
                Path.of(ClassLoader.getSystemResource("csv/lng.csv").toURI()));
        List<String[]> list = new ArrayList<>();
        Map<String, String[]> hashMap = new HashMap<String, String[]>();
        CSVReader csvReader = new CSVReader(reader);
        String[] line;
        //HashMap позволяет избавиться от дублирующихся строк без лишнего перебора элементов
        // (Set не справляется с этой задачей, так как каждая строка это отдельный инстанс объекта String, из-за чего Set сохраняет их все)
        while ((line = csvReader.readNext()) != null) {
            if (!line[0].contains("\"")) {
                hashMap.put(line[0], line);
            }
        }
        list.addAll(hashMap.values());
        reader.close();
        csvReader.close();
        return list;
    }


    //Разделение по ;
    private static ArrayList<String[]> spl(List<String[]> strings) {
        //Задаем сразу примерное capacity чтобы избавиться от лишних операций по расширению списка
        ArrayList<String[]> str = new ArrayList<>(900000);
        for (int i = 0; i < strings.size(); i++) {
            String[] s = strings.get(i)[0].split(";");
            //Заменяем у пустых ячеек значение null на "" (null появляется именно тогда, когда пустыми являются последние столбцы)
            if (s.length != 0) {
                if (s.length == 3) {
                    str.add(s);
                }
                if (s.length == 2) {
                    String[] st = new String[3];
                    st[0] = s[0];
                    st[1] = s[1];
                    st[2] = "";
                    str.add(st);
                }
                if (s.length == 1) {
                    String[] st = new String[3];
                    st[0] = s[0];
                    st[1] = "";
                    st[2] = "";
                    str.add(st);
                }
            }
        }
        return str;
    }

    //Группировка данных по совпадению в столбцах и запись группы в общих список
    private static void grouping() {
        //Первая строка, по которой будем искать совпадения
        String[] string = strings.get(0);
        //Сразу удаляем строку из общего списка
        strings.remove(0);
        //Список совпадающих строк
        ArrayList indexes;
        while (string != null) {
            ArrayList<String[]> firstGroup = new ArrayList<String[]>();
            firstGroup.add(string);
            for (int i = 0; i < firstGroup.size(); i++) {
                indexes = find(firstGroup.get(i));
                if (indexes != null){
                    firstGroup.addAll(indexes);
                }
            }
            firstGroup.add(0, new String[]{"Группа " + groupNumber, "", ""});
            groupNumber++;
            allGroup.add(firstGroup);
            if (firstGroup.size() > 2) {
                count++;
            }
            if (strings.size() > 0) {
                string = strings.get(0);
                strings.remove(0);
            } else {
                string = null;
            }
        }
    }

    //Поиск совпадение по столбцам (пустые столбцы исключаются)
    private static ArrayList<String[]> find(String[] string) {
        ArrayList<String[]> indexes = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            if (((string[0].equals(strings.get(i)[0]) && !string[0].equals("")) ||
                    (string[1].equals(strings.get(i)[1]) && !string[1].equals("")) ||
                    (string[2].equals(strings.get(i)[2]) && !string[2].equals(""))
            )) {
                indexes.add(strings.get(i));
                strings.remove(i);
            }
        }
        if (indexes.size() != 0) {
            return indexes;
        }
        return null;
    }


    //Вложенный класс, для создание условия сортировки списка всех групп
    private static class GroupSizeSort implements Comparator<ArrayList<String[]>> {
        @Override
        public int compare(ArrayList<String[]> o1, ArrayList<String[]> o2) {
            return o2.size() - o1.size();
        }
    }

}
