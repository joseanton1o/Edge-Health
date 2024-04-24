class Device:
    """ Device model, this device model is used to store the device data in the database
    Example of a device model:
        "_id":"123456789",
        "brand":"Ticwatch",
        "type": "smartwatch",
        "sensors":{
            "gyroscope":{
                "x":[[1,2,3]],
                "y":[[1,2,3]],
                "z":[[1,2,3]]
            },
            "gyroscope_timestamp":[ 1111111111, 1111111112, 1111111113],
            .
            .
            .
        }
    """

    def __init__(self, _id, brand, type, sensors):
        self._id = _id
        self.brand = brand
        self.type = type
        self.sensors = sensors

    def json(self):
        return {
            "_id": self._id,
            "brand": self.brand,
            "type": self.type,
            "sensors": self.sensors
        }

    @classmethod
    def from_json(cls, json):
        return cls(**json)