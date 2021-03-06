CSVDB
=====
### Yoav Marco and Ofek Nariski


# Running
The project was written in java.

A jar is supplied at out/artifacts/master_jar/master.jar.


# Design

### Table Organization
All database variable types are classes extending the abstract class DBVar: 

* DBInt 

* DBFloat

* DBVarchar

* DBTS

these types have a comparator and a toString function that is used in queries.

A table is just a **2D DBVar array**.

### Inner Table File Format
Tables are stored with [this](https://github.com/RuedigerMoeller/fast-serialization) serialization.
We originally used [java's built in serialization](https://docs.oracle.com/javase/10/docs/api/java/io/ObjectOutputStream.html) 
but the this new one is much much faster. It takes roughly half the time to load tables to RAM with this one, which is the major slowdown is queries.

Serialized table files are compressed with [GZIP](https://docs.oracle.com/javase/7/docs/api/java/util/zip/GZIPOutputStream.html).
GZIPping the files reduces their size about 10 times, 
and since we write straight to a GZIP stream the compression doesnt slow things by a much. It even sped things up.

### Queries
Queries are done with [java8's streams](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html). 
`where` is a [stream filter](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#filter-java.util.function.Predicate), 
`order by` is [sorting](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#sorted-java.util.Comparator), 
and `group by` uses the [stream iterator](https://docs.oracle.com/javase/8/docs/api/java/util/stream/BaseStream.html#iterator--).
`having` is just where after a group by.



# Optimization process and benchmarks

comparison _from clicks test_ | Built in serialization | Seri through GZIPStream | Fast Seri | Fast Seri through GZIPStream |
----|----|----|----|---
load file #1  | 27s | 25s | 14s | 18s
load file #2  | 83s | 55s | 24s | 36s
select        | 66s | 24s | 10s |10.5s
inner table file size     | 322MB | 62MB | 256MB | 56MB

(Benchmarks taken on my 4-core Intel i5 8250U lenovo laptop)

With a profiler we discovered that the (constant) calculation of the select command takes only ~2 seconds, 
and the rest is spent *loading the date from disk*. So we optimized table loading time.

