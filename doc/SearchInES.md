## Start ElasticSearch/Kibana:
```
sudo systemctl start elasticsearch
sudo systemctl start kibana
```

## Stop ElasticSearch:
```
sudo systemctl stop elasticsearch
sudo systemctl stop kibana.service
```

## GET
```
curl -X GET http://localhost:9200/_cat/indices
curl -X GET http://localhost:9200/sink2/_doc/_search | jq
```

## DELETE
```
curl -X DELETE http://localhost:9200/sink2  
```

## [Install jq in Ubuntu](https://www.howtoinstall.me/ubuntu/18-04/jq/)