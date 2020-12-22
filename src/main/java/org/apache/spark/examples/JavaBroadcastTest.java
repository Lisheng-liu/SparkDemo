/**
 * Copyright (C) 2015 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.examples;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;

/**
 * 广播变量测试
 * <p>
 * 
 * @author liulisheng
 * @date : 2016年3月11日
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class JavaBroadcastTest {
    public static void main(String[] args) {
        SparkConf conf = new SparkConf()
                .setMaster("local")
                .setAppName("BroadCast");
        JavaSparkContext sc = new JavaSparkContext(conf);
        /*
         * 使用广播变量，只读变量，每个executor 只保留一份 task 共用， TorrentBroadcast  方式广播
         * */
       /* final Broadcast<String> blackname = sc.broadcast("dwj3");
        List<String> name = Arrays.asList(
                "dwj1",
                "dwj2",
                "dwj3");
        //String blackName = "dwj3";
        JavaRDD<String> nameRDD = sc.parallelize(name);
        JavaRDD<String> namefilter = nameRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                String blacknames = blackname.getValue();
                return !blacknames.equals(s);
            }
        });
        List<String> lastname = namefilter.collect();
        for(String str:lastname){
            System.out.println(str);
        }*/
       final Broadcast<String> blackName = sc.broadcast("dwj3");
       JavaRDD<String> nameRDD = sc.parallelize(Arrays.asList("dwj1","dwj2","dwj3","dwj4"),2);
       List<String> lastName = nameRDD.filter(new Function<String, Boolean>() {
           @Override
           public Boolean call(String v1) throws Exception {
               return !blackName.getValue().equals(v1);
           }
       }).collect();
       lastName.forEach(new Consumer<String>() {
           @Override
           public void accept(String s) {
               System.out.println(s);
           }
       });





    }
}