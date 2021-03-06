<template>
  <section class="section">
    <div class="details-header">
      <div class="field is-horizontal">
        <div class="field-label">
          <label class="label">Select DLQ:</label>
        </div>
        <div class="field-body">
          <div class="control">
            <div class="select">
              <select v-model="currentQueueId">
                <option
                  v-for="queueId in availableQueues"
                  v-text="queueId"
                  :key="queueId" />
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
    <hr>
    <div class="box">
      <h3 class="title">{{ currentQueueId }}</h3>
      <div>Queue Size: {{ queueSize }}</div>
      <div v-if="queueSize > 200">Showing top 200 messages</div>
      <button
        class="button"
        @click="onGetAllDLQMessages"
      >
        Get Items
      </button>
      &nbsp;
      <button
        class="button"
        @click="onDropAllDLQMessages"
      >
        Drop All Items
      </button>
    </div>
    <hr>
    <div class="columns">
      <div class="column is-desktop">
        <div class="card panel">
          <header class="card-header">
            <p class="card-header-title">Messages</p>
          </header>
          <div class="card-content">
            <table class="table is-fullwidth">
              <tr>
                <th>Actions</th>
                <th>MessageId</th>
                <th>Headers</th>
                <th>Body</th>
              </tr>
              <tr
                v-for="(message, index) in dlqMessages"
                :key="message.id"
              >
                <td>
                  <Button
                    class="button"
                    @click="onDropMessage(message.id, index)"
                  >
                    Drop
                  </Button>
                  &nbsp;
                  <Button
                    class="button"
                    @click="onRepublishMessage(message.id, index)"
                  >
                    Republish
                  </Button>
                </td>
                <td>
                  {{ message.id }}
                </td>
                <td>
                  <ul>
                    <li
                      v-for="(value, name) in message.headers"
                      :key="name"
                    >
                      {{ name }} => {{ value }}
                    </li>
                  </ul>
                </td>
                <td>
                  {{ message.body }}
                </td>
              </tr>
            </table>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
    const EndpointService = require('@/EndpointService.js');

    export default {
        props: {
            instance: {
                type: Object,
                required: true
            },
            queueSize: {
                type: Number,
                default: () => -1
            },
            dlqMessages: {
                type: Array,
                default: () => []
            },
            availableQueues: {
                type: Array,
                default: () => []
            },
            currentQueueId: {
                type: String,
                default: () => ''
            }
        },
        methods: {
            // eslint-disable-next-line
            async onDropMessage(messageId, index) {
                // eslint-disable-next-line
                console.log('on drop message');

                // eslint-disable-next-line
                console.log(`messageId: ${messageId}`);

                const response = await EndpointService.post(this.instance, 'drop-message', {
                    queueId: this.currentQueueId,
                    messageId: messageId
                });

                if(response.data === "success") {
                    await this.getQueueSize();
                }

                // eslint-disable-next-line
                console.log('on drop message - success');
            },
            async onRepublishMessage(messageId, index) {
                // eslint-disable-next-line
                console.log('on republish message');

                const response = await EndpointService.post(this.instance, 'publish-message', {
                    queueId: this.currentQueueId,
                    messageId: messageId
                });

                if(response.data === "success") {
                    await this.getQueueSize();
                }

                // eslint-disable-next-line
                console.log('on republish message - success');
            },
            async onGetAllDLQMessages() {
                // eslint-disable-next-line
                console.log('get all messages');

                const response = await EndpointService.get(this.instance, 'get-dlq-messages', {
                  queueId: this.currentQueueId,
                  limit: 200
                });

                this.$set(this, 'dlqMessages', response.data);

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('get all messages - success');
            },
            async onDropAllDLQMessages() {
                // eslint-disable-next-line
                console.log('drop all messages');

                await EndpointService.post(this.instance, 'drop-all-messages', {
                  queueId: this.currentQueueId,
                });

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('drop all messages - success');
            },
            async getQueueSize() {
                const queueSizeResponse = await EndpointService.getQueueSize(this.instance, this.currentQueueId);
                this.$set(this, 'queueSize', queueSizeResponse.data);
            },
        },
        data: () => ({
            queueSize: -1,
            dlqMessages: [],
            availableQueues: [],
            currentQueueId: ''
        }),
        watch: {
            currentQueueId: async function(newCurrentQueueId, oldCurrentQueueId) {
                if(newCurrentQueueId === oldCurrentQueueId) {
                    return;
                }

                //clear dlqMessages
                this.$set(this, 'dlqMessages', []);

                //get queue size
                const queueSizeResponse = await EndpointService.getQueueSize(this.instance, newCurrentQueueId);
                this.$set(this, 'queueSize', queueSizeResponse.data);
            }
        },
        async mounted() {
            const getQueuesResponse = await EndpointService.get(this.instance, 'get-queues');
            this.$set(this, 'availableQueues', getQueuesResponse.data);

            //setting first queue
            this.$set(this, currentQueueId, this.availableQueues[0]);
        }
    };
</script>
