package org.apache.spark.examples;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.sources.In;
import scala.Int;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WordCountTest {

    public static void main(String[] args) {
    /*    SparkConf sparkConf = new SparkConf().setAppName("JavaLocalFileTest");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);

        JavaRDD<String> lines = jsc.textFile("file://" + args[0]);

        for (String line : lines.collect()) {
            System.out.println(line);
        }

        jsc.close();*/

    String inputFile = "/tmp/spark/ambari.properties.1";
    String master = "local";
    SparkConf sparkConf = new SparkConf().setAppName("wordcount");
        sparkConf.setMaster(master);
    JavaSparkContext javaSparkContext= new JavaSparkContext(sparkConf);

    JavaRDD<String> lines = javaSparkContext.textFile("file://"+inputFile);



       JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
           @Override
           public Iterable<String> call(String s) throws Exception {
               List<String> splits = Arrays.asList(s.split(" "));
               return splits;
           }
       });


        //4.将数据生成元组
        //第一个泛型是输入的数据类型，后两个参数是输出参数元组的数据

        JavaPairRDD<String,Integer> tuples = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) throws Exception {
                return new Tuple2<>(s,1);
            }
        });



        //5.聚合
        JavaPairRDD<String,Integer> sumd = tuples.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1+v2;
            }
        });



        //因为Java API 没有提供sortedBy 算子，此时需要将元组中的数据进行位置调换，排完序再换回来
        //第一次交换是为了排序

        JavaPairRDD<Integer,String> swapped = sumd.mapToPair(new PairFunction<Tuple2<String, Integer>, Integer, String>() {
            @Override
            public Tuple2<Integer, String> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.swap();
            }
        });

        //排序
        JavaPairRDD<Integer,String> sorted = swapped.sortByKey(false);

        //第二次交换是为了最终结果 <单词，数量>
        JavaPairRDD<String, Integer> res = sorted.mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(Tuple2<Integer, String> integerStringTuple2) throws Exception {
                return integerStringTuple2.swap();
            }
        });

        List<Tuple2<String,Integer>> top = res.take(3);
        for(Tuple2<String, Integer> tuple2 : top){
            System.out.println(tuple2);
        }

        JavaRDD<String> topNRes = res.mapPartitions(new FlatMapFunction<Iterator<Tuple2<String, Integer>>, String>() {
            @Override
            public Iterable<String> call(Iterator<Tuple2<String, Integer>> tuple2Iterator) throws Exception {
                return null;
            }
        });

        System.out.println(res.collect());
        res.saveAsTextFile("/tmp/spark/wordcount/out4");
        javaSparkContext.stop();
    }

}
