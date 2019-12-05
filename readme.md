
# 快速检查集群的健康状况
GET /_cat/health?v

# 快速查看集群中有哪些索引
GET /_cat/indices?v

# 简单的索引操作

## 创建索引
PUT /test_index?pretty
# 删除索引：
DELETE /test_index?pretty

# 商品的CRUD操作

# 新增商品：新增文档，建立索引

PUT /ecommerce/_doc/1
{
    "name" : "gaolujie yagao",
    "desc" :  "gaoxiao meibai",
    "price" :  30,
    "producer" :      "gaolujie producer",
    "tags": [ "meibai", "fangzhu" ]
}

PUT /ecommerce/_doc/2
{
    "name" : "jiajieshi yagao",
    "desc" :  "youxiao fangzhu",
    "price" :  25,
    "producer" :      "jiajieshi producer",
    "tags": [ "fangzhu" ]
}

PUT /ecommerce/_doc/3
{
    "name" : "zhonghua yagao",
    "desc" :  "caoben zhiwu",
    "price" :  40,
    "producer" :      "zhonghua producer",
    "tags": [ "qingxin" ]
}


# es会自动建立index，不需要提前创建，而且es默认会对document每个field都建立倒排索引，让其可以被搜索

# 查询商品：检索文档

GET /ecommerce/_doc/1


# 修改商品：替换文档

PUT /ecommerce/_doc/1
{
    "name" : "jiaqiangban gaolujie yagao",
    "desc" :  "gaoxiao meibai",
    "price" :  30,
    "producer" :      "gaolujie producer",
    "tags": [ "meibai", "fangzhu" ]
}

# 替换方式有一个不好，即使必须带上所有的field，才能去进行信息的修改

# 修改商品：更新文档

POST /ecommerce/_update/1
{
  "doc": {
    "name": "jiaqiangban gaolujie yagao 2"
  }
}

# 删除商品：删除文档

DELETE /ecommerce/_doc/1


# 高级搜索部分
# --------------------------------------------------

# query string search
# query DSL
# query filter
# full-text search
# phrase search
# highlight search

# 1、query string search
# 搜索全部商品：
GET /ecommerce/_search

# took：耗费了几毫秒
# timed_out：是否超时，这里是没有
# _shards：数据拆成了1个分片，所以对于搜索请求，会打到所有的primary shard（或者是它的某个replica shard也可以）
# hits.total：查询结果的数量，3个document
# hits.max_score：score的含义，就是document对于一个search的相关度的匹配分数，越相关，就越匹配，分数也高
# hits.hits：包含了匹配搜索的document的详细数据

# query string search的由来，因为search参数都是以http请求的query string来附带的

# 搜索商品名称中包含yagao的商品，而且按照售价降序排序：
GET /ecommerce/_search?q=name:yagao&sort=price:desc

# 适用于临时的在命令行使用一些工具，比如curl，快速的发出请求，来检索想要的信息；但是如果查询请求很复杂，是很难去构建的
# 在生产环境中，几乎很少使用query string search

# 2、query DSL

# DSL：Domain Specified Language，特定领域的语言
# http request body：请求体，可以用json的格式来构建查询语法，比较方便，可以构建各种复杂的语法，比query string search肯定强大多了

# 查询所有的商品

GET /ecommerce/_search
{
  "query": { "match_all": {} }
}

# 查询名称包含yagao的商品，同时按照价格降序排序

GET /ecommerce/_search
{
    "query" : {
        "match" : {
            "name" : "yagao"
        }
    },
    "sort": [
        { "price": "desc" }
    ]
}

# 分页查询商品，总共3条商品，假设每页就显示1条商品，现在显示第2页，所以就查出来第2个商品

GET /ecommerce/_search
{
  "query": { "match_all": {} },
  "from": 1,
  "size": 1
}

# 指定要查询出来商品的名称和价格就可以

GET /ecommerce/_search
{
  "query": { "match_all": {} },
  "_source": ["name", "price"]
}

# 更加适合生产环境的使用，可以构建复杂的查询

# 3、query filter

# 搜索商品名称包含yagao，而且售价大于25元的商品

GET /ecommerce/_search
{
    "query" : {
        "bool" : {
            "must" : {
                "match" : {
                    "name" : "yagao" 
                }
            },
            "filter" : {
                "range" : {
                    "price" : { "gt" : 25 } 
                }
            }
        }
    }
}

# 4、full-text search（全文检索）

GET /ecommerce/_search
{
    "query" : {
        "match" : {
            "producer" : "yagao producer"
        }
    }
}


# producer这个字段，会先被拆解，建立倒排索引
# 
# special		4
# yagao		4
# producer	1,2,3,4
# gaolujie	1
# zhognhua	3
# jiajieshi	2
# 
# yagao producer ---> yagao和producer


# 5、phrase search（短语搜索）

# 全文检索相对应，相反，全文检索会将输入的搜索串拆解开来，去倒排索引里面去一一匹配，只要能匹配上任意一个拆解后的单词，就可以作为结果返回
# phrase search，要求输入的搜索串，必须在指定的字段文本中，完全包含一模一样的，才可以算匹配，才能作为结果返回

GET /ecommerce/_search
{
    "query" : {
        "match_phrase" : {
            "producer" : "yagao producer"
        }
    }
}

# 6、highlight search（高亮搜索结果）

GET /ecommerce/_search
{
    "query" : {
        "match" : {
            "producer" : "producer"
        }
    },
    "highlight": {
        "fields" : {
            "producer" : {}
        },
        "pre_tags" : ["<b>"],
        "post_tags" : ["</b>"]
    }
}
# 分析搜索
# --------------------------------------------------


# 第一个分析需求：计算每个tag下的商品数量

GET /ecommerce/_search
{
  "aggs": {
    "group_by_tags": {
      "terms": { "field": "tags" }
    }
  }
}

# 默认情况下, Elasticsearch 对 text 类型的字段(field)禁用了 fielddata;
# text 类型的字段在创建索引时会进行分词处理, 而聚合操作必须基于字段的原始值进行分析;
# 所以如果要对 text 类型的字段进行聚合操作, 就需要存储其原始值 —— 创建mapping时指定fielddata=true, 以便通过反转倒排索引(即正排索引)将索引数据加载至内存中

# 将文本field的fielddata属性设置为true
PUT /ecommerce/_mapping
{
  "properties": {
    "tags": {
      "type": "text",
      "fielddata": true
    }
  }
}

GET /ecommerce/_search
{
  "size": 0,
  "aggs": {
    "all_tags": {
      "terms": { "field": "tags" }
    }
  }
}

# 第二个聚合分析的需求：对名称中包含yagao的商品，计算每个tag下的商品数量

GET /ecommerce/_search
{
  "size": 0,
  "query": {
    "match": {
      "name": "yagao"
    }
  },
  "aggs": {
    "all_tags": {
      "terms": {
        "field": "tags"
      }
    }
  }
}

# 第三个聚合分析的需求：先分组，再算每组的平均值，计算每个tag下的商品的平均价格

GET /ecommerce/_search
{
    "size": 0,
    "aggs" : {
        "group_by_tags" : {
            "terms" : { "field" : "tags" },
            "aggs" : {
                "avg_price" : {
                    "avg" : { "field" : "price" }
                }
            }
        }
    }
}


# 第四个数据分析需求：计算每个tag下的商品的平均价格，并且按照平均价格降序排序

GET /ecommerce/_search
{
    "size": 0,
    "aggs" : {
        "all_tags" : {
            "terms" : { "field" : "tags", 
            "order": { "avg_price": "desc" } 
          },
          "aggs" : {
              "avg_price" : {
                  "avg" : { "field" : "price" }
              }
          }
        }
    }
}



# 第五个数据分析需求：按照指定的价格范围区间进行分组，然后在每组内再按照tag进行分组，最后再计算每组的平均价格

GET /ecommerce/_search
{
  "size": 0,
  "aggs": {
    "group_by_price": {
      "range": {
        "field": "price",
        "ranges": [
          {
            "from": 0,
            "to": 20
          },
          {
            "from": 20,
            "to": 40
          },
          {
            "from": 40,
            "to": 50
          }
        ]
      },
      "aggs": {
        "group_by_tags": {
          "terms": {
            "field": "tags"
          },
          "aggs": {
            "average_price": {
              "avg": {
                "field": "price"
              }
            }
          }
        }
      }
    }
  }
}
