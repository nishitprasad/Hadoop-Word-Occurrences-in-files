#Count the number of files a particular word has occurred in a Single Node Hadoop cluster
```sh
Hadoop Version: 2.6
Operating System: Ubuntu 14.04
Cluster Environment: Single-Node (localhost)
Language: Java (JDK 7)
```
##Summary:
The program WordCount.java performs Map Reduce tasks for getting the count of the number of files that have a particular word. The result file shows the required count number against each word, regardless of the cases, special characters or numbers (just the letters).

##Design:

The design can be explained via a simple example.
Suppose the following are the contents in each file.

-   file1.txt
	>nishit!@$#$&nishit^&()*&*&$#$
	>prasad)(*&^?><Nishit-546

-   file2.txt
	>nishit2143465487Nishit69843421@#%@^&^%&^%@
	>Nishit@#@%#@%Nishit

-   file3.txt
	>@#)##112prasad>14365%^&*()
	>p324Prasad#@#%@P324

As per the requirement and abiding by the constraints provided, the following shall be the result:
```
Words      Count    Optional Description
nishit	    2		(present in files, file1.txt and file2.txt)
p	        1		(present in files, file3.txt)
prasad	    2		(present in files, file1.txt and file3.txt)
```
All the words are case-insensitive, hence, all words by default takes the lowercase value.

So the following is the flow that has been designed:

A static hash-map is created that contains <key, value> pairs as:
><"filename-word", dummy_value>

So in this case, the hash-map will have the following contents:
```sh
<"file1.txt-nishit, dummy_value>
<"file1.txt-prasad, dummy_value>
<"file2.txt-nishit, dummy_value>
<"file3.txt-prasad, dummy_value>
<"file3.txt-p, dummy_value>
```
So, it has been considered that:
```
if (hash-map has the key) then
	map-output = <word, 0>
else (hash-map doesn't have the key) then
	insert the key with dummy_value in hashmap
	map-output = <word, 1>
```
So the output provided my the Mapper class instances' map() method will be:
```sh
      file1.txt			file2.txt		file3.txt
		<nishit, 1>		<nishit, 1>		<prasad, 1>
		<nishit, 0>		<nishit, 0>		<p, 1>
		<prasad, 1>		<nishit, 0>		<prasad, 0>
		<nishit, 0>		<nishit, 0>		<p, 0>
```
The Combiner class aggregates the values of the same keys in the local files.
Hence, the output from the Combiner class, by default will be:
```sh
    file1.txt			file2.txt		file3.txt
	<nishit, 1>		    <nishit, 1>		<prasad, 1>
	<prasad, 1>			<p, 1>
```
The above output will act as input for the Reducer class and the reduce function will add all the values of same key.
Hence, the output given by the reduce class is:
```sh
    <nishit, 2>
    <prasad, 2>
    <p, 1>
```
##Implementation:
The following are the respective implementations done in the WordCount.java file:

In class Mapper:
```java
	private static HashMap<String, Integer> fileMap = new HashMap<String, Integer>();
	private static int count = 0; /*Dummy counter for value of a key

	In map() method:*/
		String givenLine = value.toString();
		String[] wordSet = givenLine.split("[^A-Za-z]+");
		for(String token: wordSet) {
			if(!token.isEmpty()) {
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
```
In class Reducer, nothing is changed. The same implementation as given in the typical WordCount java program.

##Build Command
- Make a Runnable jar with this java file via Eclipse.
- Make an input directory

    ```sh
    hadoop dfs -mkdir -p /input_files
    ```
- Copy all the text files from local machine to HDFS

    ```sh
    hadoop dfs -copyFromLocal <path of the file> /input_files
    ```
- Run the WordCount.jar file stored in local machine with first argument as input folder path and second argument as output folder path:

    ```sh
    hadoop jar <local path of WordCount.jar> /input_files /desired_output
    ```
- Print the necessary results in command prompt.

    ```sh	
    hdfs dfs -cat /desired_output/part-r-00000
    ```

```
Copyright [2016] [Nishit Prasad]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
