# temporal-spring-boot
An example Temporal application with Spring Boot

### How to use
Run `docker-compose up` to create a local Temporal deployment.
Then start TemporalApplication.

- Workflow can be started using REST:
  GET http://localhost:8081/workflow (returns the workflowId) `curl http://localhost:8081/workflow -H "Accept: application/json"`
- Trigger @SignalMethod changeName:
  PUT http://localhost:8081/workflow/{workflowId}/{newName} `curl -X PUT http://localhost:8081/workflow/{workflowId}/{newName} -H 'Content-Type: application/json'`
- Trigger @SignalMethod terminate:
  DELETE http://localhost:8081/workflow/{workflowId} `curl -X DELETE http://localhost:8081/workflow/{workflowId} -H 'Content-Type: application/json'`
- Trigger @QueryMethod getCurrentName:
  GET http://localhost:8081/workflow/{workflowId}/current-name `curl -X GET http://localhost:8081/workflow/{workflowId}/current-name -H 'Content-Type: application/json'`
- Temporal GUI can be accessed through http://localhost:8088