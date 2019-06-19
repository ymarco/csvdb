CSVDB
======

Running
-----
The project was written in java.

There is a jar file in out/artifacts/master_jar/master.jar. It requires **java version 12**.

Design
------

### Table Organization
All database variable types are classes extending the abstract class DBVar: 

* DBInt 

* DBFloat

* DBVarchar

* DBTS

these types have a comparator and a toString function for queries.

A table is just a **2D DBVar array**.

### Inner Table File Format
Tables are stored with [this](https://github.com/RuedigerMoeller/fast-serialization) serialization.
We originally used [java's built in serialization](https://docs.oracle.com/javase/10/docs/api/java/io/ObjectOutputStream.html) 
but the current one we use on github is much much faster. It takes roughly half the time to load tables to RAM with this one.

Serialized table files are compressed with [GZIP](https://docs.oracle.com/javase/7/docs/api/java/util/zip/GZIPOutputStream.html).
GZIPping the files reduces their size about 12 times, 
and since we write straight to a GZIP stream the compression doesnt slow things by a much.

### Queries
Queries are done with [java8's streams](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html). 
Where is a [stream filter](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#filter-java.util.function.Predicate), 
order by is [sorting](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#sorted-java.util.Comparator), 
and group by is implemented by us by using the [stream iterator](https://docs.oracle.com/javase/8/docs/api/java/util/stream/BaseStream.html#iterator--).
having is just where after a group by.


Optimization process and benchmarks
-------

Command from clicks | Built in serialization | Seri through GZIPStream | Fast Seri through GZIPStream
---|---|---|---
load file #1 | 27s | 25s | 18s
load file #2 | 83s | 55s | 36s
select  | 66s | 24s | 10.5s

Using a profiler we know that the (constant) the calculation of the select command takes approximately 2 seconds, 
and the rest is spent loading the date from disk. Thus we tried to optimize that, and succeeded.
