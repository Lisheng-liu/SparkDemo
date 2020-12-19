package org.apache.spark.examples.sql;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.Arrays;
import java.util.List;

public class TestJavaSparkSQL {

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("sparkSql");
        sparkConf.setMaster("local");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(ctx);

//        testTextDataSet(ctx,sqlContext);
//        testTextParquet(ctx,sqlContext);
        testJsonDataset(ctx,sqlContext);
    }

    public static void testTextDataSet(JavaSparkContext ctx,SQLContext sqlContext){
        System.out.println("=== Data source: RDD ===");
        // 数据源为txt文本
        // 加载txt文件，文件在hdfs中
        JavaRDD<Person> people = ctx.textFile("/tmp/examples/people.txt").map(new Function<String, Person>() {
            @Override
            public Person call(String line) throws Exception {
                String[] parts = line.split(",");
                TestJavaSparkSQL.Person person = new TestJavaSparkSQL.Person();
                person.setName(parts[0]);
                person.setAge(Integer.parseInt(parts[1].trim()));

                return person;
            }
        });

        DataFrame schemaPeople = sqlContext.createDataFrame(people,TestJavaSparkSQL.Person.class);
        schemaPeople.registerTempTable("people");

        DataFrame teengers = sqlContext.sql("select name from people where age > 1 and age < 100");

        List<String> teenagerNames = teengers.toJavaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return row.getString(0);
            }
        }).collect();

        for (String name : teenagerNames) {
            System.out.println(name);
        }
    }

    public static void testTextParquet(JavaSparkContext ctx,SQLContext sqlContext){

        System.out.println("=== Data source: RDD ===");
        // 数据源为txt文本
        // 加载txt文件，文件在hdfs中
        JavaRDD<Person> people = ctx.textFile("/tmp/examples/people.txt").map(new Function<String, Person>() {
            @Override
            public Person call(String line) throws Exception {
                String[] parts = line.split(",");
                TestJavaSparkSQL.Person person = new TestJavaSparkSQL.Person();
                person.setName(parts[0]);
                person.setAge(Integer.parseInt(parts[1].trim()));

                return person;
            }
        });

        DataFrame schemaPeople = sqlContext.createDataFrame(people,TestJavaSparkSQL.Person.class);

        schemaPeople.write().parquet("/tmp/examples/people.parquet");

        // 再将刚刚存储的parquet文件读取出来
        DataFrame parquetFile = sqlContext.read().parquet("/tmp/examples/people.parquet");
        parquetFile.registerTempTable("people");

        DataFrame teengers = sqlContext.sql("select name from people where age > 1 and age < 100");

        List<String> teenagerNames = teengers.toJavaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return row.getString(0);
            }
        }).collect();

        for (String name : teenagerNames) {
            System.out.println(name);
        }
    }

    private static void testJsonDataset(JavaSparkContext ctx, SQLContext sqlContext) {
        System.out.println("=== Data source: JSON Dataset ===");
        // 数据源为json文件
        String path = "/tmp/examples/people.json"; // hdfs中
        // 从json数据源创建DataFrame
        DataFrame peopleFromJsonFile = sqlContext.read().json(path);

        // 由于json格式的文件能够直接推断出数据结构，所以我们直接打印下看看
        peopleFromJsonFile.printSchema();
        // 打印如下：
        // root
        // |-- age: long (nullable = true)
        // |-- name: string (nullable = true)

        // 将DataFrame注册为一个Table
        peopleFromJsonFile.registerTempTable("people");

        // 执行SQL查询
        DataFrame teenagers = sqlContext.sql("SELECT name FROM people WHERE age >= 13 AND age <= 19");
        // 处理（名称前加"Name:"）并打印结果
        List<String> teenagerNames = teenagers.toJavaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) throws Exception {
                return row.getString(0);
            }
        }).collect();

        for (String name : teenagerNames) {
            System.out.println(name);
        }



        // 换一个json数据结构的例子
        List<String> jsonData = Arrays.asList(new String[]{"{\"name\":\"Yin\",\"address\":{\"city\":\"Columbus\",\"state\":\"Ohio\"}}"});
        JavaRDD<String>  aPeopleRDD = ctx.parallelize(jsonData);
        DataFrame peopleFromJsonRDD = sqlContext.read().json(aPeopleRDD.rdd());
        // 打印出新的数据结构
        peopleFromJsonRDD.printSchema();
        // 打印如下：
        // root
        // |-- address: struct (nullable = true)
        // | |-- city: string (nullable = true)
        // | |-- state: string (nullable = true)
        // |-- name: string (nullable = true)

        // 将DataFrame注册为一个Table
        peopleFromJsonRDD.registerTempTable("people2");

        // 执行SQL查询
        DataFrame peopleWithCity = sqlContext.sql("SELECT name, address.city FROM people2");
        List<String> nameAndCity = peopleWithCity.toJavaRDD().map(new Function<Row, String>() {
            @Override
            public String call(Row row) {
                return "Name: " + row.getString(0) + ", City: " + row.getString(1);
            }
        }).collect();
        for (String name : nameAndCity) {
            System.out.println(name);
        }
    }

    public  static class Person {
        private String name;

        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}
