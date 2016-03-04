package messaging

import (
	"encoding/json"
	"fmt"
	"model"
	"os"
	"reflect"
	"testing"

	"github.com/streadway/amqp"
)

var client *Client

func GetClient(t *testing.T) *Client {
	var err error
	if client != nil {
		return client
	}
	client, err = NewClient(uri(), false)
	if err != nil {
		t.Error(err)
		t.Fail()
	}
	client.SetupPublishing(JobsExchange)
	go client.Listen()
	return client
}

func shouldrun() bool {
	if os.Getenv("RUN_INTEGRATION_TESTS") != "" {
		return true
	}
	return false
}

func uri() string {
	return "amqp://guest:guest@rabbit:5672/"
}

func TestConstants(t *testing.T) {
	expected := 0
	actual := int(Launch)
	if actual != expected {
		t.Errorf("Launch was %d instead of %d", actual, expected)
	}
	expected = 1
	actual = int(Stop)
	if actual != expected {
		t.Errorf("Stop was %d instead of %d", actual, expected)
	}
	expected = 0
	actual = int(Success)
	if actual != expected {
		t.Errorf("Success was %d instead of %d", actual, expected)
	}
}

func TestNewStopRequest(t *testing.T) {
	actual := NewStopRequest()
	expected := &StopRequest{Version: 0}
	if !reflect.DeepEqual(actual, expected) {
		t.Errorf("NewStopRequest returned:\n%#v\n\tinstead of:\n%#v", actual, expected)
	}
}

func TestNewLaunchRequest(t *testing.T) {
	job := &model.Job{}
	actual := NewLaunchRequest(job)
	expected := &JobRequest{
		Version: 0,
		Job:     job,
		Command: Launch,
	}
	if !reflect.DeepEqual(actual, expected) {
		t.Errorf("NewLaunchRequest returned:\n%#v\n\tinstead of:\n%#v", actual, expected)
	}
}

func TestNewClient(t *testing.T) {
	if !shouldrun() {
		return
	}
	actual, err := NewClient(uri(), false)
	if err != nil {
		t.Error(err)
		t.Fail()
	}
	defer actual.Close()
	expected := uri()
	if actual.uri != expected {
		t.Errorf("Client's uri was %s instead of %s", actual.uri, expected)
	}
}

func TestClient(t *testing.T) {
	if !shouldrun() {
		return
	}

	client := GetClient(t)

	//defer client.Close()
	key := "tests"
	actual := ""
	expected := "this is a test"
	coord := make(chan int)

	handler := func(d amqp.Delivery) {
		d.Ack(false)
		actual = string(d.Body)
		coord <- 1
	}
	client.AddConsumer(JobsExchange, "test_queue", key, handler)
	client.Publish(key, []byte(expected))
	<-coord
	if actual != expected {
		t.Errorf("Handler received %s instead of %s", actual, expected)
	}

}

func TestSendTimeLimitRequest(t *testing.T) {
	if !shouldrun() {
		return
	}
	client := GetClient(t)
	var actual []byte
	coord := make(chan int)
	handler := func(d amqp.Delivery) {
		d.Ack(false)
		actual = d.Body
		coord <- 1
	}
	key := fmt.Sprintf("%s.%s", TimeLimitRequestsKey, "test")
	client.AddConsumer(JobsExchange, "test_queue1", key, handler)
	client.SendTimeLimitRequest("test")
	<-coord
	req := &TimeLimitRequest{}
	err := json.Unmarshal(actual, req)
	if err != nil {
		t.Error(err)
		t.Fail()
	}
	if req.InvocationID != "test" {
		t.Errorf("TimeLimitRequest's InvocationID was %s instead of test", req.InvocationID)
	}
}

func TestSendTimeLimitResponse(t *testing.T) {
	if !shouldrun() {
		return
	}
	client := GetClient(t)
	var actual []byte
	coord := make(chan int)
	handler := func(d amqp.Delivery) {
		d.Ack(false)
		actual = d.Body
		coord <- 1
	}
	key := fmt.Sprintf("%s.%s", TimeLimitResponseKey, "test")
	client.AddConsumer(JobsExchange, "test_queue2", key, handler)
	client.SendTimeLimitResponse("test", 0)
	<-coord
	resp := &TimeLimitResponse{}
	err := json.Unmarshal(actual, resp)
	if err != nil {
		t.Error(err)
		t.Fail()
	}
	if resp.InvocationID != "test" {
		t.Errorf("TimeLimitRequest's InvocationID was %s instead of test", resp.InvocationID)
	}
}

func TestSendTimeLimitDelta(t *testing.T) {
	if !shouldrun() {
		return
	}
	client := GetClient(t)
	var actual []byte
	coord := make(chan int)
	handler := func(d amqp.Delivery) {
		d.Ack(false)
		actual = d.Body
		coord <- 1
	}
	key := fmt.Sprintf("%s.%s", TimeLimitDeltaKey, "test")
	client.AddConsumer(JobsExchange, "test_queue3", key, handler)
	client.SendTimeLimitDelta("test", "10s")
	<-coord
	delta := &TimeLimitDelta{}
	err := json.Unmarshal(actual, delta)
	if err != nil {
		t.Error(err)
		t.Fail()
	}
	if delta.InvocationID != "test" {
		t.Errorf("TimeLimitDelta's InvocationID was %s instead of test", delta.InvocationID)
	}
	if delta.Delta != "10s" {
		t.Errorf("TimeLimitDelta's Delta was %s instead of 10s", delta.Delta)
	}
}

func TestSendStopRequest(t *testing.T) {
	if !shouldrun() {
		return
	}
	client := GetClient(t)
	var actual []byte
	var err error
	coord := make(chan int)
	invID := "test"
	handler := func(d amqp.Delivery) {
		d.Ack(false)
		actual = d.Body
		coord <- 1
	}
	key := fmt.Sprintf("%s.%s", StopsKey, invID)
	client.AddConsumer(JobsExchange, "test_queue4", key, handler)
	client.SendStopRequest(invID, "test_user", "this is a test")
	<-coord
	req := &StopRequest{}
	if err = json.Unmarshal(actual, req); err != nil {
		t.Error(err)
		t.Fail()
	}
	if req.Reason != "this is a test" {
		t.Errorf("Reason was '%s' instead of '%s'", req.Reason, "this is a test")
	}
	if req.InvocationID != invID {
		t.Errorf("InvocationID was %s instead of %s", req.InvocationID, invID)
	}
	if req.Username != "test_user" {
		t.Errorf("Username was %s instead of %s", req.Username, "test_user")
	}
}