# rdelay
a redis-based lightweight timing framework powered by spring boot as personal experimental work

### Theory
Use redis ZSET to maintain taskIds which are ordered by their execution time asc,get the tasks whose execution time is 
reached by now and then send them back to the url specified by each task respectively.It supports retry mechanism.


### Attention
The framework is now very primitive,there is much work ahead.Use it at your own risk.


### Rough structure
![Rough structure](./assets/rdealy.png)