class BeanExecutionEventApi {

    constructor(callback) {
        this.eventSource = new EventSource("/api/v1/event/flow/execution")
        this.eventSource.onmessage = (event)=> {
            const data = JSON.parse(event.data)
            callback(data)
        }
    }

}