package com.frtelg.temporal.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.frtelg.temporal.configuration.EnableTemporalIntegrationTest;
import com.frtelg.temporal.dto.NameFromWorkflowResponse;
import com.frtelg.temporal.dto.WorkflowResponse;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.EventType;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryRequest;
import io.temporal.client.WorkflowClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableTemporalIntegrationTest
class GreetingWorkflowIntegrationTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final WorkflowClient workflowClient;

    @Autowired
    public GreetingWorkflowIntegrationTest(WebApplicationContext webApplicationContext, ObjectMapper objectMapper, WorkflowClient workflowClient) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = objectMapper;
        this.workflowClient = workflowClient;
    }

    @Test
    void integrationTest() throws Exception {
        // Start workflow
        var startWorkflowRequest = MockMvcRequestBuilders.get("/workflow")
                .accept(MediaType.APPLICATION_JSON);

        var startWorkflowResponse = mockMvc.perform(startWorkflowRequest)
                .andExpect(status().isOk())
                .andReturn();

        var startWorkflowResponseBodyJson = startWorkflowResponse.getResponse()
                .getContentAsString();

        var startWorkflowResponseBody = objectMapper.readValue(startWorkflowResponseBodyJson, WorkflowResponse.class);

        var workflowId = startWorkflowResponseBody.workflowId();

        // Query workflow after start
        var queryWorkflowBeforeTriggerRequest = MockMvcRequestBuilders.get("/workflow/" + workflowId + "/current-name")
                .accept(MediaType.APPLICATION_JSON);

        var queryWorkflowBeforeTriggerResponse = mockMvc.perform(queryWorkflowBeforeTriggerRequest)
                .andExpect(status().isOk())
                .andReturn();

        var queryWorkflowBeforeTriggerResponseBodyJson = queryWorkflowBeforeTriggerResponse.getResponse()
                .getContentAsString();

        var queryWorkflowBeforeTriggerResponseBody = objectMapper.readValue(queryWorkflowBeforeTriggerResponseBodyJson, NameFromWorkflowResponse.class);

        assertEquals("Stranger", queryWorkflowBeforeTriggerResponseBody.name());

        // Trigger workflow
        var triggerWorkflowRequest = MockMvcRequestBuilders.put("/workflow/" + workflowId + "/Telg")
                .accept(MediaType.APPLICATION_JSON);

        var triggerWorkflowResponse = mockMvc.perform(triggerWorkflowRequest)
                .andExpect(status().isOk())
                .andReturn();

        var triggerWorkflowResponseBodyJson = triggerWorkflowResponse.getResponse()
                .getContentAsString();

        var triggerWorkflowResponseBody = objectMapper.readValue(triggerWorkflowResponseBodyJson, WorkflowResponse.class);

        assertEquals(workflowId, triggerWorkflowResponseBody.workflowId());

        // Query workflow after trigger
        var queryWorkflowAfterTriggerRequest = MockMvcRequestBuilders.get("/workflow/" + workflowId + "/current-name")
                .accept(MediaType.APPLICATION_JSON);

        var queryWorkflowAfterTriggerResponse = mockMvc.perform(queryWorkflowAfterTriggerRequest)
                .andExpect(status().isOk())
                .andReturn();

        var queryWorkflowAfterTriggerResponseBodyJson = queryWorkflowAfterTriggerResponse.getResponse()
                .getContentAsString();

        var queryWorkflowAfterTriggerResponseBody = objectMapper.readValue(queryWorkflowAfterTriggerResponseBodyJson, NameFromWorkflowResponse.class);

        assertEquals("Telg", queryWorkflowAfterTriggerResponseBody.name());

        // Terminate workflow
        var terminateWorkflowRequest = MockMvcRequestBuilders.delete("/workflow/" + workflowId)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(terminateWorkflowRequest)
                .andExpect(status().isOk())
                .andReturn();

        await().atMost(1000, TimeUnit.MILLISECONDS)
                .until(() -> getLatestWorkflowEvent(workflowId).getEventType() == EventType.EVENT_TYPE_WORKFLOW_EXECUTION_COMPLETED);

        assertEquals(EventType.EVENT_TYPE_WORKFLOW_EXECUTION_COMPLETED, getLatestWorkflowEvent(workflowId).getEventType());
    }

    private HistoryEvent getLatestWorkflowEvent(String workflowId) {
        var workflowHistory = workflowClient.getWorkflowServiceStubs()
                .blockingStub()
                .getWorkflowExecutionHistory(GetWorkflowExecutionHistoryRequest.newBuilder()
                        .setNamespace(workflowClient.getOptions().getNamespace())
                        .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build())
                        .build()
                );

        var numberOfHistoryEvents = workflowHistory.getHistory().getEventsCount();
        return workflowHistory.getHistory().getEvents(numberOfHistoryEvents - 1);
    }
}
