package edu.rylynn.datamining;

import edu.rylynn.datamining.core.associations.Apriori;
import edu.rylynn.datamining.core.associations.Eclat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        Map<Integer, String> attributeIndex = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("./第四次实验.csv"));
        List<String> data = new ArrayList<>();
        int i = 0;

        String line = br.readLine();
        while (line != null) {
            String[] items = line.split(",");
            if (i == 0) {
                for (String item : items) {
                    attributeIndex.put(i++, item);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < items.length; j++) {
                    if (!items[j].equals("?")) {
                        if (j != items.length - 1) {

                            sb.append(attributeIndex.get(j)).append(",");
                        } else {
                            sb.append(attributeIndex.get(j));
                        }
                    }
                }
                data.add(sb.toString());
                //System.out.println(sb.toString());
            }
            line = br.readLine();
        }
        long start = System.currentTimeMillis();
        //new Eclat(0.3, 0.9, data).generateRules();
        long end = System.currentTimeMillis();
        System.out.println(end - start);

    }
}
