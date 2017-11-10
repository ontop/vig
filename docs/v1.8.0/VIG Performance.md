We here report the generation times that we encountered for producing data for the NPD Benchmark. 

### Hardware and Test Details

* **Machine:** HP Proliant Server, 24 Intel(R) Xeon(R) X5690@3.47GHz CPUs
* **Number of Threads:** Single Thread
* **RAM allowed to VIG:** 8 Gb

### Results

#### NPD5 : (approx 235 Mb)

~~~
real    3m51.162s
user    16m20.525s
sys     0m8.781s
~~~

#### NPD50: (approx 2.3 Gb)

~~~
real    21m11.663s
user    148m37.189s
sys     1m1.104s
~~~

#### NPD500: (approx 23.5 Gb)

~~~
real    194m16.946s
user    1470m3.136s
sys     9m47.857s
~~~

Observe that the wall clock time (`real` row) increases approximately as the scale factor, and that this growth is not influenced by the amount of data to be generated. 