import { get, getQueueSize } from '../../src/EndpointService';

describe('EndpointService', () => {
  describe('getQueueSize', () => {
    const instance = {};
    beforeEach(() => {
      instance.axios = {
        get: jest.fn().mockResolvedValue({data: []})
      };

      instance.endpoints = [
        {
          id: 'get-queues', url: 'https://something/get-queues'
        }
      ];
    });

    it('should pass url for get-queue-size to axios', async () => {
      //Arrange
      const queueId = 'queueId123123';

      //Act
      await getQueueSize(instance, queueId);

      //Assert
      expect(instance.axios.get).toBeCalledWith(`https://something/get-queue-size/${queueId}`, { params: {} })
    });
  });
});
