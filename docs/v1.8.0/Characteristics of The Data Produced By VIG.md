We here explain how VIG scales up an initial data instance through an example based on the NPD benchmark. The example is constructed upon the following components:

### NPD Ontology

~~~
npdv:ShallowWellbore rdfs:subClassOf npdv:Wellbore
npdv:SuspendedWellbore rdfs:subClassOf npdv:Wellbore
npdv:ExplorationWellbore rdfs:subClassOf npdv:Wellbore
npdv:ExplorationWellbore owl:disjointWith npdv:ShallowWellbore
~~~

### NPD Mappings

~~~
Target: npdv:wellbore/{id} a npdv:SuspendedWellbore. 
Source: SELECT id FROM shallow_wellbores

Target: npdv:wellbore/{id} a npdv:ExplorationWellbore.
Source: SELECT id FROM exploration_wellbores

Target: npdv:wellbore/{id} a SuspendedWellbore.
Source: SELECT id FROM exploration_wellbores WHERE state=`suspended`

Target: npdv:field/{fid} a Field.
Source: SELECT fid FROM fields

Target: npdv:wellbore/{id} npdv:completionYear {year}^^xsd:integer.
Source: SELECT id, year FROM shallow_wellbores

Target: npdv:wellbore/{id} npdv:name {name}^^xsd:string.
Source: SELECT id, name FROM shallow_wellbores

Target: npdv:wellbore/{id} npdv:completionYear {year}^^xsd:integer.
Source: SELECT id, year FROM exploration_wellbores

Target: npdv:wellbore/{id} npdv:name {name}^^xsd:string.
Source: SELECT id, name FROM exploration_wellbores

Target: npdv:wellbore/{id} npdv:explorationWellboreForField npdv:field/{fid}.
Source: SELECT id, fname FROM exploration_wellbores JOIN fields
~~~

VIG is a __data scaler__, that is, it automatically tunes its generation parameters to produce, starting from a source data instance __D__, a data instance __D'__ which is "similar" to __D__ but `s` times its size. Being the goal benchmarking, similarity is defined in terms of measures collected over __D__ which are commonly used by query optimizers in SQL planning. Moreover, OBDA-specific measures are considered to produce data of better quality and that is better suited for the OBDA context. We discuss here discuss the similarity measures used by VIG.

## Similarity Measures

### Schema Dependencies

The data produced by VIG will satisfy the schema dependencies (primary and foreign keys) of the source database instance. VIG supports multi-attribute primary keys, but it does not currently support multi-attribute foreign keys.

### Column-based Duplicates and NULL Ratios. 

They respectively measure the ratio of duplicates and of nulls in a given column, and are common parameters for the cost estimation performed by query planners in databases (e.g., PostgreSQL). By default, VIG maintains them
in the generated database to preserve the cost of joining columns in a key-foreign key relationship (e.g.,
the join from the last mapping assertion in the mappings for NPD).

This default behavior, however, is not applied with fixed-domain columns, which are columns whose content
does not depend on the size of the database instance. For instance, the column `state` in the table `exploration_wellbores` is fixed-domain, because it partitions the elements of `id` into a fixed number of classes (namely, 2). Fixed-domain columns can be manually specified through the `fixed` parameter in the configuration file, or are automatically detected by VIG through mappings analysis. The analysis search for mappings of the form

~~~
uri-template(a1, ..., an) rdf:type Class <- SELECT a1, ..., an FROM table WHERE b1='label1', ..., bm='labelm' 
~~~

and marks the columns `b1, ..., bm` as fixed domain. Notice that the automatic analysis performed by VIG captures common cases of fixed-domain columns, as the column `state` in table `exploration_table` discussed above. 

To generate values for a fixed-domain column, VIG reuses the values found in the original data instance so as
to prevent empty answers for the SQL queries in the mappings. For instance, a value `suspended` must be generated for the column `state` in order to produce objects for the class `SuspendedWellbore`.

In case the automatic analysis deliver columns that one wants not to be fixed-domain, then one can manually configure VIG to enforce these columns not to be recognized as fixed through the `non-fixed` parameter in the configuration file. However, notice that this will likely cause the SQL query in the mapping which caused the detection of the fixed column to deliver empty results.

#### Data Distribution

VIG generates values in columns according to a _uniform distribution_, that is, values
in columns have all the same probability of being repeated. Replication of the distributions from **D** is currently under study for being included in next releases of VIG.
 
### Joinable Columns Analysis

For this section, consider the following fragment of the source data instance __D__:

| fields.fid | ... |
| ------ | --- |
| 1      | ...  |
| 2      | ...  |
| 3      | ...  |
| .      | ...  |
| .      | ...  |
| .      | ...  |

| exploration_wellbores.id | ... |
| ------ | --- |
| 1      | ...  |
| 3      | ...  |
| 5      | ...  |
| .      | ...  |
| .      | ...  |
| .      | ...  |


| shallow_wellbores.id | ... |
| ------ | --- |
| 2      | ...  |
| 4      | ...  |
| 6      | ...  |
| .      | ...  |
| .      | ...  |
| .      | ...  |


By analyzing the mapping file it is possible to deduce what columns can possibly be joined over the database instance. VIG does this by finding mappings constructing individuals out of the same template. To show the concept, consider the following SPARQL query:

~~~
SELECT ?s WHERE {?s a npdv:ExplorationWellbore; a npdv:Field}
~~~

Intuitively, wellbores and fields are two distinct semantic objects and such query should always return empty results. However, observe that there are common values between elements in `exploration_wellbores.id` and `fields.fid` in the considered data instance **D**. Therefore, an SQL join between the two columns is non-empty. In a canonical OBDA translation for the considered SPARQL query, the join for these two columns is prevented by  by the fact that different uri templates are assigned to wellbores and fields (npdv:wellbore/{} and npdv:field/{}, respectively). In fact, by materializing the triples produced by the mappings and **D**, one would obtain the triples

| ?s | ?p | ?o |
| --- | --- | --- |
| npdv:wellbore/1 | a | npdv:ExplorationWellbore |  
| npdv:field/1 | a | npdv:Field |
| npdv:wellbore/3 | a | npdv:ExplorationWellbore |
| npdv:field/3 | a | npdv:Field |
| ... | ... | ... |

Observe that no object is shared between the classes **npdv:ExplorationWellbore** and **npdv:Field**. 

A different scenario, instead, occurs when dealing with individuals for the classes **npdv:ExplorationWellbore** and **npdv:ShallowWellbore**, since they are built out of the same template `npdv:wellbore/{}`. Since these two classes should be disjoint, according to the axiom `ExplorationWellbore owl:disjointWith ShallowWellbore` in the NPD ontology, the same uri should never be produced for both. Being the templates the same, this can only be achieved by ensuring that there are no values in common for the columns `exploration_wellbores.id` and `shallow_wellbores.id`. Observe that this is indeed the case in the considered instance **D**, since `exploration_wellbores.id` contains only odd numbers, as opposed to `shallow_wellbores.id` which contains even numbers. VIG is able to guarantee the disjointness on a scaled instance __D'__ of __D__. This is done by retrieving all sets of columns that could be possibly joined (i.e., by looking for those columns that occur in the same position of the same URI template), and by evaluating the selectivities of such joins on __D__ so as to reproduce them in __D'__. The next example gives an intuition of the mechanism:

#### Example

Consider the template `npdv:wellbore/{}` appearing in the mapping. The template has a single position/placeholder, which in the mapping is filled by the following set of columns:

`Cc := {exploration_wellbore.id, shallow_wellbore.id}`

VIG calculates such sets for each template appearing in the mapping file. For each of such sets Cc, VIG evaluates the selectivity for all possible joins between columns in Cc. In our example, VIG will evaluate the number of distinct results in the query

~~~
SELECT DISTINCT(exploration_wellbore.id) FROM exploration_wellbore JOIN shallow_wellbore
~~~

Such query returns 0 results when evaluated over **D**. VIG collects this statistics as a characteristic that should displayed by the data to be generated. In other words, VIG will generate data a scaled instance **D'** so that the same query, when evaluated over **D'**, will return 0 results as well. Observe that this is enough to guarantee that the disjointness between the classes **npdv:ShallowWellbore** and **npdv:ExplorationWellbore** is maintained for the instance **D'** and the considered set of mappings.

#### Current Limitations

Currently, the mentioned analysis is carried on only on mappings whose source part consist of a single table. Observe that VIG is able to reproduce disjointness at the column level, but not at the tuple level. This means that disjointness constraints in the ontology over classes whose individuals are built out of the values in a tuple, rather than in a single column as in our example, is not guaranteed.