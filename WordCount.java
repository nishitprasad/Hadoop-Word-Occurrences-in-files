import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Counter;

import org.apache.hadoop.mapreduce.lib.input.FileSplit;


public class WordCount {

  public static class MyMapper
    extends Mapper<Object, Text, Text, IntWritable> {
    private Configuration conf;
	private Text word = new Text();
	private static HashMap<String, Integer> fileMap = new HashMap<String, Integer>();
	private static int count = 0;
    @Override
    public void setup(Context context) throws IOException,
      InterruptedException {
      conf = context.getConfiguration();
    }

    @Override
    public void map(Object key, Text value, Context context
                   ) throws IOException, InterruptedException {
      // The following to line is to get which file current key vale pair comes from.
      FileSplit filesplit = (FileSplit)context.getInputSplit();
      String filename = filesplit.getPath().getName();
      
	//My code
	String givenLine = value.toString();
	String[] wordSet = givenLine.split("[^A-Za-z]+");
	for(String token: wordSet){
		if(!token.isEmpty()){
			word.set(token.toLowerCase());
			String keyStr = filename +"-"+ word.toString();
			if(fileMap.containsKey(keyStr))
		    	context.write(word, new IntWritable(0));
			else {
				fileMap.put(keyStr, ++count);
		    	context.write(word, new IntWritable(1));
			}
		}
	}
    }
  }

  public static class MyReducer
    extends Reducer<Text, IntWritable, Text, IntWritable> {
    private IntWritable result = new IntWritable();
    public List<Integer> files;
	
	public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                      ) throws IOException, InterruptedException {
    files = new ArrayList<Integer>();
    
    int sum=0;
      for (IntWritable val : values) {
    		  sum += val.get();
      }
     result.set(sum);
     context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    // use this conf to store int, double, string, boolean values, which can be retrieved at Map and Reduce tasks.

    Job job = Job.getInstance(conf, "wordcount");
    job.setJarByClass(WordCount.class);
    job.setMapperClass(MyMapper.class);
    job.setCombinerClass(MyReducer.class);
    job.setReducerClass(MyReducer.class);

    // specify the output type for both map and reduce
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    //if the output type of map task is different from reduce task, specify them  this way
    job.setMapOutputKeyClass(Text.class);
    job.setMapOutputValueClass(IntWritable.class);

    job.setJarByClass(WordCount.class);
    //job.setInputFormatClass(TextInputFormat.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));

    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
