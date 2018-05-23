package edu.rylynn.datamining.mapreduce.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
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
        subJob.setMapOutputKeyClass(LongWritable.class);

        subJob.setReducerClass(AprioriReducer.class);
        subJob.setMapOutputKeyClass(Text.class);
        subJob.setMapOutputKeyClass(LongWritable.class);

        FileInputFormat.addInputPath(subJob, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/input"));
        FileOutputFormat.setOutputPath(subJob, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/sub_output"));
        System.out.println(subJob.waitForCompletion(true));

        Job job = new Job(conf);
        job.setJarByClass(Son.class);
        job.setMapperClass(AprioriCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputKeyClass(LongWritable.class);

        job.setReducerClass(AprioriCountReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputKeyClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/sub_output"));
        FileOutputFormat.setOutputPath(job, new Path("hdfs://localhost:9000/user/rylynn/apriori-son/final_output"));
        System.out.println(job.waitForCompletion(true));

    }

    public static class AprioriMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
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

        private ItemSet generateSuperSet(ItemSet cn1, ItemSet cn2) {
            int[] cn1Item = cn1.getItem();
            int[] cn2Item = cn2.getItem();
            int len1 = cn1Item.length;
            int len2 = cn2Item.length;
            int[] superSet = new int[len1 + 1];
            if (len1 == len2) {
                int flag = 1;
                for (int i = 0; i < len1 - 1; i++) {
                    if (cn1Item[i] != cn2Item[i]) {
                        return null;
                    } else {
                        superSet[i] = cn1Item[i];
                    }
                }
                if (cn1Item[len1 - 1] != cn2Item[len1 - 1]) {
                    if (cn1Item[len1 - 1] < cn2Item[len1 - 1]) {
                        superSet[len1 - 1] = cn1Item[len1 - 1];
                        superSet[len1] = cn2Item[len1 - 1];
                    } else {
                        superSet[len1 - 1] = cn2Item[len1 - 1];
                        superSet[len1] = cn1Item[len1 - 1];
                    }

                    return new ItemSet(len1 + 1, superSet);
                }
            }
            return null;
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

        private List<ItemSet> generateSubSet(ItemSet itemSet) {
            List<ItemSet> subSets = new ArrayList<>();
            int[] items = itemSet.getItem();
            int superSetLen = items.length;
            for (int i = 0; i < items.length; i++) {
                int k = 0;
                int[] subSetItems = new int[superSetLen - 1];
                for (int j = 0; j < items.length; j++) {
                    if (i != j) {
                        subSetItems[k++] = items[j];
                    }
                }
                subSets.add(new ItemSet(superSetLen - 1, subSetItems));
            }
            return subSets;
        }

        public Set<ItemSet> getFrequentItemSet() {

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
                    System.out.println(item[0]);
                }
            }

            while (lastFrequentSet.size() != 0) {//from C_(n-1) get C_n and the lastFrequentSet is the C_(n-1)
                Set<ItemSet> newFrequentSet = new HashSet<>();

                for (ItemSet cni : lastFrequentSet) {
                    for (ItemSet cnj : lastFrequentSet) {
                        //get the candicate set
                        ItemSet superSet = generateSuperSet(cni, cnj);

                        if (superSet != null) {
                            int support = countItemSet(superSet);
                            if (support >= subMinSupport) {
                                //get the subset of the candicate set
                                //and judge if all the subset of the candicateset is in the frequentItemSet
                                List<ItemSet> subSets = generateSubSet(superSet);
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
            long count = 1L;
            LongWritable value = new LongWritable(count);
            for (ItemSet set : frequentItemSet) {
                context.write(new Text(set.toString()), value);
            }
        }
    }

    public static class AprioriReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        @Override
        protected void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            long count = 0L;
            for (LongWritable value : values) {
                count += value.get();
            }
            context.write(key, new LongWritable(count));
        }
    }

    public static class AprioriCountMapper extends Mapper<Text, LongWritable, Text, LongWritable> {
        @Override
        protected void map(Text key, LongWritable value, Context context) throws IOException, InterruptedException {
            context.write(key, value);
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
