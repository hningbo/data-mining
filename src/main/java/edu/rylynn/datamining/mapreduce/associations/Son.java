package edu.rylynn.datamining.mapreduce.associations;

import edu.rylynn.datamining.mapreduce.associations.common.ItemSet;
import edu.rylynn.datamining.mapreduce.associations.common.ItemSetUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Son {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Son.run(2, 2);
    }

    public static void run(int minSupport, int nodeNum) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        conf.setInt("subMinSupport", minSupport / nodeNum);
        conf.setInt("minSupport", minSupport);

        Job subJob = new Job(conf);
        subJob.setJarByClass(Son.class);
        subJob.setMapperClass(AprioriMapper.class);
        subJob.setMapOutputKeyClass(Text.class);
        subJob.setMapOutputValueClass(NullWritable.class);

        subJob.setReducerClass(AprioriReducer.class);
        subJob.setOutputKeyClass(Text.class);
        subJob.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(subJob, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/input"));
        FileOutputFormat.setOutputPath(subJob, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/sub_output"));
        System.out.println(subJob.waitForCompletion(true));

        Job job = new Job(conf);
        job.setJarByClass(Son.class);
        job.setMapperClass(AprioriCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setReducerClass(AprioriCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/input"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/final_output"));
        System.out.println(job.waitForCompletion(true));

    }

    public static class AprioriMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        private int subMinSupport;
        private List<String[]> itemData;
        private Set<ItemSet> frequentItemSet;
        private List<String> itemIndex;
        private Map<String, Integer> transaction;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            itemData = new ArrayList<>();
            frequentItemSet = new HashSet<>();
            itemIndex = new ArrayList<>();
            transaction = new HashMap<>();
        }

        private int countItemSet(ItemSet itemSet) {
            int[] items = itemSet.getItem();
            int count = 0;
            for (String[] line : itemData) {
                int flag = 1;
                for (int i = 0; i < items.length; i++) {
                    flag = 0;
                    for (int j = 0; j < line.length; j++) {
                        if (itemIndex.indexOf(line[j]) == items[i]) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        break;
                    }
                }
                if (flag == 1) {
                    count++;
                }
            }
            return count;
        }

        Set<ItemSet> getFrequentItemSet() {
            for (String[] line : itemData) {
                for (int i = 0; i < line.length; i++) {
                    if (transaction.containsKey(line[i])) {
                        transaction.put(line[i], transaction.get(line[i]) + 1);
                    } else {
                        transaction.put(line[i], 1);
                        itemIndex.add(line[i]);
                    }
                }
            }

            Set<ItemSet> lastFrequentSet = new HashSet<>();
            for (Map.Entry<String, Integer> entry : transaction.entrySet()) {
                if (entry.getValue() >= subMinSupport) {
                    int[] item = new int[1];
                    item[0] = itemIndex.indexOf(entry.getKey());
                    lastFrequentSet.add(new ItemSet(1, item));
                    frequentItemSet.add(new ItemSet(1, item));
                }
            }

            while (lastFrequentSet.size() != 0) {//from C_(n-1) get C_n and the lastFrequentSet is the C_(n-1)
                Set<ItemSet> newFrequentSet = new HashSet<>();

                for (ItemSet cni : lastFrequentSet) {
                    for (ItemSet cnj : lastFrequentSet) {
                        //get the candicate set
                        ItemSet superSet = ItemSetUtil.generateSuperSet(cni, cnj);

                        if (superSet != null) {
                            int support = countItemSet(superSet);
                            if (support >= subMinSupport) {
                                //get the subset of the candicate set
                                //and judge if all the subset of the candicateset is in the frequentItemSet
                                List<ItemSet> subSets = ItemSetUtil.generateSubSet(superSet);
                                int flag = 1;
                                for (ItemSet subSet : subSets) {
                                    if (!this.frequentItemSet.contains(subSet)) {
                                        flag = 0;
                                        break;
                                    }
                                }
                                if (flag == 1) {
                                    if (!this.frequentItemSet.contains(superSet)) {
                                        frequentItemSet.add(superSet);
                                    }
                                    newFrequentSet.add(superSet);
                                }
                            }
                        }
                    }
                }
                lastFrequentSet.clear();
                lastFrequentSet = newFrequentSet;
            }
            return frequentItemSet;
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            itemData.add(line.split(","));
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);
            Configuration conf = new Configuration();
            subMinSupport = conf.getInt("subMinSupport", 2);
            Set<ItemSet> frequentItemSet = getFrequentItemSet();
            for (ItemSet set : frequentItemSet) {
                StringBuilder items = new StringBuilder();
                for (int i : set.getItem()) {
                    items.append(itemIndex.get(i)).append(" ");
                }
                context.write(new Text(items.toString()), NullWritable.get());
            }
        }
    }

    public static class AprioriReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {

            context.write(key, NullWritable.get());
        }
    }

    public static class AprioriCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        private List<String[]> candicateItemSets;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            candicateItemSets = new ArrayList<>();
            Configuration conf = new Configuration();
            Path path = new Path("hdfs://localhost:9000/user/rylynn/apriori-son/sub_output" + "/part-r-00000");
            FileSystem fs = path.getFileSystem(conf);
            InputStream in;
            in = fs.open(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            while (line != null) {
                String[] items = line.split(" ");
                candicateItemSets.add(items);
                line = br.readLine();
            }
        }


        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            LongWritable count = new LongWritable(1);
            String[] trasaction = line.split(",");
            Set<String> tSet = new HashSet<String>(Arrays.asList(trasaction));
            for (String[] candicateItemSet : candicateItemSets) {
                Set<String> cSet = new HashSet<String>(Arrays.asList(candicateItemSet));
                if(tSet.containsAll(cSet)){
                    StringBuilder sb = new StringBuilder();
                    for(String i: cSet){
                        sb.append(i).append(" ");
                    }
                    context.write(new Text(sb.toString()), count);
                }
            }

        }
    }

    public static class AprioriCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private int minSupport;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = new Configuration();
            minSupport = conf.getInt("subMinSupport", 2);
        }

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long sum = 0L;
            for (LongWritable value : values) {
                sum += value.get();
            }
            if (sum >= minSupport) {
                context.write(key, new LongWritable(sum));
            }
        }
    }
}

/*
intput:
1,2,3,4
2,3,4,5
2,3,4,5
1,2,3,4
2,3,4,5
4,5,6,2

 */

/*
sub_output:

1
1 2
1 2 3
1 2 3 4
1 2 4
1 3
1 3 4
1 4
2
2 3
2 3 4
2 3 4 5
2 3 5
2 4
2 4 5
2 5
3
3 4
3 4 5
3 5
4
4 5
5
 */

/*
final_output:

1 	2
1 2 	2
1 2 3 	2
1 2 3 4 	2
1 2 4 	2
1 3 	2
1 3 4 	2
1 4 	2
2 	6
2 3 	5
2 3 4 	5
2 3 4 5 	3
2 3 5 	3
2 4 	6
2 4 5 	4
2 5 	4
3 	5
3 4 	5
3 4 5 	3
3 5 	3
4 	6
4 5 	4
5 	4
 */
