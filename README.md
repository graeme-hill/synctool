# synctool

## Test cases

### Single node

#### Steps

* Startup
* Init database
* Create self node X
* Create collection A
* Create dir1 with some files
* Create node_collection X, A, dir1
* Create node_collection X, A, dir2

#### Result

* `dir2` gets created and has same files as `dir1`
* new/updated files in either dir sync to other