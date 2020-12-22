package org.apache.spark.examples;

import org.apache.hadoop.hive.ql.exec.spark.session.SparkSession;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Encoders;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;

public class SparkWebUIShowAccumulatorDemo {
    public static void main(String[] args) {

    /*    SparkSession spark = SparkSession.builder().master("local[*]").getOrCreate();
        LongAccumulator fooCount = spark.sparkContext().longAccumulator("fooCount");

        spark.createDataset(Collections.singletonList(1024), Encoders.INT())
                .foreach((ForeachFunction<Integer>) fooCount::add);

        try {
            TimeUnit.DAYS.sleep(365 * 10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        SparkConf sparkConf = new SparkConf().setAppName("JavaAccumulatorTest").setMaster("local");
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        Accumulator<Integer> count = jsc.accumulator(0);
        JavaRDD<Integer> rdd = jsc.parallelize(Collections.singletonList(1024));
        JavaRDD<Integer> intRdd = rdd.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                count.add(1);
                return v1;
            }
        });
        intRdd.collect();
       /* intRdd.cache().collect().forEach((x->{
            System.out.println(x);
        }));*/


        try {
            TimeUnit.DAYS.sleep(365 * 10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
