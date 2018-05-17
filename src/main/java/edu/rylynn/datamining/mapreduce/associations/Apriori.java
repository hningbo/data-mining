package edu.rylynn.datamining.mapreduce.associations;

import edu.rylynn.datamining.mapreduce.associations.common.ItemSet;
import edu.rylynn.datamining.mapreduce.associations.common.ItemSetUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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
import java.util.ArrayList;
import java.util.List;

//Only support the input format of int
public class Apriori {


    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        run(true);
    }

    public static void run(boolean run) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        conf.setInt("minSupport", 2);
        conf.setDouble("minConfidence", 0.8);
        Job job = new Job(conf);
        job.setJarByClass(Apriori.class);
        job.setMapperClass(AprioriMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setReducerClass(AprioriReducer.class);

        FileInputFormat.addInputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/input.txt"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/out0"));

        System.out.println(job.waitForCompletion(true));

        Path path = new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/out0/part-r-00000");
        FileSystem fs = path.getFileSystem(conf);
        InputStream in;
        in = fs.open(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        int i = 1;
        while (line != null) {
            Configuration conf1 = new Configuration();
            conf1.setInt("n", i++);
            Job job1 = new Job(conf1);
            job1.setJarByClass(Apriori.class);
            job1.setMapperClass(AprioriNMapper.class);
            job1.setMapOutputKeyClass(Text.class);
            job1.setMapOutputValueClass(LongWritable.class);

            job1.setReducerClass(AprioriNReducer.class);

            FileInputFormat.addInputPath(job1, new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/input.txt"));
            FileOutputFormat.setOutputPath(job1, new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/out"+Integer.toString(i-1)));

            System.out.println(job1.waitForCompletion(true));
        }
    }

    public static class AprioriMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String trasaction = value.toString();
            String[] items = trasaction.split(",");
            for (String item : items) {
                context.write(new Text(item), new LongWritable(1));
            }
        }
    }

    public static class AprioriReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private int minSupport;

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long sum = 0;
            minSupport = context.getConfiguration().getInt("minSupport", 2);

            for (LongWritable value : values) {
                sum += value.get();
            }

            if (sum >= minSupport) {
                System.out.println(key);
                context.write(key, new LongWritable(sum));
            } else {
                System.out.println(key.toString() + " is not a frequent itemset...");
            }
        }
    }

    public static class AprioriNMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
        private List<ItemSet> lastFrequentItemSet = new ArrayList<>();
        private List<ItemSet> candicateItemSet = new ArrayList<>();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = new Configuration();
            int n = conf.getInt("n", 0);
            Path path = new Path("hdfs://localhost:9000/user/rylynn/apriori-mr/out"+Integer.toString(n-1)+"/part-r-00000");
            FileSystem fs = path.getFileSystem(conf);
            InputStream in;
            in = fs.open(path);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            while (line != null) {
                String[] frequentItemSet = line.split("\t");
                String[] items = frequentItemSet[0].split(",");
                int[] itemset = new int[items.length];
                for (int i = 0; i < items.length; i++) {
                    itemset[i] = Integer.parseInt(items[i]);
                }
                lastFrequentItemSet.add(new ItemSet(itemset.length, itemset));
                line = br.readLine();
            }

            for (int i = 0; i < lastFrequentItemSet.size(); i++) {
                for (int j = i + 1; j < lastFrequentItemSet.size(); j++) {
                    ItemSet superSet = ItemSetUtil.generateSuperSet(lastFrequentItemSet.get(i), lastFrequentItemSet.get(j));
                    if (superSet != null) {
                        candicateItemSet.add(superSet);
                    }
                }
            }

        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] items = value.toString().split(",");
            int[] trasaction = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                trasaction[i] = Integer.parseInt(items[i]);
            }
            for (ItemSet c : candicateItemSet) {
                int[] cItem = c.getItem();
                for (int i : cItem) {
                    int flag = 0;
                    for (int j : trasaction) {
                        if (i == j) {
                            flag = 1;
                        }
                    }
                    if (flag == 0) {
                        return;
                    }
                }
                System.out.println(c.toString());
                context.write(new Text(c.toString()), new LongWritable(1));
            }
        }
    }

    public static class AprioriNReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private int minSupport;

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            String itemset = key.toString();
            minSupport = context.getConfiguration().getInt("minSupport", 2);
            long sum = 0L;
            for (LongWritable value : values) {
                sum += value.get();
            }

            if (sum >= minSupport) {
                System.out.println(key);
                context.write(key, new LongWritable(sum));
            }
        }
    }
}