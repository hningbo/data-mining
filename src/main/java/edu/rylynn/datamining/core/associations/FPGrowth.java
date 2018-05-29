package edu.rylynn.datamining.core.associations;

import edu.rylynn.datamining.core.associations.common.ItemSet;

import java.util.*;

public class FPGrowth {
    private double minSupport;
    private double minConfidence;
    private int size;
    private List<TreeNode> itemTable;
    private Map<String, Integer> itemIndex;
    private Map<Integer, Integer> itemCount;
    private List<String[]> transaction;
    private Map<ItemSet, Integer> frequentItemSet;
    private TreeNode fpTree;

    public FPGrowth(double minSupport, double minConfidence, List<String> data) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.frequentItemSet = new HashMap<>();
        this.transaction = new ArrayList<>();
        this.itemTable = new ArrayList<>();
        this.itemIndex = new HashMap<>();
        this.itemCount = new HashMap<>();
        this.size = data.size();
        fpTree = new TreeNode(-1, 0, null);
        for (String line : data) {
            transaction.add(line.split(","));
        }
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a,b,c,d");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        list.add("c,d,e,f");
        new FPGrowth(0.5, 0.7, list).run();


    }

//    public void firstScan() {
//        int index = 1;
//        for (String[] line : transaction) {
//            for (int i = 0; i < line.length; i++) {
//                if (itemIndex.containsKey(line[i])) {
//                    int thisIndex = itemIndex.get(line[i]);
//                    TreeNode node = itemTable.get(thisIndex - 1);
//                    itemCount.put(thisIndex, itemCount.get(thisIndex) + 1);
//                    node.count++;
//                } else {
//                    itemTable.add(new TreeNode(index, 1, null));
//                    itemCount.put(index, 1);
//                    itemIndex.put(line[i], index++);
//                }
//            }
//        }
//        Collections.sort(itemTable, new Comparator<TreeNode>() {
//            public int compare(TreeNode t1, TreeNode t2) {
//                return t2.count - t1.count;
//            }
//        });
//        List<TreeNode> removeNode = new ArrayList<>();
//        for (TreeNode tableNode : itemTable) {
//            if (tableNode.count < this.size * this.minSupport) {
//                removeNode.add(tableNode);
//            }
//        }
//        itemTable.removeAll(removeNode);
//    }

    private List<TreeNode> buildTree(List<List<Integer>> thisTransaction) {
        List<TreeNode> fpTree = new ArrayList<>();
        for(List<Integer> line: thisTransaction){
            for(int i = 0; i<line.size(); i++){
                int flag = 0;
                for(TreeNode node: fpTree){
                    if(node.index == i){
                        flag = 1;
                        node.count++;
                    }
                }
                if(flag == 1){
                    fpTree.add(new TreeNode(i, 1, null));
                }
            }
        }
        Collections.sort(fpTree, new Comparator<TreeNode>() {
            public int compare(TreeNode t1, TreeNode t2) {
                return t2.count - t1.count;
            }
        });
        List<TreeNode> removeNode = new ArrayList<>();
        for (TreeNode tableNode : fpTree) {
            if (tableNode.count < this.size * this.minSupport) {
                removeNode.add(tableNode);
            }
        }
        itemTable.removeAll(removeNode);

        TreeNode node = null;
        for (List<Integer> line : thisTransaction) {
            List<Integer> thisLineIndex = new ArrayList<>(line);
            for (TreeNode tableNode : fpTree) {
                if (thisLineIndex.contains(tableNode.index)) {
                    node = node.addNode(tableNode.index); //avoid the previous node point to the same node as the now node
                    if (node != tableNode.previousNode)
                        node.previousNode = tableNode.previousNode;
                    tableNode.previousNode = node;
                }
            }
        }
        return fpTree;
    }

    public List<FrequentPatternBase> getFrequentPatternBaseFromTree(List<TreeNode> fpTree) {
        List<FrequentPatternBase> frequentPatternBases = new ArrayList<>();
        for (int i = fpTree.size() - 1; i > 0; i--) {
            TreeNode thisNode = fpTree.get(i);
            int nodeIndex = thisNode.index;
            while (thisNode.previousNode != null) {
                int count = thisNode.previousNode.count;
                TreeNode treeTail = thisNode.previousNode.parent;
                List<Integer> fpBaseItems = new ArrayList<>();
                while (treeTail.index != -1) {
                    fpBaseItems.add(treeTail.index);
                    treeTail = treeTail.parent;
                }
                Collections.reverse(fpBaseItems);
                for (FrequentPatternBase patternBase : frequentPatternBases) {
                    if (patternBase.start == nodeIndex) {
                        patternBase.addPath(fpBaseItems, count);
                        break;
                    }
                }
                thisNode = thisNode.previousNode;
            }
        }
        return frequentPatternBases;
    }

    public void run() {
        List<List<Integer>> trasactionList = new ArrayList<>();
        for (String[] line : transaction) {
            List<Integer> trasactionLine = new ArrayList<>();
            for (String aLine : line) {
                trasactionLine.add(itemIndex.get(aLine));
            }
            trasactionList.add(trasactionLine);
        }

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

    public void fpgrowth(List<TreeNode> fpTree, int start) {
        int isSinglePath = 1;
        Map<Integer, Integer> pattern = new HashMap<>();

        TreeNode node = fpTree.get(0).previousNode;
        while (node.childs.size() != 0) {
            if (node.childs.size() == 1) {
                node = node.childs.get(0);
                pattern.put(node.index, node.count);
            } else {
                isSinglePath = 0;
                break;
            }
        }

        if (isSinglePath == 1) {
            int[] patternArray = new int[pattern.size()];
            Object[] keyArray = pattern.keySet().toArray();
            for (int i = 0; i < keyArray.length; i++) {
                patternArray[i] = (int) keyArray[i];
            }
            List<ItemSet> patterns = generateSubSet(new ItemSet(pattern.size(), patternArray));
            for (ItemSet frequentPattern : patterns) {
                int[] items = frequentPattern.getItem();
                int support = pattern.get(items[0]);
                for (int i = 0; i < items.length; i++) {
                    if (support > pattern.get(items[i])) {
                        support = pattern.get(items[i]);
                    }
                }
                frequentItemSet.put(frequentPattern, support);
            }
        } else {
            List<FrequentPatternBase> patternBases = getFrequentPatternBaseFromTree(fpTree);
            for (FrequentPatternBase patternBase : patternBases) {
                List<List<Integer>> newTrascation = new ArrayList<>(patternBase.paths.keySet());
                //TreeNode tree = buildTree(newTrascation);
                //fpgrowth(tree, start);
            }
        }
    }

    private class TreeNode {
        int index;
        int count;
        List<TreeNode> childs;
        TreeNode parent;
        TreeNode previousNode;

        TreeNode(int index, int count, TreeNode parent) {
            this.index = index;
            this.count = count;
            this.childs = new ArrayList<>();
            this.parent = parent;
            this.previousNode = null;
        }

        TreeNode addNode(int index) {
            for (TreeNode child : childs) {
                if (child.index == index) {
                    child.count++;
                    return child;
                }
            }
            TreeNode newNode = new TreeNode(index, 1, this);
            this.childs.add(newNode);
            return newNode;
        }

        @Override
        public String toString() {
            return (index + " : " + count);
        }
    }

    private class FrequentPatternBase {
        int start;
        Map<List<Integer>, Integer> paths;


        FrequentPatternBase(int start) {
            this.start = start;
            this.paths = new HashMap<>();
        }

        void addPath(List<Integer> path, int count) {
            this.paths.put(path, count);
        }

    }
}