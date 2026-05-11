在windows系统下，mysql配置文件名称为 my.ini 而在 linux系统中，名称则为 my.cnf

以下是一个MySQL配置文件（my.cnf）的参考示例：

```ini
# MySQL配置文件示例

# 服务器设置
[server]
port = 3306
bind-address = 127.0.0.1

# 字符集设置
[client]
default-character-set = utf8mb4

[mysql]
default-character-set = utf8mb4

[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 日志设置
[log]
general-log = 0
log-error = /var/log/mysql/error.log

# 缓冲设置
[mysqld]
key_buffer_size = 16M
max_allowed_packet = 64M

# 连接设置
[mysqld]
max_connections = 200
max_user_connections = 100

# 查询缓存设置
[mysqld]
query_cache_type = 1
query_cache_size = 16M

# InnoDB存储引擎设置
[mysqld]
innodb_buffer_pool_size = 256M
innodb_flush_log_at_trx_commit = 1
innodb_lock_wait_timeout = 50

# 复制设置
[mysqld]
server-id = 1
log-bin = /var/log/mysql/mysql-bin.log

# 其他设置
[mysqld]
skip-name-resolve
```

这只是一个示例配置文件，实际的配置根据你的需求和系统环境可能会有所不同。你可以根据自己的需求进行相应的修改和调整。注意，在修改配置文件之后，可能需要重启MySQL服务才能使更改生效。

```ini
[client]
port    = 3306
socket = /tmp/mysql.sock
 
[mysql]
#prompt="\u@mysqldb \R:\m:\s [\d]> "
#关闭自动补全sql命令功能
no-auto-rehash
 
###########################################################################
##服务端参数配置
###########################################################################
[mysqld]
port    = 3306
datadir    =  /usr/mysql-8.0.32/data
log-error = /usr/mysql-8.0.32/logs/error.log
pid-file = /usr/mysql-8.0.32/data/mysql.pid
 
#只能用IP地址检查客户端的登录，不用主机名
skip_name_resolve = 1
 
#若你的MySQL数据库主要运行在境外，请务必根据实际情况调整本参数
default_time_zone = "+8:00"
 
#数据库默认字符集, 主流字符集支持一些特殊表情符号（特殊表情符占用4个字节）
character-set-server = utf8mb4
 
#数据库字符集对应一些排序等规则，注意要和character-set-server对应
collation-server = utf8mb4_general_ci
 
#设置client连接mysql时的字符集,防止乱码
init_connect='SET NAMES utf8mb4'
 
#是否对sql语句大小写敏感，1表示不敏感
#lower_case_table_names = 1
 
# 执行sql的模式，规定了sql的安全等级, 暂时屏蔽，my.cnf文件中配置报错
#sql_mode = STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
 
#事务隔离级别，默认为可重复读，mysql默认可重复读级别（此级别下可能参数很多间隙锁，影响性能）
transaction_isolation = READ-COMMITTED
 
#TIMESTAMP如果没有显示声明NOT NULL，允许NULL值
explicit_defaults_for_timestamp = true
 
#它控制着mysqld进程能使用的最大文件描述(FD)符数量。
#需要注意的是这个变量的值并不一定是你设定的值，mysqld会在系统允许的情况下尽量获取更多的FD数量
open_files_limit    = 65535
 
#最大连接数
max_connections = 300
 
#最大错误连接数
max_connect_errors = 600
 
#在MySQL暂时停止响应新请求之前的短时间内多少个请求可以被存在堆栈中
#官方建议 back_log = 50 + (max_connections / 5),封顶数为65535,默认值= max_connections
back_log = 110
 
# 为所有线程打开的表的数量
# 例如，对于200个并发运行的连接，指定表缓存大小至少为200 * N 
# 其中N是您执行的任何查询中每个连接的最大表数 
table_open_cache = 600
 
# 可以存储在定义缓存中的表定义数量 
# MIN(400 + table_open_cache / 2, 2000)
table_definition_cache = 700
 
# 为了减少会话之间的争用，可以将opentables缓存划分为table_open_cache/table_open_cache_instances个小缓存
table_open_cache_instances = 64
 
# 每个线程的堆栈大小 如果线程堆栈太小，则会限制执行复杂SQL语句
thread_stack = 512K
 
# 禁止外部系统锁
external-locking = FALSE
 
#SQL数据包发送的大小，如果有BLOB对象建议修改成1G
max_allowed_packet = 128M
 
#order by 或group by 时用到
#建议先调整为4M，后期观察调整
sort_buffer_size = 4M
 
#inner left right join时用到
#建议先调整为4M，后期观察调整
join_buffer_size = 4M
 
# How many threads the server should cache for reuse.
# 如果您的服务器每秒达到数百个连接，则通常应将thread_cache_size设置得足够高，以便大多数新连接使用缓存线程
# default value = 8 + ( max_connections / 100) 上限为100
thread_cache_size = 20
 
#MySQL连接闲置超过一定时间后(单位：秒)将会被强行关闭
#MySQL默认的wait_timeout  值为8个小时, interactive_timeout参数需要同时配置才能生效
interactive_timeout = 1800
wait_timeout = 1800
 
#Metadata Lock最大时长（秒）， 一般用于控制 alter操作的最大时长sine mysql5.6
#执行 DML操作时除了增加innodb事务锁外还增加Metadata Lock，其他alter（DDL）session将阻塞
lock_wait_timeout = 3600
 
#内部内存临时表的最大值。
#比如大数据量的group by ,order by时可能用到临时表，
#超过了这个值将写入磁盘，系统IO压力增大
tmp_table_size = 64M
max_heap_table_size = 64M
 
#--###########################-- 慢SQL日志记录 开始 --##########################################
 
#是否启用慢查询日志，1为启用，0为禁用  
slow_query_log = 1
 
#记录系统时区
log_timestamps = SYSTEM
 
#指定慢查询日志文件的路径和名字
slow_query_log_file = /usr/mysql-8.0.32/logs/slow.log
 
#慢查询执行的秒数，必须达到此值可被记录
long_query_time = 5
 
#将没有使用索引的语句记录到慢查询日志  
log_queries_not_using_indexes = 0
 
#设定每分钟记录到日志的未使用索引的语句数目，超过这个数目后只记录语句数量和花费的总时间  
log_throttle_queries_not_using_indexes = 60
 
#对于查询扫描行数小于此参数的SQL，将不会记录到慢查询日志中
min_examined_row_limit = 5000
 
#记录执行缓慢的管理SQL，如alter table,analyze table, check table, create index, drop index, optimize table, repair table等。  
log_slow_admin_statements = 0
 
#作为从库时生效, 从库复制中如何有慢sql也将被记录
#对于ROW格式binlog，不管执行时间有没有超过阈值，都不会写入到从库的慢查询日志
log_slow_slave_statements = 1
 
#--###########################-- 慢SQL日志记录 结束 --##########################################
 
 
#--###########################-- Bin-Log设置 开始 --############################################
server-id = 110
 
#开启bin log 功能
log-bin=mysql-bin
 
#binlog 记录内容的方式，记录被操作的每一行
binlog_format = ROW
 
#对于binlog_format = ROW模式时，FULL模式可以用于误操作后的flashBack。
#如果设置为MINIMAL，则会减少记录日志的内容，只记录受影响的列，但对于部分update无法flashBack
binlog_row_image = FULL
 
#bin log日志保存的天数
#如果 binlog_expire_logs_seconds 选项也存在则 expire_logs_days 选项无效
#expire_logs_days 已经被标注为过期参数
#expire_logs_days = 7
binlog_expire_logs_seconds = 1209600
 
#master status and connection information输出到表mysql.slave_master_info中
master_info_repository = TABLE
 
#the slave's position in the relay logs输出到表mysql.slave_relay_log_info中
relay_log_info_repository = TABLE
 
#作为从库时生效, 想进行级联复制，则需要此参数
log_slave_updates
 
#作为从库时生效, 中继日志relay-log可以自我修复
relay_log_recovery = 1
 
#作为从库时生效, 主从复制时忽略的错误
#如果在备份过程中执行ddl操作，从机需要从主机的备份恢复时可能会异常，从而导致从机同步数据失败
#如果对数据完整性要求不是很严格，那么这个选项确实可以减轻维护的成本
slave_skip_errors = ddl_exist_errors
 
#####RedoLog日志 和 binlog日志的写磁盘频率设置 BEGIN ###################################
# RedoLog日志（用于增删改事务操作） +  binlog日志（用于归档，主从复制）
# 为什么会有两份日志呢？ 
# 因为最开始MySQL没有 InnoDB 引擎,自带MyISAM引擎没有 crash-safe能力，binlog日志只用于归档
# InnoDB 引擎是另一个公司以插件形式引入MySQL的，采用RedoLog日志来实现 crash-safe 能力
 
# redo log 的写入（即事务操作）拆成两阶段提交（2PC）：prepare阶段 和 commit阶段
#(事务步骤1) 执行commit命令，InnoDB redo log 写盘，然后告知Mysql执行器:[你可以写binlog了，且一并提交事务]，事务进入 prepare 状态
#(事务步骤2) 如果前面 prepare 成功，Mysql执行器生成 binlog 并且将binlog日志写盘
#(事务步骤3) 如果binlog写盘成功，Mysql执行器一并调用InnoDB引擎的提交事务接口，事务进入 commit 状态，操作完成，事务结束
 
#参数设置成 1，每次事务都直接持久化到磁盘
#参数设置成 0，mysqld进程的崩溃会导致上一秒钟所有事务数据的丢失。
#参数设置成 2，只有在操作系统崩溃或者系统掉电的情况下，上一秒钟所有事务数据才可能丢失。
#即便都设置为1，服务崩溃或者服务器主机crash，Mysql也可能丢失但最多一个事务
 
#控制 redolog 写磁盘频率 默认为1
innodb_flush_log_at_trx_commit = 1
 
#控制 binlog 写磁盘频率
sync_binlog = 1
 
#####RedoLog日志 和 binlog日志的写磁盘频率设置 END #####################################
 
#一般数据库中没什么大的事务，设成1~2M，默认32kb
binlog_cache_size = 4M
 
#binlog 能够使用的最大cache 内存大小
max_binlog_cache_size = 2G
 
#单个binlog 文件大小 默认值是1GB
max_binlog_size = 1G
 
#开启GTID复制模式
gtid_mode = on
 
#强制gtid一致性，开启后对于create table ... select ...或 CREATE TEMPORARY TABLE 将不被支持
enforce_gtid_consistency = 1
 
#解决部分无主键表导致的从库复制延迟问题
#其基本思路是对于在一个ROWS EVENT中的所有前镜像收集起来，
#然后在一次扫描全表时，判断HASH中的每一条记录进行更新
#该参数已经被标注为过期参数
#slave-rows-search-algorithms = 'INDEX_SCAN,HASH_SCAN'
 
# default value is CRC32
#binlog_checksum = 1
 
# default value is ON
#relay-log-purge = 1
 
#--###########################-- Bin-Log设置 结束 --##########################################
 
#--###########################-- 可能用到的MyISAM性能设置 开始 --#############################
 
#对MyISAM表起作用，但是内部的临时磁盘表是MyISAM表，也要使用该值。
#可以使用检查状态值 created_tmp_disk_tables 得知详情
key_buffer_size = 15M
 
#对MyISAM表起作用，但是内部的临时磁盘表是MyISAM表，也要使用该值，
#例如大表order by、缓存嵌套查询、大容量插入分区。
read_buffer_size = 8M
 
#对MyISAM表起作用 读取优化
read_rnd_buffer_size = 4M
 
#对MyISAM表起作用 插入优化
bulk_insert_buffer_size = 64M
#--###########################-- 可能用到的MyISAM性能设置 开始 --################################
 
#--###########################-- innodb性能设置 开始 --##########################################
# Defines the maximum number of threads permitted inside of InnoDB. 
# A value of 0 (the default) is interpreted as infinite concurrency (no limit)
innodb_thread_concurrency = 0
 
#一般设置物理存储的 60% ~ 70%
innodb_buffer_pool_size = 8G
 
#当缓冲池大小大于1GB时，将innodb_buffer_pool_instances设置为大于1的值，可以提高繁忙服务器的可伸缩性
innodb_buffer_pool_instances = 4
 
#默认启用。指定在MySQL服务器启动时，InnoDB缓冲池通过加载之前保存的相同页面自动预热。 通常与innodb_buffer_pool_dump_at_shutdown结合使用
innodb_buffer_pool_load_at_startup = 1
 
#默认启用。指定在MySQL服务器关闭时是否记录在InnoDB缓冲池中缓存的页面，以便在下次重新启动时缩短预热过程
innodb_buffer_pool_dump_at_shutdown = 1
 
# 定义InnoDB系统表空间数据文件的名称、大小和属性
#innodb_data_file_path = ibdata1:1G:autoextend
 
#InnoDB用于写入磁盘日志文件的缓冲区大小（以字节为单位）。默认值为16MB
innodb_log_buffer_size = 32M
 
#InnoDB日志文件组数量
innodb_log_files_in_group = 3
 
#InnoDB日志文件组中每一个文件的大小
innodb_log_file_size = 2G
 
#是否开启在线回收（收缩）undo log日志文件，支持动态设置，默认开启
innodb_undo_log_truncate = 1
 
#当超过这个阀值（默认是1G），会触发truncate回收（收缩）动作，truncate后空间缩小到10M
innodb_max_undo_log_size = 4G
 
#The path where InnoDB creates undo tablespaces
#没有配置则在数据文件目录下
#innodb_undo_directory = /usr/mysql-8.0.32/undolog
 
#用于设定创建的undo表空间的个数
#已经弃用了，只能手动添加undo表空间
#innodb_undo_tablespaces变量已弃用，从MySQL 8.0.14开始不再可配置
#innodb_undo_tablespaces = 95
 
#提高刷新脏页数量和合并插入数量，改善磁盘I/O处理能力
#根据您的服务器IOPS能力适当调整
#一般配普通SSD盘的话，可以调整到 10000 - 20000
#配置高端PCIe SSD卡的话，则可以调整的更高，比如 50000 - 80000
innodb_io_capacity = 4000
innodb_io_capacity_max = 8000
 
#如果打开参数innodb_flush_sync, checkpoint时，flush操作将由page cleaner线程来完成，此时page cleaner会忽略io capacity的限制，进入激烈刷脏
innodb_flush_sync = 0
innodb_flush_neighbors = 0
 
#CPU多核处理能力设置，假设CPU是4颗8核的，设置如下
#读多，写少可以设成 2:6的比例
innodb_write_io_threads = 8
innodb_read_io_threads = 8
innodb_purge_threads = 4
innodb_page_cleaners = 4
innodb_open_files = 65535
innodb_max_dirty_pages_pct = 50
 
#该参数针对unix、linux，window上直接注释该参数.默认值为 NULL
#O_DIRECT减少操作系统级别VFS的缓存和Innodb本身的buffer缓存之间的冲突
innodb_flush_method = O_DIRECT
 
innodb_lru_scan_depth = 4000
innodb_checksum_algorithm = crc32
 
#为了获取被锁定的资源最大等待时间，默认50秒，超过该时间会报如下错误:
# ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
innodb_lock_wait_timeout = 20
 
#默认OFF，如果事务因为加锁超时，会回滚上一条语句执行的操作。如果设置ON，则整个事务都会回滚
innodb_rollback_on_timeout = 1
 
#强所有发生的死锁错误信息记录到 error.log中，之前通过命令行只能查看最近一次死锁信息
innodb_print_all_deadlocks = 1
 
#在创建InnoDB索引时用于指定对数据排序的排序缓冲区的大小
innodb_sort_buffer_size = 67108864
 
#控制着在向有auto_increment 列的表插入数据时，相关锁的行为，默认为2
#0：traditonal （每次都会产生表锁）
#1：consecutive （mysql的默认模式，会产生一个轻量锁，simple insert会获得批量的锁，保证连续插入）
#2：interleaved （不会锁表，来一个处理一个，并发最高）
innodb_autoinc_lock_mode = 1
 
#表示每个表都有自已独立的表空间
innodb_file_per_table = 1
 
#指定Online DDL执行期间产生临时日志文件的最大大小，单位字节，默认大小为128MB。
#日志文件记录的是表在DDL期间的数据插入、更新和删除信息(DML操作)，一旦日志文件超过该参数指定值时，
#DDL执行就会失败并回滚所有未提交的当前DML操作，所以，当执行DDL期间有大量DML操作时可以提高该参数值，
#但同时也会增加DDL执行完成时应用日志时锁定表的时间
innodb_online_alter_log_max_size = 4G
 
#--###########################-- innodb性能设置 结束 --##########################################
 
[mysqldump]
quick
max_allowed_packet = 128M
```

#### 为什么要修改mysql配置文件？

修改MySQL配置文件（my.cnf）是为了对MySQL服务器进行个性化配置，以满足特定需求和优化数据库性能。通过修改配置文件，可以更改各种参数和选项，以适应不同的工作负载和系统环境。

以下是一些常见的原因，为什么要修改MySQL配置文件：

1. **性能优化**：通过调整缓冲区大小、连接数限制、查询缓存和存储引擎参数等，可以改善数据库的性能和响应时间。

2. **字符集和排序规则**：配置文件允许你设置默认的字符集和排序规则，确保数据库能正确地处理和存储不同的语言字符和排序方式。

3. **日志记录和错误处理**：可以启用或禁用不同类型的日志记录，包括错误日志、查询日志和慢查询日志，以便跟踪数据库操作和故障排除。

4. **权限和安全性**：通过配置文件，可以设置访问控制、密码策略和安全选项，以保护数据库免受未经授权的访问和恶意行为。

5. **复制和高可用性**：配置文件中的参数可以用于设置MySQL复制和高可用性方案，例如设置主从复制或配置集群环境。

修改MySQL配置文件需要谨慎操作，确保了解每个参数的含义和可能的影响。在修改配置文件之前，建议备份原始文件，以便在需要时可以还原配置。

#### MySQL架构图

![img](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/d9d1701cbbf9409bb671a72ae7a61466~tplv-k3u1fbpfcp-zoom-in-crop-mark:1512:0:0:0.awebp)

可插拔式存储引擎意味着我们可以随时进行存储引擎切换

在MySQL5.7和之前的版本中，存在查询缓存这一项，但在MySQL8.0中移除了查询缓存，其原因是查询缓存的命中率很低，缓存是以查询语句作为key，以查询结果作为value。因此，想要命中缓存则必须key一模一样，即使查询语句存在空格、顺序、参数不同、大小写等不一样都会无法命中缓存。同时