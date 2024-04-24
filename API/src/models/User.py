from pymongo import MongoClient
from dotenv import load_dotenv
import os

# Load environment variables from .env file
dotenv_path = os.path.join(os.path.dirname(__file__), '.env')
load_dotenv(dotenv_path)
print(dotenv_path)
MONGO_USERNAME = os.getenv('MONGO_USERNAME')
MONGO_PASSWORD = os.getenv('MONGO_PASSWORD')

client = MongoClient('mongodb://mongodb:27017/')

db = client.users
collection = db.users

class User:
    """ User model, this user model is used to store the user data in the database
    Example of a user model:
        "_id":"123456789"
        "username": "John95",
        "email": "John95@fake_email.com",
        "password": "Ahashedpassword"
        "dob": 30,
        "risk_factors":["smoker", "sedentary"],
        "devices":[123456789,33333333],
        "disabled_devices":[2222222]
    """
    def __init__(self, username, email, password, dob, devices, disabled_devices):
        self.username = username
        self.email = email
        self.password = password
        self.dob = dob
        self.devices = devices
        self.disabled_devices = disabled_devices

    def json(self):
        return {
            "username": self.username,
            "email": self.email,
            "password": self.password,
            "dob": self.dob,
            "devices": self.devices,
            "disabled_devices": self.disabled_devices
        }

    @classmethod
    def get_by_id(self, _id):
        user = collection.find_one({"_id":_id})
        return user
    
    @classmethod
    def get_by_username(self, username):
        user = collection.find_one({"username":username})
        return user
    
    @classmethod
    def create(self, user_json):
        new_user = collection.insert_one(user_json)
        return new_user.inserted_id

    @classmethod
    def update(self, username, updated_content):
        user = collection.find_one({"username":username})
        if user is None:
            return None
        collection.update_one({"username":username}, {"$set":updated_content})
        return collection.find_one({"username":username})

    @classmethod
    def from_json(cls, json):
        return cls(**json)