package com.softwareEngineeringLab1;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.softwareEngineeringLab1.structure.DirectedMap;
import org.jgrapht.Graph;
import com.softwareEngineeringLab1.structure.Pair;
import org.jgrapht.ext.JGraphXAdapter;
import com.mxgraph.util.mxCellRenderer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import com.mxgraph.view.mxGraph;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


class RandomTravel {
    private static volatile boolean running = true;

    private static char key;

    public class MyEdge {
        public Integer startPoint;
        public Integer endPoint;

        public MyEdge(Integer startPoint, Integer endPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyEdge myClass = (MyEdge) o;
            return Objects.equals(startPoint, myClass.startPoint) && Objects.equals(endPoint, myClass.endPoint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startPoint, endPoint);
        }
    }


    public String startTravel(DirectedMap directedMap)  {
        // 启动一个线程来读取命令行输入
        Thread inputThread = new Thread(new InputReader());
        inputThread.setDaemon(true); // 将输入线程设置为守护线程，这样程序可以在主线程结束后自动退出
        inputThread.start();
        String res = "";
        // 主线程可以继续执行其他任务
        System.out.println("Main thread is running. Type 'x' to quit.");
        while (running) {
            // 执行其他任务
            res =  travel(directedMap);
        }
        return res;
    }

    public String travel(DirectedMap directedMap) {
        String travelPath = "";
        HashSet<MyEdge> visitedEdge = new HashSet<>();
        Map<String, String> exchangedDict = directedMap.getExchangeDict();
        int nodeNum = directedMap.nodeNumber;
        long seed = System.currentTimeMillis();

        //随机起始点
        int startNumber = getRandomNumberTimeSeed(nodeNum,seed);

        while(running){
            seed +=  1;
            String tmp = exchangedDict.get(Integer.toString(startNumber));
            //加入起始点
            travelPath +=  tmp+ ' ';
            System.out.println(tmp);
            HashSet<Integer> nextPointList = directedMap.map.get(startNumber);

            //如果没有出边
            if (nextPointList.size() == 0) {
                //停止travel
                running = false;
                break;
            }

            //开始随机遍历
            Integer nextPointIdx = getRandomNumberTimeSeed(nextPointList.size(),seed);
            Integer nextPoint = 0;
            for (Integer value : nextPointList) {
                if(nextPointIdx == 0){
                    nextPoint = value;
                    break;
                }
                nextPointIdx--;
            }
            MyEdge myEdge = new MyEdge(startNumber, nextPoint);

            //添加到访问边
            if (!visitedEdge.add(myEdge)) {
                //停止travel
                running = false;
                break;
            }
            startNumber = nextPoint;
            try {
                // 暂停当前线程 1 秒
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // 处理可能的 InterruptedException 异常
                e.printStackTrace();
            }
        }
        return travelPath;
    }

    public static Integer getRandomNumberTimeSeed(Integer nodeNum,long seed) {
        // 使用当前时间的毫秒数作为种子
        Random random = new Random(seed);
        return random.nextInt(nodeNum);
    }

    // 输入读取器线程的实现
    public static class InputReader implements Runnable {
        @Override
        public void run() {
            try {
                while (running) {
                    int c = System.in.read();
                    if (c != -1) {
                        char ch = (char) c;
                        key = ch;
                        if (ch == 'x' || ch == 'X') { // 输入 'x' 时退出
                            running = false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class DirectedGraphExample extends JFrame {
    private static final long serialVersionUID = 1L;

    private JGraphXAdapter<String, DefaultEdge> graphAdapter;

    public JGraphXAdapter<String, DefaultEdge> getGraphAdapter() {
        return graphAdapter;
    }

    public void createDirectedGraph(DirectedMap directedMap) {
        // 创建有向图
        Graph<String, DefaultEdge> graph = new DirectedMultigraph<>(DefaultEdge.class);

        // 添加顶点
        for (String word :
                directedMap.wordSet) {
            graph.addVertex(word);
        }

        Map<String, String> exhcangedDict = directedMap.getExchangeDict();
        // 添加边
        for (int i = 0; i < directedMap.map.size(); i++) {
            String point1 = exhcangedDict.get(Integer.toString(i));
            //对于 startPoint 所连的点
            for (Integer idx :
                    directedMap.map.get(i)) {
                String point2 = exhcangedDict.get(Integer.toString(idx));
                graph.addEdge(point1, point2);
            }
        }

        // 使用JGraphX适配器
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(graph);

        // 设置布局
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // 创建图形组件并添加到JFrame
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        getContentPane().add(graphComponent);

        this.graphAdapter = graphAdapter;
    }

    public static void saveGraphAsImage(mxGraph graph, String filePath) {
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);
        File imgFile = new File(filePath);
        try {
            ImageIO.write(image, "PNG", imgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DirectedMapUtils {
    public static String minPath(DirectedMap directedMap, String sourceWord, String targetWord) {
        int nodeNumber = directedMap.nodeNumber;
        int INF = Integer.MAX_VALUE; // 使用一个非常大的数表示无穷大

        // 初始化邻接矩阵
        ArrayList<ArrayList<Integer>> graph = new ArrayList<>(nodeNumber);
        for (int i = 0; i < nodeNumber; i++) {
            ArrayList<Integer> row = new ArrayList<>(Arrays.asList(new Integer[nodeNumber]));
            for (int j = 0; j < nodeNumber; j++) {
                row.set(j, (i == j) ? 0 : INF); // 自己到自己距离为0，其他初始化为INF
            }
            graph.add(row);
        }
        //初始化边
        for (int i = 0; i < directedMap.edge.size(); i++) {
            ArrayList<Pair> edges = directedMap.edge.get(i);
            for (Pair edge :
                    edges) {
                graph.get(i).set(edge.idx, edge.weight);
            }
        }

        // 源点
        int source = directedMap.dict.get(sourceWord);
        int target = directedMap.dict.get(targetWord);

        // 运行Dijkstra算法
        Result result = dijkstra(graph, source, nodeNumber);

        if(result.distances[target] == INF){
            return null;
        }
        // 打印最短路径结果
//        System.out.println("source\tVertex\tDistance from Source\tPath");
//        System.out.print(source + "\t\t" + target + "\t\t" + (result.distances[target] == INF ? "INF" : result.distances[target]) + "\t\t");

        return printPath(result.prev, target,directedMap);
    }

    private static class Result {
        int[] distances;
        int[] prev;

        public Result(int[] distances, int[] prev) {
            this.distances = distances;
            this.prev = prev;
        }
    }

    private static Result dijkstra(ArrayList<ArrayList<Integer>> graph, int source, int nodeNumber) {
        int[] dist = new int[nodeNumber];
        int[] prev = new int[nodeNumber];
        boolean[] visited = new boolean[nodeNumber];
        PriorityQueue<Pair> pq = new PriorityQueue<>((a, b) -> a.weight - b.weight);

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1); // -1表示前驱节点不存在
        dist[source] = 0;
        pq.add(new Pair(source, 0));

        while (!pq.isEmpty()) {
            Pair current = pq.poll();
            int u = current.idx;

            if (visited[u]) continue;
            visited[u] = true;

            for (int v = 0; v < nodeNumber; v++) {
                if (!visited[v] && graph.get(u).get(v) != Integer.MAX_VALUE) {
                    int newDist = dist[u] + graph.get(u).get(v);
                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        prev[v] = u;
                        pq.add(new Pair(v, newDist));
                    }
                }
            }
        }

        return new Result(dist, prev);
    }


    private static String printPath(int[] prev, int target,DirectedMap directedMap) {
        String res = "";
        if (prev[target] == -1) {
            res += directedMap.getExchangeDict().get(Integer.toString(target));
            return res;
        }
        res = printPath(prev, prev[target],directedMap);
        res +=" -> " + directedMap.getExchangeDict().get(Integer.toString(target));
        return res;
    }

    //根据bridge生成文本
    public static String genTxtBasedOnBridge(DirectedMap directedMap, String filePath) throws FileNotFoundException {
        String res = "";
        String preWord = "";
        try (Scanner sc = new Scanner(new FileReader(filePath))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] words = line.split(",| ");
                for (String word :
                        words) {
                    if (preWord.length() != 0) {
                        String[] bridgeWords = DirectedMapUtils.selectBridgeWords(directedMap, preWord, word).toArray(new String[0]);
                        if (bridgeWords.length != 0) {
                            // 创建一个 Random 实例
                            Random random = new Random();
                            // 生成一个随机索引
                            int randomIndex = random.nextInt(bridgeWords.length);
                            res += bridgeWords[randomIndex] + " ";
                        }
                    }
                    preWord = word;
                    res += preWord + " ";
                }
            }
        }
        return res.substring(0, res.length() - 1);
    }

    //桥接功能
    public static ArrayList<String> selectBridgeWords(DirectedMap directedMap, String startWord, String endWord) {
        Map<String, Integer> dict = directedMap.dict;

        if (!directedMap.wordSet.contains(startWord) || !directedMap.wordSet.contains(endWord)) {
            return new ArrayList<String>();
        }

        HashSet<Integer> startIdxList = directedMap.map.get(dict.get(startWord));
        Integer endIdx = dict.get(endWord);
        ArrayList<String> res = new ArrayList<>();

        //遍历每一个词
        for (String word :
                directedMap.wordSet) {
            Integer midIdx = dict.get(word);
            HashSet<Integer> endIdxList = directedMap.map.get(dict.get(word));//指向的目标

            // 进行桥接词判断
            if (startIdxList.contains(midIdx) && endIdxList.contains(endIdx)) {
                res.add(word);
            }
        }

        return res;
    }

    public static DirectedMap analyseInputFile(String filePath) throws IOException {
        DirectedMap directedMap = new DirectedMap();

        //初始化
        String preWord = "";
        try (Scanner sc = new Scanner(new FileReader(filePath))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] words = line.split(",| |\\.");

                for (String word : words) {
                    if (preWord.length() != 0) {
                        //如果word第一次出现，进行初始化
                        if (directedMap.wordSet.add(word)) {
                            directedMap.dict.put(word, directedMap.nodeNumber);
                            directedMap.map.add(new HashSet<>());
                            directedMap.edge.add(new ArrayList<>());
                            directedMap.nodeNumber++;
                        }

                        //添加preWord -> word的边
                        Integer preWordIdx = directedMap.dict.get(preWord);
                        Integer nowWordIdx = directedMap.dict.get(word);

                        ArrayList<Pair> preWordPointToEdge = directedMap.edge.get(preWordIdx);
                        HashSet<Integer> preWordPointToPoint = directedMap.map.get(preWordIdx);

                        //如果之前不存在指向关系
                        if (preWordPointToPoint.add(nowWordIdx)) {
                            preWordPointToEdge.add(new Pair(nowWordIdx));
                        } else {
                            // 寻找到已经存在的边，进行++
                            for (Pair edge :
                                    preWordPointToEdge) {
                                if (Objects.equals(edge.getIdx(), nowWordIdx)) {
                                    edge.increaseWeight();
                                }
                            }
                        }
                    } else {
                        preWord = word;
                        directedMap.dict.put(word, directedMap.nodeNumber);
                        directedMap.wordSet.add(word);
                        directedMap.map.add(new HashSet<>());
                        directedMap.edge.add(new ArrayList<>());
                        directedMap.nodeNumber++;
                    }
                    preWord = word;
                }
            }
        }
        Map<String, String> exchangeMap = directedMap.dict.entrySet().stream().collect(Collectors.toMap(o -> String.valueOf(o.getValue()), o -> o.getKey()));
        directedMap.setExchangeDict(exchangeMap);
        return directedMap;
    }

    public static void showMapCmd(DirectedMap directedMap) {
        Map<String, String> exchangeMap = directedMap.getExchangeDict();
        for (int i = 0; i < directedMap.map.size(); i++) {
            if (directedMap.map.get(i).size() == 0) {
                continue;
            }
            System.out.print(exchangeMap.get(Integer.toString(i)) + ' ');
            for (Integer idx :
                    directedMap.map.get(i)) {
                System.out.print(exchangeMap.get(Integer.toString(idx)) + ' ');
            }
            System.out.println();
        }
    }

    public static void showDirectedGraph(DirectedMap directedMap) {
        // 创建并显示 DirectedGraphExample 窗口
        DirectedGraphExample frame = new DirectedGraphExample();
        frame.createDirectedGraph(directedMap);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void saveDirectedGraph(DirectedMap directedMap, String savePath) {
        DirectedGraphExample frame = new DirectedGraphExample();
        frame.createDirectedGraph(directedMap);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.saveGraphAsImage(frame.getGraphAdapter(), savePath);
    }
}

class Controller {
    public String filePath = "C:\\Users\\孤岛\\IdeaProjects\\SE_lab1\\src\\main\\resources\\words.txt";
    public String fileNewPath = "C:\\Users\\孤岛\\IdeaProjects\\SE_lab1\\src\\main\\resources\\wordNew.txt";
    public String savePath = "C:\\Users\\孤岛\\IdeaProjects\\SE_lab1\\src\\main\\resources\\directedMap.png";

    private   DirectedMap directedMap = null;
    public  void menu(){
        System.out.println("0:重新展示菜单");
        System.out.println("1:读入文本生成有向图,进行展示");
        System.out.println("2:查询桥接词");
        System.out.println("3:根据文本插入桥接词");
        System.out.println("4:计算两个单词之间的最短路径");
        System.out.println("5:随机游走,用户输入'x'进行停止");
        System.out.println("6:退出程序");
        System.out.println("7:输入单词，生成单词到其他单词路径");
    }

    public void userCenter() throws IOException {
        this.menu();
        System.out.print("input:");
        int c = System.in.read();
        while ((char)c != 'q' && (char)c != 'Q') {
            switch (c){
                case '0':{
                    this.menu();
                    break;
                }
                case '1':{
                    this.showDirectedGraph(filePath);
                    break;
                }
                case '2':{
                    System.out.println("请输入Word1:");
                    Scanner sc = new Scanner(System.in);
                    String word1 = sc.next(); // 从命令行读取单词
                    System.out.println("请输入Word2:");
                    String word2 = sc.next(); // 从命令行读取单词
                    System.out.println(this.queryBridgeWords(word1,word2));
                    break;
                }
                case '3':{
                    System.out.println("请输入Text:");
                    Scanner sc = new Scanner(System.in);
                    String input = sc.nextLine();
                    input = sc.nextLine();
                    System.out.println(this.generateNewText(input));
                    break;
                }
                case '4':{
                    System.out.println("请输入Word1:");
                    Scanner sc = new Scanner(System.in);
                    String word1 = sc.next(); // 从命令行读取单词
                    System.out.println("请输入Word2:");
                    String word2 = sc.next(); // 从命令行读取单词
                    String res = this.calcShortestPath(word1,word2);
                    if(this.calcShortestPath(word1,word2) != null){
                        System.out.println(res);
                    }else {
                        System.out.println("不可达");
                    }
                    break;
                }
                case '5':{
                    System.out.println(this.randomWalk());
                    break;
                }
                case '6':{
                    return;
                }
                case '7':{
                    System.out.println("请输入Word:");
                    Scanner sc = new Scanner(System.in);
                    String word = sc.next(); // 从命令行读取单词
                    this.calOtherPath(word);
                    break;
                }
            }
            System.out.print("input:");
            c = System.in.read();
        }
    }

    public void calOtherPath(String word) throws IOException {
        if (this.directedMap == null){
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
        }
        for (String targeWord:
             directedMap.wordSet) {
            if(!Objects.equals(word, targeWord)){
                System.out.print(word + "-->" + targeWord +":"+ this.calcShortestPath(word,targeWord));
            }
            System.out.println();
        }
        return;
    }
    public void showDirectedGraph(String filePath){
        try{
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
            DirectedMapUtils.showDirectedGraph(directedMap);
            DirectedMapUtils.saveDirectedGraph(directedMap,savePath);
        }catch (IOException e){
            return;
        }
    }

    public  String calcShortestPath(String word1, String word2) throws IOException {
        if (this.directedMap == null){
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
        }
        if(!directedMap.wordSet.contains(word1) ||!directedMap.wordSet.contains(word2) ){
            return "存在词不在此文段中";
        }

        return DirectedMapUtils.minPath(directedMap,word1,word2);
    }

    public String randomWalk() throws IOException {
        if (this.directedMap == null){
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
        }
        RandomTravel randomTravel = new RandomTravel();
        return randomTravel.startTravel(directedMap);
    }

    public String generateNewText(String inputText) throws IOException {
        if (this.directedMap == null){
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
        }
        String path = "./output.txt";
        String res = null;
        try {
            // 创建一个 FileWriter 对象，用于写入文件
            FileWriter fileWriter = new FileWriter(path);

            // 使用 BufferedWriter 包装 FileWriter，以提高性能
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // 将字符串写入文件
            bufferedWriter.write(inputText);

            // 关闭 BufferedWriter（会自动关闭 FileWriter）
            bufferedWriter.close();
            res = DirectedMapUtils.genTxtBasedOnBridge(directedMap,path);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }



    public String queryBridgeWords(String word1, String word2) throws IOException {
        if (this.directedMap == null){
            directedMap  = DirectedMapUtils.analyseInputFile(filePath);
        }
        if(!directedMap.wordSet.contains(word1) || !directedMap.wordSet.contains(word2)){
            return "No word1 or word2 in the graph!";
        }
        ArrayList<String> result =  DirectedMapUtils.selectBridgeWords(directedMap,word1,word2);

        if(result.size() == 0){
            return "No bridge words from word1 to word2!";
        }else{
            String ret = "The bridge words from word1 to word2 are:";
            if(result.size() == 1){
                ret += result.get(0);
                return ret;
            }

            for (int i = 0; i < result.size()-1; i++) {
                ret +=result.get(i) + ' ';
            }
            ret += "and " + result.get(result.size()-1);
            return ret;
        }
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.userCenter();
    }
}

