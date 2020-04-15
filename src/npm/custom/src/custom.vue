<template>
  <section class="section">
    <div class="details-header">
      <div>Queue Size: {{ queueSize }}</div>
    </div>
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
                <th>Message Fingerprint</th>
                <th>Headers</th>
                <th>Body</th>
              </tr>
              <tr
                v-for="message in dlqMessages"
                :key="message.fingerprint"
              >
                <td>
                  <Button
                    class="button"
                    @click="onDropMessage(message.fingerprint)"
                  >
                    Drop
                  </Button>
                  &nbsp;
                  <Button
                    class="button"
                    @click="onRepublishMessage(message.fingerprint)"
                  >
                    Republish
                  </Button>
                </td>
                <td>
                  {{ message.fingerprint }}
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
            }
        },
        methods: {
            // eslint-disable-next-line
            async onDropMessage(fingerprint) {
                // eslint-disable-next-line
                console.log('on drop message');

                // eslint-disable-next-line
                console.log(`fingerprint: ${fingerprint}`);

                await EndpointService.post(this.instance, 'drop-message', {
                    fingerprint: fingerprint
                });

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('on drop message - success');
            },
            async onRepublishMessage(fingerprint) {
                // eslint-disable-next-line
                console.log('on republish message');

                await EndpointService.post(this.instance, 'publish-message', {
                    fingerprint: fingerprint
                });

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('on republish message - success');
            },
            async onGetAllDLQMessages() {
                // eslint-disable-next-line
                console.log('get all messages');

                const response = await EndpointService.get(this.instance, 'get-dlq-messages');

                this.dlqMessages = response.data;

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('get all messages - success');
            },
            async onDropAllDLQMessages() {
                // eslint-disable-next-line
                console.log('drop all messages');

                await EndpointService.post(this.instance, 'drop-all-messages');

                await this.getQueueSize();

                // eslint-disable-next-line
                console.log('drop all messages - success');
            },
            async getQueueSize() {
                const queueSizeResponse = await EndpointService.get(this.instance, 'get-queue-size');
                this.queueSize = queueSizeResponse.data;
            },
        },
        data: () => ({
            queueSize: -1,
            dlqMessages: []
        }),
        async created() {
            const queueSizeResponse = await EndpointService.get(this.instance, 'get-queue-size');
            this.queueSize = queueSizeResponse.data;
        }
    };
</script>
