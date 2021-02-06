## Sending Docker Logs to ElasticSearch and Kibana with FileBeat
- A self-hosted solution to store, search and analyze your logs using ELK stack (ElasticSearch, Logstash, Kibana)
- The Kibana interface let you very easily browse the logs previously stored in ElasticSearch
- To import the logs into ElasticSearch, can be achieved with the use of Logstash that supports numerous input plugins
- FileBeat is used as a replacement for Logstash. It was created because Logstash requires a JVM and tends to consume a lot of resources. Although FileBeat is simpler than Logstash, you can still do a lot of things with it.

### The setup meets the following requirements:
- All the docker container logs (available with the docker logs command) must be searchable in the Kibana interface.
- Even after being imported into ElasticSearch, the logs must remain available with the docker logs command.
- It should be as efficient as possible in terms of resource consumption (cpu and memory).
- It should be able to decode logs encoded in JSON.

### Architecture
1. Docker Daemon: Write the container logs in files and give additional information to FileBeat
2. Docker Log files: Logs file is written on the file system
3. ElasticSearch: Logs file are added to ES database by FileBeat
4. Kibana: A graphical interface to search the logs

### Add the services using docker-compose file
- image for Kibana
- image for filebeat

### Problems:
- [Filebeat docker Mac support](https://github.com/elastic/beats/issues/17310)
## Resources:
- [Sending Docker Logs to ElasticSearch and Kibana with FileBeat](https://www.sarulabs.com/post/5/2019-08-12/sending-docker-logs-to-elasticsearch-and-kibana-with-filebeat.html)