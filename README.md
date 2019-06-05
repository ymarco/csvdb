csvdb
========

file format
-------------

every column in a table is stored in a separate file.
timestamps, ints and floats are stored in binary formats.

varchars are stored in a continuous stream, with a '\0' character terinating each one.
a (also binary) pointers file is stored with each varchars file.
this lets us relate to them as c-strings when querying.


query steps
----------

**where** creates a binary index file containing only the indexes of the objects to be in the new table.


**order-by** takes an index file (or creates a default one if none is specified), sorts the indexes part by part (because the data may not fit in ram)
**and then writes the sorted parts as normal data, *not index data*, to a new table**.

then it merges the sorted parts to a new final table.


**group-by** does the same thing as order-by, then collapses the grouped-by columns and creates the aggragated column (if specified an aggragating function, e.g sum(\*)).
it takes an index file, orders-by it and in the merging collapses and aggragets, and outputs a table.


**having** is just where on an grouped-by table.