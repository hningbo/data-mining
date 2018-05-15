package edu.rylynn.datamining.mapreduce.associations;

import edu.rylynn.datamining.mapreduce.associations.common.ItemSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;
import java.util.List;

//Only support the input format of int
public class Apriori {
    public static void main(String[] args) throws IOException ,ClassNotFoundException{
        Configuration conf = new Configuration();
        conf.set("mapred.job.tracker", "127.0.0.1:9001");
        Path path = new Path("hdfs://localhost:9000/user/rylynn/1.txt/part-00000");
        FileSystem fs = path.getFileSystem(conf);
        FSDataInputStream fsis = fs.open(path);
        LineReader linereader = new LineReader(fsis, conf);
        Text line = new Text();
        while(linereader.readLine(line)>0){
            System.out.println(line);
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
            minSupport = context.getConfiguration().getInt("minSupport", 5);

            for (LongWritable value : values) {
                sum += value.get();
            }

            if (sum >= minSupport) {
                context.write(key, new LongWritable(sum));
            } else {
                System.out.println(key.toString() + " is not a frequent itemset...");
            }
        }
    }

    public static class AprioriNMapper extends Mapper<Text, LongWritable, Text, LongWritable> {
        private List<ItemSet> lastFrequentItemSet;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);


        }

        @Override
        protected void map(Text key, LongWritable value, Context context) throws IOException, InterruptedException {

        }
    }

    public static class AprioriNReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        private int minSupport;

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            String itemset = key.toString();
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