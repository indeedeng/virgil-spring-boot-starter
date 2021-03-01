/**
 *
 * @param {{endpoints: Array}} instance
 * @param {string} endpointId
 * @returns {string|*}
 */
export const getEndpointUrl = (instance, endpointId) => {
    for(let i = 0, len = instance.endpoints.length; i < len; i++) {
        if(instance.endpoints[i].id === endpointId) {
            return instance.endpoints[i].url;
        }
    }

    return '';
};

/**
 *
 * @param {{axios: Object, endpoints: Array}} instance
 * @param endpointId
 * @param [queryParams={}]
 * @returns {Promise<{data: *, errors: [{message: string, code: string}]}>}
 */
export const get = async (instance, endpointId, queryParams = {}) => {

    const getUrl = getEndpointUrl(instance, endpointId);

    const axiosResponse = await instance.axios.get(getUrl, {
        params: queryParams
    });

    const endpointResponse = axiosResponse.data;

    return endpointResponse;
};

/**
 *
 * @param {{axios: Object, endpoints: Array}} instance
 * @param {string} endpointId
 * @param {{Object}} payload
 * @param [queryParams={}]
 * @returns {Promise<{data: *, errors: [{message: string, code: string}]}>}
 */
export const post = async (instance, endpointId, payload = {}, queryParams = {}) => {
    const postUrl = getEndpointUrl(instance, endpointId);

    const optionalConfig = {};

    if(queryParams) {
        optionalConfig.params = queryParams;
    }

    const axiosResponse = await instance.axios.post(postUrl, payload, optionalConfig);

    const endpointResponse = axiosResponse.data;

    return endpointResponse;
};

/**
 *
 * @param {{axios: Object, endpoints: Array}} instance
 * @param {string} queueId
 * @returns {Promise<{data: *, errors: [{message: string, code: string}]}>}
 */
export const getQueueSize = async (instance, queueId) => {
  //templated URLs are not registered within the instance, so
  // we lookup a related url for the base Url
  const getUrl = getEndpointUrl(instance, 'get-queues');
  const computedUrl = `${getUrl.replace('get-queues', 'get-queue-size')}/${queueId}`

  const axiosResponse = await instance.axios.get(computedUrl, {params: {}})

  return axiosResponse.data;
};
