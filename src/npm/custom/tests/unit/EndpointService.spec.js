import { get, getQueueSize } from '../../src/EndpointService';

describe('EndpointService', () => {
  describe('get', () => {
    const instance = {};
    beforeEach(() => {
      instance.axios = {
        get: jest.fn().mockResolvedValue({data: []})
      };

      instance.endpoints = [
        {
          id: 'get-queue-size', url: 'https://something/get-queue-size'
        }
      ];
    });

    it('should pass url with queueId to axios', async () => {
      //Act
      await get(instance, 'get-queue-size', 'primary');

      //Assert
      expect(instance.axios.get).toBeCalledWith('https://something/get-queue-size/primary', {params: {}});
    });

    it('should pass url without queueId to axios', async () => {
      //Act
      await get(instance, 'get-queue-size');

      //Assert
      expect(instance.axios.get).toBeCalledWith('https://something/get-queue-size', {params: {}});
    });
  });

  describe('getQueueSize', () => {
    const instance = {};
    beforeEach(() => {
      instance.axios = {
        get: jest.fn().mockResolvedValue({data: []})
      };

      instance.endpoints = [
        {
          id: 'get-queues', url: 'https://something/get-queues'
        },
        {
          id: 'get-queue-size', url: 'https://something/get-queue-size'
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
