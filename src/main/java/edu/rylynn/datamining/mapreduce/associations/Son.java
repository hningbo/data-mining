package edu.rylynn.datamining.mapreduce.associations;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class Son {
    public static class AprioriMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            super.map(key, value, context);
        }
    }

    public static class AprioriReducer extends Reducer<Text, NullWritable ,Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            super.reduce(key, values, context);
        }
    }

    public static class AprioriCountMapper extends Mapper<Text, NullWritable, Text, LongWritable>{
        @Override
        protected void map(Text key, NullWritable value, Context context) throws IOException, InterruptedException {
            super.map(key, value, context);
        }
    }

    public static class AprioriCountReducer extends Reducer<Text, LongWritable, Text , LongWritable>{
        private int minSupport;

        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {

            long sum = 0L;
            for(LongWritable value: values){
                sum += value.get();
            }
            if(sum >= minSupport){
                context.write(key, new LongWritable(sum));
            }
        }
    }
}
