class User:
    """ User model, this user model is used to store the user data in the database
    Example of a user model:
        "_id":"123456789"
        "username": "John95",
        "email": "John95@fake_email.com",
        "password": "Ahashedpassword"
        "age": 30,
        "risk_factors":["smoker", "sedentary"],
        "devices":[123456789,33333333],
        "disabled_devices":[2222222]
    """
    def __init__(self, _id, username, email, password, age, devices, disabled_devices):
        self._id = _id
        self.username = username
        self.email = email
        self.password = password
        self.age = age
        self.devices = devices
        self.disabled_devices = disabled_devices

    def json(self):
        return {
            "_id": self._id,
            "username": self.username,
            "email": self.email,
            "password": self.password,
            "age": self.age,
            "devices": self.devices,
            "disabled_devices": self.disabled_devices
        }

    @classmethod
    def from_json(cls, json):
        return cls(**json)